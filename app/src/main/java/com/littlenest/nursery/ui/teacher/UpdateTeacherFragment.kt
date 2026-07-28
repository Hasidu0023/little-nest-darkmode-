    package com.littlenest.nursery.ui.teacher

    import android.app.AlertDialog
    import android.net.Uri
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.Toast
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.ViewModelProvider
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.resource.bitmap.CircleCrop
    import com.littlenest.nursery.R
    import com.littlenest.nursery.databinding.FragmentTeacherUpdateBinding
    import com.littlenest.nursery.ui.common.BaseFragment
    import com.littlenest.nursery.ui.group.Group
    import com.littlenest.nursery.viewmodel.group.GroupViewModel
    import java.io.File
    import okhttp3.MultipartBody
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.RequestBody.Companion.asRequestBody
    //import android.widget.ImageView
    //import com.google.android.material.navigation.NavigationView


    class UpdateTeacherFragment :
        BaseFragment(R.layout.fragment_teacher_update) {

        private lateinit var binding: FragmentTeacherUpdateBinding

        private val teacherViewModel: TeacherViewModel by viewModels()
        private lateinit var groupViewModel: GroupViewModel

        private lateinit var teacher: SingleTeacher
        private var groupList: List<Group> = emptyList()

        // selected groups
        private val selectedGroupIds = mutableListOf<Int>()

        // image
        private var selectedImageUri: Uri? = null
        private lateinit var pickImageLauncher: ActivityResultLauncher<String>

        private lateinit var token: String
        private lateinit var apiKey: String

        // ------------------------------------------------
        // Lifecycle
        // ------------------------------------------------

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding = FragmentTeacherUpdateBinding.bind(view)

            token = getToken() ?: ""
            apiKey = getApiKey()

            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show()
                handleLogout()
                return
            }

            setupImagePicker()
            setupGenderSpinner()
            setupGroupViewModel()
            setupObservers()
            setupUploadButton()
            setupSubmitButton()
            fetchTeacher()
        }

        // ------------------------------------------------
        // Fetch teacher
        // ------------------------------------------------

        private fun fetchTeacher() {
            teacherViewModel.fetchTeacherById(
                teacherId = getProfileId(),
                token = token,
                apiKey = apiKey
            )
        }

        // ------------------------------------------------
        // Observers
        // ------------------------------------------------

        private fun setupObservers() {

            teacherViewModel.teacherById.observe(viewLifecycleOwner) {
                teacher = it
                populateTeacherBasicData()

                // groups might already be loaded
                if (groupList.isNotEmpty()) {
                    preselectAssignedGroups()
                }
            }

            teacherViewModel.loading.observe(viewLifecycleOwner) {
                binding.btnSubmit.isEnabled = !it
            }

            teacherViewModel.error.observe(viewLifecycleOwner) {
                it?.let { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }

            teacherViewModel.updateResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess {
                    Toast.makeText(requireContext(), "Teacher updated successfully", Toast.LENGTH_SHORT).show()

    //                val imageUrl =
    //                    "${uploadsBaseUrl()}${teacher.extraData.profilePicture}?t=${System.currentTimeMillis()}"


                    // Update drawer header image
    //                val navView =
    //                    requireActivity().findViewById<NavigationView>(R.id.nav_view)
    //                val headerView = navView.getHeaderView(0)
    //                val imgProfileHeader: ImageView = headerView.findViewById(R.id.headerImage)
    //
    //                val profileUrl = uploadsBaseUrl() + teacher.extraData.profilePicture
    //                if (!profileUrl.isNullOrEmpty()) {
    //                    Glide.with(this)
    //                        .load(profileUrl)
    //                        .transform(CircleCrop())
    //                        .placeholder(R.drawable.avatar_placeholder)
    //                        .into(imgProfileHeader)
    //                }
    //
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                result.onFailure { exception ->
                    Log.e("Teacher Update failed", "Error updating teacher", exception)
                    //Toast.makeText(requireContext(), it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                    // Show the backend error if available
                    Toast.makeText(
                        requireContext(),
                        exception.message ?: "Update failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // ------------------------------------------------
        // Groups
        // ------------------------------------------------

        private fun setupGroupViewModel() {
            groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
            groupViewModel.fetchGroups(token, apiKey)

            groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
                if (groups.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                groupList = groups
                setupGroupSelectionDialog()

                // teacher might already be loaded
                if (::teacher.isInitialized) {
                    preselectAssignedGroups()
                }
            }

            groupViewModel.error.observe(viewLifecycleOwner) {
                it?.let { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
            }
        }

        private fun setupGroupSelectionDialog() {
            binding.btnSelectGroups.setOnClickListener {

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
                        updateGroupButtonText()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun preselectAssignedGroups() {
            selectedGroupIds.clear()
            selectedGroupIds.addAll(teacher.extraData.assignedGroups.map { it.id })
            updateGroupButtonText()
        }

        private fun updateGroupButtonText() {
            val selectedNames = selectedGroupIds.mapNotNull { id ->
                groupList.find { it.id == id }?.name
            }

            binding.btnSelectGroups.text =
                if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ")
                else "Select Groups"
        }

        // ------------------------------------------------
        // Image Picker
        // ------------------------------------------------

        private fun setupImagePicker() {
            pickImageLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    uri?.let {
                        selectedImageUri = it
                        Glide.with(this)
                            .load(it)
                            .circleCrop()
                            .into(binding.imgProfile)
                    }
                }
        }

        private fun setupUploadButton() {
            binding.btnUploadPicture.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
        }

        // ------------------------------------------------
        // UI
        // ------------------------------------------------

        private fun setupGenderSpinner() {
            val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")

            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item,
                genderOptions
            )

            adapter.setDropDownViewResource(R.layout.spinner_item)

            binding.spinnerGender.adapter = adapter
        }

        private fun populateTeacherBasicData() {
            binding.etName.setText(teacher.extraData.name)
            binding.teacherUsername.setText(teacher.username)
            //binding.teacherUsername.isEnabled = false
            //binding.teacherUsername.alpha = 0.6f
            binding.teacherPassword.setText("")

            val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
            val index = genderOptions.indexOfFirst {
                it.equals(teacher.gender, ignoreCase = true)
            }
            if (index >= 0) binding.spinnerGender.setSelection(index)

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

        // ------------------------------------------------
        // Submit
        // ------------------------------------------------

        private fun setupSubmitButton() {
            binding.btnSubmit.setOnClickListener {
                //Log.d("updateteacher enter", "sss")

                if (selectedGroupIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select at least one group", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

//                val request = RegisterRequestTeacher(
//                    username = teacher.username,
//                    //password = binding.teacherPassword.text.toString().ifEmpty { "unchanged" },
//                    password = binding.teacherPassword.text.toString(),
//                    gender = binding.spinnerGender.selectedItem.toString().lowercase(),
//                    role = "teacher",
//                    extraData = RegisterTeacherExtraData(
//                        name = binding.etName.text.toString().trim(),
//                        profilePicture = teacher.extraData.profilePicture, // backend handles new image
//                        nurseryId = teacher.extraData.nursery.id,
//                        assignedGroups = selectedGroupIds
//                    )
//                )

                val request = UpdateTeacherRequest(
                    username = binding.teacherUsername.text.toString(),
                    password = binding.teacherPassword.text.toString().ifBlank { null },
                    gender = binding.spinnerGender.selectedItem.toString().lowercase(),
                    name = binding.etName.text.toString().trim(),
                    nurseryId = teacher.extraData.nursery.id,
                    assignedGroups = selectedGroupIds,
                    profilePicture = teacher.extraData.profilePicture
                )


                val imagePart = selectedImageUri?.let { uriToMultipart(it) }

                Log.d("updateteacher1", "$request")
                if (selectedImageUri == null) {
                    //Log.d("updateteacher2", "dkdkdk")
                    teacherViewModel.updateTeacher(
                        teacherId = teacher.teacherId,
                        request = request,
                        token = token,
                        apiKey = apiKey
                    )
                } else {
                    teacherViewModel.updateTeacherWithImage(
                        teacherId = teacher.teacherId,
                        request = request,
                        imagePart = imagePart,
                        token = token,
                        apiKey = apiKey
                    )
                }



    //            teacherViewModel.updateTeacherWithImage(
    //                teacherId = teacher.teacherId,
    //                request = request,
    //                imageUri = selectedImageUri,
    //                token = token,
    //                apiKey = apiKey,
    //                context = requireContext()
    //            )
            }
        }

        private fun uriToMultipart(uri: Uri): MultipartBody.Part {
            val inputStream = requireContext().contentResolver.openInputStream(uri)!!
            val file = File(requireContext().cacheDir, "teacher_${System.currentTimeMillis()}.jpg")

            file.outputStream().use {
                inputStream.copyTo(it)
            }

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("profilePicture", file.name, requestBody)
        }
    }
