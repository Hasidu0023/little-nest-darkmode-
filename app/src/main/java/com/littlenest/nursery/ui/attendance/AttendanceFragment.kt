package com.littlenest.nursery.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyAttendanceBinding
import com.littlenest.nursery.ui.common.BaseFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Assuming your layout file is in R.layout.fragment_family_attendance
class AttendanceFragment : BaseFragment(R.layout.fragment_family_attendance) {

    private var _binding: FragmentFamilyAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels()

    // Variable to hold the date selected by the user, formatted as YYYY-MM-DD
    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Use the BaseFragment's setupUI method
    override fun setupUI(view: View) {
        super.setupUI(view)

        // ----------------------------------------------------
        // NEW: Disable all past dates by setting the minimum date to the current time.
        // Subtracting a small amount (like 1000ms) ensures that the current day is fully selectable.
        binding.calendarView.minDate = System.currentTimeMillis() - 1000
        // ----------------------------------------------------

        // 1. Initialize the display text with today's date
        binding.textDateToMark.text = "Selected Date: $selectedDate"

        // 2. Set the listener for the CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                // CalendarView returns month as 0-11, so we add 1 for display/formatting
                set(year, month, dayOfMonth)
            }

            // Format the date into the required YYYY-MM-DD string
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            binding.textDateToMark.text = "Selected Date: $selectedDate"
        }

        // 3. Setup button click handler
        binding.buttonMarkAbsence.setOnClickListener {
            val authToken = getToken()
            val apiKey = getApiKey()

            if (authToken.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Authentication token missing.", Toast.LENGTH_LONG).show()
                handleLogout()
            } else if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date first.", Toast.LENGTH_SHORT).show()
            } else {
                // Note: If you need to select a student, you must update the ViewModel and Fragment
                // to include studentId here. For now, it assumes the student is determined by the token.
                viewModel.markAbsence(
                    date = selectedDate,
                    authToken = "Bearer $authToken",
                    apiKey = apiKey
                )
            }
        }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonMarkAbsence.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonMarkAbsence.text = if (isLoading) "Processing..." else "Mark Absence for Selected Date"
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty() && !viewModel.isLoading.value!!) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }

        viewModel.absenceRecord.observe(viewLifecycleOwner) { absence ->
            if (absence != null) {
                binding.textResult.text = "Absence successfully recorded:\nID: ${absence.id}, Date: ${absence.date}"
            } else {
                binding.textResult.text = "Absence Already "
            }
        }

        // Assuming 'viewModel' is an instance of AttendanceViewModel
        viewModel.message.observe(viewLifecycleOwner) { message ->
            // 1. Check if the message is not empty (it was just set by the ViewModel)
            if (message.isNotEmpty()) {

                // 2. Display the message to the user as a Toast
                //Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                binding.textResult.text = "$message"
                // 3. IMPORTANT: Clear the message immediately after showing it
                viewModel.clearMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}