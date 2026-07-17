package com.littlenest.nursery.ui.group

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentGroupListBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AlertDialog

class GroupListFragment : BaseFragment(R.layout.fragment_group_list) {

    private var _binding: FragmentGroupListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GroupAdapter
    private val viewModel: GroupViewModel by viewModels()
    private var groupsList: List<Group> = emptyList()

    override fun setupUI(view: View) {
        _binding = FragmentGroupListBinding.bind(view)
        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // Setup RecyclerView
        //adapter = GroupAdapter(emptyList())
        val role = getUserRole() ?: "student"
        adapter = GroupAdapter(
            groupsList,
            userRole = role,
            // ✅ GROUP ITEM CLICK
            onItemClick = { group ->
//                val action =
//                    GroupListFragmentDirections
//                        .actionGroupListFragmentToGroupStudentListFragment(
//                            groupId = group.id,
//                            groupName = group.name ?: ""
//                        )

                val action =
                    GroupListFragmentDirections
                        .actionGroupListFragmentToStudentFragment(
                            groupId = group.id,
                            groupName = group.name
                        )
                findNavController().navigate(action)
            },
            onEditClick = { group ->
                // Navigate to edit screen
                val action = GroupListFragmentDirections
                    .actionGroupListFragmentToEditGroupFragment(
                        groupId = group.id,
                        groupName = group.name ?: "",
                        groupDescription = group.description ?: ""
                    )
                findNavController().navigate(action)
                          },
            onDeleteClick = { group ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Group")
                    .setMessage("Are you sure you want to delete ${group.name}?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteGroup(token, apiKey, group.id)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )
        binding.recyclerViewGroups.adapter = adapter


        binding.recyclerViewGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGroups.adapter = adapter

        // Pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadGroups()
        }

        // Observe ViewModel
        observeViewModel()

        // Floating Add Button visibility
        //val role = getUserRole() ?: "student"
        when (role) {
            "admin" -> binding.GroupAddFloatingIcon.visibility = View.VISIBLE
            else -> binding.GroupAddFloatingIcon.visibility = View.GONE
        }


        // Floating Add Button
        binding.GroupAddFloatingIcon.setOnClickListener {
            // You can navigate to AddGroupFragment or show dialog here
            findNavController().navigate(R.id.action_groupListFragment_to_addGroupFragment)
        }

        // Initial load
        loadGroups()
    }

    private fun observeViewModel() {
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            adapter.updateData(groups)
            binding.textViewEmpty.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGroups() {
        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }
        viewModel.fetchGroups(token, apiKey)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}