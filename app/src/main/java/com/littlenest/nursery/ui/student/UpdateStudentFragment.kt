package com.littlenest.nursery.ui.student

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentStudentUpdateBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateStudentFragment : BaseFragment(R.layout.fragment_student_update) {

    private var _binding: FragmentStudentUpdateBinding? = null
    private val binding get() = _binding!!

    private val studentViewModel: StudentViewModel by viewModels()

    private var studentId: Int = -1
    private var selectedImageFile: File? = null

    // Image Picker Result Launcher
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri: Uri = data?.data!!
                binding.imageProfile.setImageURI(fileUri)
                selectedImageFile = File(fileUri.path!!)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
        }

    override fun setupUI(view: View) {
        _binding = FragmentStudentUpdateBinding.bind(view)

        // Retrieve the student object passed via Safe Args or Bundle
        @Suppress("DEPRECATION")
        val student = arguments?.getParcelable<Student>("student")
        
        if (student != null) {
            studentId = student.studentId
            populateFields(student) // Populate UI immediately with available data
        } else {
            studentId = arguments?.getInt("studentId") ?: arguments?.getInt("arg_student_id") ?: -1
        }

        observeViewModel()

        val token = getToken()
        val apiKey = getApiKey()

        // Fetch fresh data from API to ensure everything is current
        if (studentId != -1 && !token.isNullOrEmpty()) {
            studentViewModel.fetchStudentById(studentId, token, apiKey)
        } else if (student == null) {
            Toast.makeText(requireContext(), "Student data not found", Toast.LENGTH_SHORT).show()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSaveStudent.setOnClickListener {
            saveStudent()
        }
        
        // Removed btnUploadPicture as it was removed from the layout.
        // If you want to change the picture by clicking the profile image:
        binding.imageProfile.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.editDateOfBirth.setOnClickListener { showDatePicker() }
        binding.editDropOffTime.setOnClickListener { showTimePicker(binding.editDropOffTime) }
        binding.editPickupTime.setOnClickListener { showTimePicker(binding.editPickupTime) }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentDob = binding.editDateOfBirth.text.toString()
        if (currentDob.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                calendar.time = sdf.parse(currentDob)!!
            } catch (e: Exception) {
                Log.e("UpdateStudent", "Date parse error", e)
            }
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            binding.editDateOfBirth.setText(sdf.format(calendar.time))
        }
        
        DatePickerDialog(requireContext(), dateSetListener,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(editText: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        val currentVal = editText.text.toString()
        if (currentVal.isNotEmpty() && currentVal.contains(":")) {
            try {
                val parts = currentVal.split(":")
                calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                calendar.set(Calendar.MINUTE, parts[1].toInt())
            } catch (e: Exception) {}
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        }
        TimePickerDialog(requireContext(), timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun saveStudent() {
        val fullName = binding.editFullName.text.toString().trim()
        val dob = binding.editDateOfBirth.text.toString().trim()
        
        if (fullName.isEmpty()) {
            binding.editFullName.error = "Full Name is required"
            return
        }

        val token = getToken() ?: return
        val apiKey = getApiKey()

        // Convert UI date (dd-MM-yyyy) to API format (yyyy-MM-dd)
        val apiDob = try {
            val uiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = uiFormat.parse(dob)
            apiFormat.format(date!!)
        } catch (e: Exception) {
            dob
        }

        studentViewModel.updateStudentMultipart(
            studentId = studentId,
            token = token,
            apiKey = apiKey,
            fullName = fullName,
            nickname = binding.editNickname.text.toString().trim(),
            address = binding.editAddress.text.toString().trim(),
            city = binding.editCity.text.toString().trim(),
            nativeLanguage = binding.editNativeLanguage.text.toString().trim(),
            allergies = binding.editAllergies.text.toString().trim(),
            comment = binding.editComment.text.toString().trim(),
            dateOfBirth = apiDob,
            dropOffTime = binding.editDropOffTime.text.toString().trim(),
            pickupTime = binding.editPickupTime.text.toString().trim(),
            photoConsent = binding.checkboxPhotoConsent.isChecked,
            imageFile = selectedImageFile
        )
    }

    private fun observeViewModel() {
        studentViewModel.studentDetail.observe(viewLifecycleOwner) { student ->
            student?.let { populateFields(it) }
        }

        studentViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveStudent.isEnabled = !isLoading
        }

        studentViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Student updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        studentViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields(student: Student) {
        val extra = student.extraData

        with(binding) {
            editFullName.setText(extra.fullName)
            editNickname.setText(extra.nickname)
            editDateOfBirth.setText(formatDateForDisplay(extra.dateOfBirth))
            checkboxPhotoConsent.isChecked = extra.photoConsent == true
            editNativeLanguage.setText(extra.nativeLanguage)
            editDropOffTime.setText(extra.dropOffTime?.take(5) ?: "")
            editPickupTime.setText(extra.pickupTime?.take(5) ?: "")
            editAddress.setText(extra.address)
            editCity.setText(extra.city)
            editAllergies.setText(extra.allergies)
            editComment.setText(extra.comment)

            val imageUrl = if (extra.profilePicture?.startsWith("http") == true) {
                extra.profilePicture
            } else {
                uploadsBaseUrl() + (extra.profilePicture ?: "")
            }

            if (!extra.profilePicture.isNullOrEmpty()) {
                Glide.with(this@UpdateStudentFragment)
                    .load(imageUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(imageProfile)
            } else {
                imageProfile.setImageResource(R.drawable.avatar_placeholder)
            }
        }
    }

    private fun formatDateForDisplay(apiDate: String?): String {
        if (apiDate.isNullOrEmpty()) return ""
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val uiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = apiFormat.parse(apiDate)
            date?.let { uiFormat.format(it) } ?: apiDate
        } catch (e: Exception) {
            apiDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
