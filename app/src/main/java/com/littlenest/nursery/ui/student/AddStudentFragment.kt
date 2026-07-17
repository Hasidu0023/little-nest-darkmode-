package com.littlenest.nursery.ui.student

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentAddStudentBinding
import com.littlenest.nursery.model.RegisterRequest
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.littlenest.nursery.ui.group.Group


class AddStudentFragment : BaseFragment(R.layout.fragment_add_student) {

    private var _binding: FragmentAddStudentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels()
    private val args: AddStudentFragmentArgs by navArgs() // 👈 get student if passed (for edit)

    private lateinit var token: String
    private lateinit var apiKey: String

    private lateinit var groupViewModel: GroupViewModel
    private var selectedGroupId: Int? = null
    private var groupList: List<Group> = emptyList()


    override fun setupUI(view: View) {
        _binding = FragmentAddStudentBinding.bind(view)
        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        // Initialize and fetch groups
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        observeGroupList()

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // Setup gender spinner
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter

        // Check if this is edit mode
        val student = try {
            args.student
        } catch (e: Exception) {
            null
        }

        setupListeners()

        binding.btnSubmit.setOnClickListener {
            if (student != null) updateStudent(student.studentId)
            else onSubmit()
        }

        observeViewModel()
    }

    private fun setupListeners(){
        // Date picker
        binding.etDateOfBirth.setOnClickListener {
            val dobText = binding.etDateOfBirth.text.toString().trim()
            val calendar = Calendar.getInstance()

            // Try to parse existing DOB if available
            if (dobText.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val parsedDate = format.parse(dobText)
                    parsedDate?.let { calendar.time = it }
                } catch (e: Exception) {
                    Log.e("DatePicker", "Invalid DOB format: $dobText", e)
                }
            }

            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    binding.etDateOfBirth.setText(formatDateForDisplay(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // Dropoff time picker
        binding.etDropOffTime.setOnClickListener { showTimePicker(binding.etDropOffTime) }

        // Pickup time picker
        binding.etPickupTime.setOnClickListener { showTimePicker(binding.etPickupTime) }

    }

    // ✅ Fill fields if editing
    private fun prefillStudentData(student: Student, genderOptions: Array<String>) {
        binding.etUsername.setText(student.username)
        binding.etPassword.setText("") // don’t show existing password
        binding.etFullName.setText(student.extraData.fullName)
        binding.etNickname.setText(student.extraData.nickname)
        binding.etDateOfBirth.setText(student.extraData.dateOfBirth)
        binding.etDropOffTime.setText(student.extraData.dropOffTime)
        binding.etPickupTime.setText(student.extraData.pickupTime)
        binding.etAddress.setText(student.extraData.address)
        binding.etCity.setText(student.extraData.city)
        binding.etLanguage.setText(student.extraData.nativeLanguage)
        binding.etAllergies.setText(student.extraData.allergies)
        binding.etComment.setText(student.extraData.comment)
        binding.cbPhotoConsent.isChecked = student.extraData.photoConsent ?: false

        //Works now, because groupList is already loaded
        val preselectedIndex = groupList.indexOfFirst { it.id == student.extraData.groupId }
        if (preselectedIndex >= 0) {
            binding.spinnerGroup.setSelection(preselectedIndex)
            selectedGroupId = student.extraData.groupId
        }

        // Preselect gender
       val genderIndex = genderOptions.indexOfFirst {
            it.equals(student.gender, ignoreCase = true)
        }
        if (genderIndex >= 0) {
            binding.spinnerGender.setSelection(genderIndex)
        }
    }

    // ✅ Add new student
    private fun onSubmit() {
        val request = collectFormData() ?: return
        viewModel.registerStudent(request, token, apiKey)
    }

    // ✅ Update existing student
    private fun updateStudent(studentId: Int) {
        val request = collectFormData() ?: return
        viewModel.updateStudent(studentId, request, token, apiKey)
    }

    // 🔹 Collect all form data and validate
    private fun collectFormData(): RegisterRequest? {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()
       // val dob = binding.etDateOfBirth.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val language = binding.etLanguage.text.toString().trim()
        val allergies = binding.etAllergies.text.toString().trim()
        val comment = binding.etComment.text.toString().trim()
        val dropOff = binding.etDropOffTime.text.toString().trim()
        val pickup = binding.etPickupTime.text.toString().trim()
        val consent = binding.cbPhotoConsent.isChecked

        val dob = binding.etDateOfBirth.text.toString().trim().let {
            if (it.isNotEmpty()) convertDateToApiFormat(it) else null
        }

        if(fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill full name field", Toast.LENGTH_SHORT).show()
            return null
        } else if (username.isEmpty() ) {
            Toast.makeText(requireContext(), "Please fill username field", Toast.LENGTH_SHORT).show()
            return null
        } else if(password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill password field", Toast.LENGTH_SHORT).show()
            return null
        } else if(gender.isEmpty()) {
            Toast.makeText(requireContext(), "Please select gender field", Toast.LENGTH_SHORT).show()
            return null
        } else if (dob == null) {
            Toast.makeText(requireContext(), "Please select a valid date of birth", Toast.LENGTH_SHORT).show()
            return null
        }

        val nurseryId = getNurseryId()
        val groupId = selectedGroupId ?: run {
            Toast.makeText(requireContext(), "Please select a group", Toast.LENGTH_SHORT).show()
            return null
        }

        val extraData = ExtraData(
            profilePicture = null,
            groupId = groupId,
            nurseryId = nurseryId,
            fullName = fullName,
            nickname = nickname,
            dropOffTime = dropOff.ifEmpty { null },
            pickupTime = pickup.ifEmpty { null },
            photoConsent = consent,
            dateOfBirth = dob,
            address = address,
            city = city,
            nativeLanguage = language,
            allergies = allergies,
            comment = comment,
            guardians = emptyList()
        )

        return RegisterRequest(
            username = username,
            password = password,
            gender = gender,
            role = "student",
            extraData = extraData
        )
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Student saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Student updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showTimePicker(targetEditText: View) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                (targetEditText as? android.widget.EditText)?.setText(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun formatDateForDisplay(date: String): String {
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val uiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            uiFormat.format(apiFormat.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun convertDateToApiFormat(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            Log.e("DateConversion", "Invalid date format: $date", e)
            date
        }
    }

    private fun formatDateForDisplay(calendar: Calendar): String {
        val uiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return uiFormat.format(calendar.time)
    }

    private fun observeGroupList() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            groupList = groups

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                groupList.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGroup.adapter = adapter

            binding.spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedGroupId = groupList[position].id
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedGroupId = null
                }
            }

            // ✅ Prefill student only after groups have loaded
            val student = try { args.student } catch (e: Exception) { null }
            if (student != null) {
                binding.btnSubmit.text = "Update Student"
                prefillStudentData(student, arrayOf("Select Gender", "Male", "Female", "Other"))
            }
        }

        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}