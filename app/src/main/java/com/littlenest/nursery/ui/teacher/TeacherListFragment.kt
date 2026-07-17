package com.littlenest.nursery.ui.teacher

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.databinding.FragmentTeacherListBinding
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import androidx.lifecycle.ViewModelProvider

class TeacherListFragment : BaseFragment(R.layout.fragment_teacher_list) {

    private var _binding: FragmentTeacherListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeacherViewModel by viewModels()
    private lateinit var groupViewModel: GroupViewModel

    private lateinit var adapter: TeacherAdapter
    private var groupList: List<Group> = emptyList()

    private lateinit var token: String
    private lateinit var apiKey: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeacherListBinding.bind(view)

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // Initialize GroupViewModel and fetch groups
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        observeGroups()

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        // Initial load
        viewModel.fetchTeachers(token, apiKey)
    }

    private fun setupRecyclerView() {
        adapter = TeacherAdapter(
            emptyList(),
            groups = groupList,
            baseUrl = uploadsBaseUrl(),
            onItemClick = { clickedTeacher ->
                val action = TeacherListFragmentDirections
                    .actionTeacherListFragmentToTeacherDetailFragment(clickedTeacher)
                findNavController().navigate(action)
            },
            onEditClick = { teacher ->
                val bundle = Bundle().apply {
                    putParcelable("teacher", teacher)
                }
                findNavController().navigate(R.id.action_teacherListFragment_to_updateTeacherFragment, bundle)
            },
            onDeleteClick = { teacher ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Teacher")
                    .setMessage("Are you sure you want to delete ${teacher.extraData.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTeacher(token, apiKey, teacher.teacherId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewTeachers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTeachers.adapter = adapter
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
            viewModel.fetchTeachers(token, apiKey)
        }

        binding.addTeacherFloatingIcon.setOnClickListener {
            // Navigate to Add Teacher screen if needed
            findNavController().navigate(R.id.action_teacherListFragment_to_addTeacherFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.teachers.observe(viewLifecycleOwner) { teachers ->
            if (teachers.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewTeachers.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewTeachers.visibility = View.VISIBLE
                adapter.updateData(teachers)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.d("teacherlisterror", "$error")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}