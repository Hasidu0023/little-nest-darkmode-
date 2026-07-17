package com.littlenest.nursery.ui.curriculumNew

import androidx.lifecycle.*
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import androidx.navigation.fragment.findNavController

class StandardListFragment :
    BaseFragment(R.layout.fragment_standard_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: StandardAdapter
    private lateinit var token: String
    private lateinit var apiKey: String

    private val viewModel: StandardViewModel by viewModels()

    override fun setupUI(view: View) {

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecycler()
        observeData()

        viewModel.fetchStandards("Bearer $token", apiKey)

    }

    private fun setupRecycler() {
        adapter = StandardAdapter(emptyList(),
            onItemClick = { standard ->

                val action = StandardListFragmentDirections
                    .actionStandardListFragmentToCurriculumListFragment(
                        standard.id,
                        standard.name
                    )
                findNavController().navigate(action)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.standards.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCurriculum(standardId: Int, name: String) {
        // TODO: implement navigation later
        Toast.makeText(requireContext(), "Clicked: $name", Toast.LENGTH_SHORT).show()
    }
}