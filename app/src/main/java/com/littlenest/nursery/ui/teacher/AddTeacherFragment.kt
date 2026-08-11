package com.littlenest.nursery.ui.teacher

import android.app.AlertDialog
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentTeacherAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel

class AddTeacherFragment : BaseFragment(R.layout.fragment_teacher_add) {

    private var _binding: FragmentTeacherAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeacherViewModel by viewModels()
    private val args: AddTeacherFragmentArgs by navArgs()

    private lateinit var token: String
    private lateinit var apiKey: String

    private lateinit var groupViewModel: GroupViewModel
    private var groupList: List<Group> = emptyList()

    // ✅ Multi-select: holds selected group IDs
    private val selectedGroupIds = mutableListOf<Int>()

    override fun setupUI(view: View) {
        _binding = FragmentTeacherAddBinding.bind(view)
        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        setupGenderSpinner()
        setupGroupViewModel()
        setupSubmitButton()
        observeViewModel()
    }

    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            genderOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerGender.adapter = adapter
    }

    private fun setupGroupViewModel() {
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        observeGroupList()
    }

    private fun observeGroupList() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            groupList = groups
            setupGroupSelection() // ✅ setup dialog selector once groups are loaded
            prefillTeacherIfEditing()
        }

        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    // ✅ Multi-select group dialog
    private fun setupGroupSelection() {
        binding.btnSelectGroups.setOnClickListener {
            if (groupList.isEmpty()) {
                Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val groupNames = groupList.map { it.name }.toTypedArray()
            val checkedItems = BooleanArray(groupNames.size) { index ->
                selectedGroupIds.contains(groupList[index].id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select Groups")
                .setMultiChoiceItems(groupNames, checkedItems) { _, which, isChecked ->
                    val groupId = groupList[which].id
                    if (isChecked) selectedGroupIds.add(groupId)
                    else selectedGroupIds.remove(groupId)
                }
                .setPositiveButton("OK") { dialog, _ ->
                    val selectedNames = selectedGroupIds.mapNotNull { id ->
                        groupList.find { it.id == id }?.name
                    }
                    binding.btnSelectGroups.text =
                        if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ")
                        else "Select Groups"
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun prefillTeacherIfEditing() {
        val teacher = try { args.teacher } catch (e: Exception) { null } ?: return

        binding.etName.setText(teacher.extraData.name)
        binding.teacherUsername.setText(teacher.username)
        binding.teacherPassword.setText("")

        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val genderIndex = genderOptions.indexOfFirst { it.equals(teacher.gender, ignoreCase = true) }
        if (genderIndex >= 0) binding.spinnerGender.setSelection(genderIndex)

        selectedGroupIds.addAll(
            teacher.extraData.assignedGroups.mapNotNull { groupName ->
                groupList.find { it.name.equals(groupName, ignoreCase = true) }?.id
            }
        )

        val selectedNames = selectedGroupIds.mapNotNull { id ->
            groupList.find { it.id == id }?.name
        }
        binding.btnSelectGroups.text =
            if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ")
            else "Select Groups"

        // load existing image
        val profileUrl = uploadsBaseUrl() + teacher.extraData.profilePicture
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.avatar_placeholder)
                .into(binding.imgProfile)
        }

        binding.btnSubmit.text = "Update Teacher"
    }

    private fun setupSubmitButton() {
        val teacher = try { args.teacher } catch (e: Exception) { null }

        binding.btnSubmit.setOnClickListener {
            if (teacher != null) updateTeacher(teacher.teacherId)
            else onSubmit()
        }
    }

    private fun onSubmit() {
        val request = collectFormData() ?: return
        viewModel.registerTeacher(request, token, apiKey)
    }

    private fun updateTeacher(teacherId: Int) {
        val teacher = try { args.teacher } catch (e: Exception) { null } ?: return

        val username = binding.teacherUsername.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateTeacherRequest(
            username = username,
            password = binding.teacherPassword.text.toString().ifBlank { null },
            gender = binding.spinnerGender.selectedItem.toString().lowercase(),
            name = binding.etName.text.toString().trim(),
            nurseryId = getNurseryId(),
            assignedGroups = selectedGroupIds,
            profilePicture = teacher.extraData.profilePicture
        )
        //val updateRequest = collectUpdateFormData() ?: return
        viewModel.updateTeacher(teacherId, updateRequest, token, apiKey)
    }



    private fun collectFormData(): RegisterRequestTeacher? {
        val name = binding.etName.text.toString().trim()
        val username = binding.teacherUsername.text.toString().trim()
        val password = binding.teacherPassword.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || gender == "Select Gender") {
            //Log.d("updateteacher add", "addFragment")
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return null
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return null
        }

        if (selectedGroupIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one group", Toast.LENGTH_SHORT).show()
            return null
        }

        val nurseryId = getNurseryId()

        val extraData = RegisterTeacherExtraData(
            profilePicture = null,
            nurseryId = nurseryId,
            assignedGroups = selectedGroupIds,
            name = name
        )

        return RegisterRequestTeacher(
            username = username,
            password = password,
            gender = gender.lowercase(),
            role = "teacher",
            extraData = extraData
        )
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Teacher saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Teacher updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}