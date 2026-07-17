package com.littlenest.nursery.ui.attendance_summary

import android.app.DatePickerDialog
import android.view.View
import androidx.fragment.app.viewModels
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentAttendanceSummaryBinding
import com.littlenest.nursery.ui.common.BaseFragment
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager

class AttendanceSummaryFragment :
    BaseFragment(R.layout.fragment_attendance_summary) {

    private lateinit var binding: FragmentAttendanceSummaryBinding
    private val viewModel: AttendanceSummaryViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    private val absentAdapter = AbsentStudentAdapter(uploadsBaseUrl())

    override fun setupUI(view: View) {
        binding = FragmentAttendanceSummaryBinding.bind(view)

        binding.rvAbsentStudents.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvAbsentStudents.adapter = absentAdapter

        val args = AttendanceSummaryFragmentArgs.fromBundle(requireArguments())
        loadAttendance(args.groupId, getToday())

        // Display group name
        binding.tvGroupName.text = "GROUP: " + args.groupName.uppercase()

        binding.btnPickDate.setOnClickListener {
            openDatePicker(args.groupId)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.tvDate.text = summary.date
            binding.tvPresentCount.text =
                "Present: ${summary.presentCount} / ${summary.totalStudents}"
            absentAdapter.submitList(summary.absentStudents)

            // Show empty message if list is empty
            binding.tvEmptyMessage.visibility =
                if (summary.absentStudents.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openDatePicker(groupId: Int) {
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                calendar.set(y, m, d)
                loadAttendance(groupId, formatDate(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadAttendance(groupId: Int, date: String) {
        val token = getToken() ?: return
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }
        viewModel.loadAttendance(token, apiKey, groupId, date)
    }

    private fun getToday(): String =
        formatDate(Date())

    private fun formatDate(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
}




//package com.example.nurseryapp.ui.attendance_summary
//
//import android.os.Bundle
//import android.view.View
//import android.widget.AdapterView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.nurseryapp.R
//import com.example.nurseryapp.databinding.FragmentAttendanceSummaryBinding
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//import kotlin.collections.get
//
//class AttendanceSummaryFragment :
//    Fragment(R.layout.fragment_attendance_summary) {
//
//    private lateinit var binding: FragmentAttendanceSummaryBinding
//    //private val viewModel: AttendanceSummaryViewModel by viewModels()
//
//    private var selectedGroupId: Int? = null
//    private var selectedDate: String = today()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentAttendanceSummaryBinding.bind(view)
//
//        setupRecyclerView()
//        setupCalendar()
//        setupObservers()
//
//        viewModel.fetchGroups()
//    }
//
//    private fun setupCalendar() {
//        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
//            val cal = Calendar.getInstance()
//            cal.set(year, month, day)
//
//            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                .format(cal.time)
//
//            fetchAttendance()
//        }
//    }
//
//    private fun setupRecyclerView() {
//        binding.recyclerAbsentStudents.layoutManager =
//            LinearLayoutManager(requireContext())
//        binding.recyclerAbsentStudents.adapter =
//            AbsentStudentAdapter(emptyList())
//    }
//
//    private fun setupObservers() {
//
//        viewModel.groups.observe(viewLifecycleOwner) { groups ->
//            val adapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_item,
//                groups.map { it.name }
//            )
//            adapter.setDropDownViewResource(
//                android.R.layout.simple_spinner_dropdown_item
//            )
//
//            binding.spinnerGroup.adapter = adapter
//
//            binding.spinnerGroup.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>,
//                        view: View?,
//                        position: Int,
//                        id: Long
//                    ) {
//                        selectedGroupId = groups[position].id
//                        fetchAttendance()
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>) {}
//                }
//        }
//
//        viewModel.absentStudents.observe(viewLifecycleOwner) {
//            binding.recyclerAbsentStudents.adapter =
//                AbsentStudentAdapter(it)
//        }
//
//        viewModel.error.observe(viewLifecycleOwner) {
//            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun fetchAttendance() {
//        val groupId = selectedGroupId ?: return
//        viewModel.fetchAbsentStudents(groupId, selectedDate)
//    }
//
//    private fun today(): String =
//        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            .format(Date())
//}