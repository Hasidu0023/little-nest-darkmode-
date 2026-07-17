package com.littlenest.nursery.ui.family

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentStudentUpdateBinding
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.StudentViewModel
import java.text.SimpleDateFormat
import java.util.*

class UpdateStudentFragment : BaseFragment(R.layout.fragment_student_update) {

    private var _binding: FragmentStudentUpdateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private lateinit var student: Student

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Glide.with(this)
                    .load(it)
                    .transform(CircleCrop())
                    .into(binding.imageProfile)
            }
        }


//    private val pickImageLauncher =
//        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//            uri?.let {
//                // Persist permission to read this URI
//                requireContext().contentResolver.takePersistableUriPermission(
//                    it,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION
//                )
//
//                selectedImageUri = it
//                Glide.with(this)
//                    .load(it)
//                    .transform(CircleCrop())
//                    .into(binding.imageProfile)
//            }
//        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        student = arguments?.getParcelable("student") ?: return

        populateFields(student)
        setupListeners()
        observeViewModel()
    }

    private fun populateFields(student: Student) {
        val extra = student.extraData

        binding.editFullName.setText(extra.fullName)
        binding.editNickname.setText(extra.nickname)
        binding.editAddress.setText(extra.address)
        binding.editCity.setText(extra.city)
        binding.editNativeLanguage.setText(extra.nativeLanguage)
        binding.editAllergies.setText(extra.allergies)
        binding.editComment.setText(extra.comment)
        binding.checkboxPhotoConsent.isChecked = extra.photoConsent == true
        binding.editDateOfBirth.setText(formatDateForDisplay(extra.dateOfBirth ?: "N/A"))
        binding.editDropOffTime.setText(extra.dropOffTime?.substring(0, 5))
        binding.editPickupTime.setText(extra.pickupTime?.substring(0, 5))

        val profileUrl =
            if (extra.profilePicture?.startsWith("http") == true) extra.profilePicture
            else uploadsBaseUrl() + (extra.profilePicture ?: "")

        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.avatar_placeholder)
                .into(binding.imageProfile)
        }
    }

    private fun setupListeners() {
        // Pick image
        binding.btnUploadPicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
//        binding.btnUploadPicture.setOnClickListener {
//            pickImageLauncher.launch(arrayOf("image/*"))
//        }

        // Date picker
        binding.editDateOfBirth.setOnClickListener {
            val dobText = binding.editDateOfBirth.text.toString().trim()
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
                    binding.editDateOfBirth.setText(formatDateForDisplay(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // Dropoff time picker
        binding.editDropOffTime.setOnClickListener { showTimePicker(binding.editDropOffTime) }

        // Pickup time picker
        binding.editPickupTime.setOnClickListener { showTimePicker(binding.editPickupTime) }

        // Save button
        binding.btnSaveStudent.setOnClickListener { saveStudent() }
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

    private fun saveStudent() {
        val fullName = binding.editFullName.text.toString().trim()
        val nickname = binding.editNickname.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val city = binding.editCity.text.toString().trim()
        val nativeLang = binding.editNativeLanguage.text.toString().trim()
        val allergies = binding.editAllergies.text.toString().trim()
        val comment = binding.editComment.text.toString().trim()
        val dob = convertDateToApiFormat(binding.editDateOfBirth.text.toString().trim())
        val dropOff = binding.editDropOffTime.text.toString().trim()
        val pickup = binding.editPickupTime.text.toString().trim()
        val photoConsent = binding.checkboxPhotoConsent.isChecked

        if (fullName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Update student object
        // Create a new updated ExtraData using copy()
        val updatedExtra = student.extraData.copy(
            fullName = fullName,
            nickname = nickname,
            address = address,
            city = city,
            nativeLanguage = nativeLang,
            allergies = allergies,
            comment = comment,
            dateOfBirth = dob,
            dropOffTime = dropOff,
            pickupTime = pickup,
            photoConsent = photoConsent
        )
        // Create a new Student with updated extraData
        val updatedStudent = student.copy(extraData = updatedExtra)
        Log.d("updatedStudent", "$updatedStudent")

        // Send to ViewModel for API call
        val authToken = getToken()
        val apiKey = getApiKey()
        val studentId = getProfileId()

        viewModel.updateStudentWithImage(
            updatedStudent,
            selectedImageUri,
            "Bearer $authToken",
            apiKey,
            requireContext(), //The context is required because inside the ViewModel, we convert the Uri (from gallery) to a File
            studentId
        )
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveStudent.isEnabled = !isLoading
            if (isLoading) Toast.makeText(requireContext(), "Updating...", Toast.LENGTH_SHORT).show()
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Student updated successfully", Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to update student", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
