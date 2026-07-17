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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.AlertDialog
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Button
import android.widget.TextView

class SubTopicListFragment :
    BaseFragment(R.layout.fragment_subtopic_list) {

    private val viewModel: SubTopicViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: SubTopicAdapter

    private lateinit var token: String
    private lateinit var apiKey: String

    override fun setupUI(view: View) {

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        token = "Bearer " + (getToken() ?: "")
        apiKey = getApiKey()

        val curriculumId = arguments?.getInt("curriculumId") ?: 0

        setupRecycler()
        observeData()

        viewModel.fetchSubTopics(token, apiKey, curriculumId)

        // ✅ FAB click
        val fab: FloatingActionButton = view.findViewById(R.id.addSubTopicFab)
        fab.setOnClickListener {
            showCreateDialog(curriculumId)
        }
    }

    private fun setupRecycler() {
        adapter = SubTopicAdapter(emptyList(),
            onItemClick = { subtopic ->
                val action = SubTopicListFragmentDirections
                    .actionSubTopicListFragmentToActivityListFragment(
                        subtopic.id,
                    )
                findNavController().navigate(action)
            },
            onDeleteClick = { subTopic ->
                confirmDelete(subTopic.id)
            },
            onEditClick = { subTopic ->
                showEditDialog(subTopic)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.subTopics.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.createSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Created!", Toast.LENGTH_SHORT).show()

                val curriculumId = arguments?.getInt("curriculumId") ?: 0
                viewModel.fetchSubTopics(token, apiKey, curriculumId)

                viewModel.createSuccess.value = false // 🔥 reset
            }
        }

        viewModel.deleteSubTopicSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()

                val curriculumId = arguments?.getInt("curriculumId") ?: 0
                viewModel.fetchSubTopics(token, apiKey, curriculumId)

                viewModel.deleteSubTopicSuccess.value = false
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()

                val curriculumId = arguments?.getInt("curriculumId") ?: 0
                viewModel.fetchSubTopics(token, apiKey, curriculumId)

                viewModel.updateSuccess.value = false
            }
        }
    }

    private fun showCreateDialog(curriculumId: Int) {

        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_add_subtopic, null)

        val input = sheetView.findViewById<EditText>(R.id.inputSubTopic)
        val btnCreate = sheetView.findViewById<Button>(R.id.btnCreate)

        input.requestFocus()

        btnCreate.setOnClickListener {
            val name = input.text.toString().trim()

            if (name.isBlank()) {
                input.error = "Required"
            } else {
                btnCreate.isEnabled = false
                viewModel.createSubTopic(token, apiKey, name, curriculumId)
                dialog.dismiss()
            }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun confirmDelete(subTopicId: Int) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete SubTopic")
            .setMessage("Are you sure you want to delete this subtopic?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteSubTopic(token, apiKey, subTopicId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditDialog(subTopic: SubTopic) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_subtopic, null)

        val input = view.findViewById<EditText>(R.id.inputSubTopic)
        val btn = view.findViewById<Button>(R.id.btnCreate)
        val sheetTitle = view.findViewById<TextView>(R.id.sheetTitle)

        // ✅ Prefill
        input.setText(subTopic.name)

        // Change button text
        sheetTitle.text = "Update Sub Topic"
        btn.text = "Update"

        btn.setOnClickListener {

            val updatedName = input.text.toString().trim()

            if (updatedName.isBlank()) {
                input.error = "Required"
                return@setOnClickListener
            }

            val curriculumId = arguments?.getInt("curriculumId") ?: 0

            viewModel.updateSubTopic(
                token,
                apiKey,
                subTopic.id,
                updatedName,
                curriculumId
            )

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

}