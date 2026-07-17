package com.littlenest.nursery.ui.curriculum

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentCurriculumListBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.curriculum.CurriculumViewModel

class CurriculumListFragment : BaseFragment(R.layout.fragment_curriculum_list) {

    private var _binding: FragmentCurriculumListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CurriculumViewModel by viewModels()
    private lateinit var adapter: CurriculumListAdapter

    private lateinit var token: String
    private lateinit var apiKey: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCurriculumListBinding.bind(view)

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        setupRecyclerView()
        observeViewModel()

        // Initial load
        viewModel.fetchCurriculums("Bearer $token", apiKey)
    }

    private fun setupRecyclerView() {
        //adapter = CurriculumListAdapter()
        adapter = CurriculumListAdapter(
            emptyList(),
            onEditClick = { curriculum ->
                val action =
                    CurriculumListFragmentDirections.actionCurriculumListFragmentToUpdateCurriculumFragment(
                        curriculum
                    )
                findNavController().navigate(action)
            },
            onDeleteClick = { curriculumId ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Curriculum")
                    .setMessage("Are you sure you want to delete this curriculum?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteCurriculum(token, apiKey, curriculumId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewCurriculum.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCurriculum.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchCurriculums("Bearer $token", apiKey)
        }

        // Optional: Add navigation to `AddCurriculumFragment`
        binding.addCurriculumFAB.setOnClickListener {
            findNavController().navigate(R.id.action_curriculumListFragment_to_addCurriculumFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.curriculums.observe(viewLifecycleOwner) { response ->
            if (response.groupedCurriculums.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewCurriculum.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewCurriculum.visibility = View.VISIBLE

                val formattedList = mutableListOf<CurriculumResponse>()
                response.groupedCurriculums.forEach { (standard, curriculums) ->
                    curriculums.forEach { curriculum ->
                        formattedList.add(curriculum.copy(standard = standard))
                    }
                }
                adapter.updateData(formattedList)
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
