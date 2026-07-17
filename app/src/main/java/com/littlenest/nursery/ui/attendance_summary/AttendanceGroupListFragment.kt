package com.littlenest.nursery.ui.attendance_summary

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentAttendanceGroupListBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.group.GroupViewModel

class AttendanceGroupListFragment :
    BaseFragment(R.layout.fragment_attendance_group_list) {

    private var _binding: FragmentAttendanceGroupListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupViewModel by viewModels()
    private lateinit var adapter: AttendanceGroupAdapter

    override fun setupUI(view: View) {
        _binding = FragmentAttendanceGroupListBinding.bind(view)

        adapter = AttendanceGroupAdapter(emptyList()) { group ->
            val action =
                AttendanceGroupListFragmentDirections
                    .actionAttendanceGroupListFragmentToAttendanceSummaryFragment(
                        groupId = group.id,
                        groupName = group.name ?: ""
                    )
            findNavController().navigate(action)
        }

        binding.recyclerViewGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGroups.adapter = adapter

        observeViewModel()
        loadGroups()
    }

    private fun observeViewModel() {
        viewModel.groups.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGroups() {
        val token = getToken() ?: return
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }
        viewModel.fetchGroups(token, apiKey)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
