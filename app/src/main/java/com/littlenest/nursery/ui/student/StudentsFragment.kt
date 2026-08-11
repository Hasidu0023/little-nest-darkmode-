package com.littlenest.nursery.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.databinding.FragmentStudentsBinding
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import androidx.lifecycle.ViewModelProvider

class StudentsFragment : BaseFragment(R.layout.fragment_students) {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels()
    private lateinit var groupViewModel: GroupViewModel

    private lateinit var adapter: StudentAdapter
    private var groupList: List<Group> = emptyList()

    private lateinit var token: String
    private lateinit var apiKey: String

    private var selectedGroupId: Int? = null
    private var selectedGroupName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsBinding.bind(view)
        val role = getUserRole() ?: "student"

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // 🔐 Admin-only FAB
        binding.addStudentFloatingIcon.visibility =
            if (role == "admin") View.VISIBLE else View.GONE

        // Initialize GroupViewModel and fetch groups
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        observeGroups()


        //student list from the group list
        val args = StudentsFragmentArgs.fromBundle(requireArguments())
        selectedGroupId = args.groupId
        selectedGroupName = args.groupName

        selectedGroupName?.let {
            binding.tvTitle.text = it.uppercase()
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        // Initial load
        if (selectedGroupId != null) {
            viewModel.fetchStudentsByGroup(token, apiKey, selectedGroupId!!)
        } else {
            viewModel.fetchStudents(token, apiKey)
        }

    }

    private fun setupRecyclerView() {
        val role = getUserRole() ?: "student"
        adapter = StudentAdapter(
            emptyList(),
            groups = groupList,
            uploadsBaseUrl = uploadsBaseUrl(),
            userRole = role,
            onItemClick = { clickedStudent ->
                val action = StudentsFragmentDirections
                    .actionStudentsFragmentToStudentDetailFragment(clickedStudent)
                findNavController().navigate(action)
            },
            onEditClick = { student ->
                val action = StudentsFragmentDirections
                    .actionStudentsFragmentToUpdateStudentFragment(student)
                findNavController().navigate(action)
            },
            onDeleteClick = { student ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete ${student.extraData.fullName}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteStudent(token, apiKey, student.studentId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewStudents.adapter = adapter
    }

    private fun observeGroups() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupList = groups
            adapter.updateGroups(groups) // Update adapter with new groups
        }

        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStudents(token, apiKey)
        }

        binding.addStudentFloatingIcon.setOnClickListener {
            // Navigate to Add Student screen if needed
            findNavController().navigate(R.id.action_studentsFragment_to_addStudentFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.students.observe(viewLifecycleOwner) { students ->
            if (students.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewStudents.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewStudents.visibility = View.VISIBLE
                adapter.updateData(students)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
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