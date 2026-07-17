package com.littlenest.nursery.ui.curriculumNew

import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.AlertDialog

class CurriculumNewListFragment :
    BaseFragment(R.layout.fragment_curriculumnew_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: CurriculumNewAdapter

    private val viewModel: CurriculumNewViewModel by viewModels()

    private var standardId: Int = -1
    private lateinit var token: String
    private lateinit var apiKey: String

    override fun setupUI(view: View) {

        //standardId = arguments?.getInt("standardId") ?: -1
        standardId = 1

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecycler()
        observeData()

        // ✅ FIXED: no shadowing
        token = "Bearer ${getToken()}"
        apiKey = getApiKey()

        viewModel.fetchCurriculums(token, apiKey, standardId)

        val fab: FloatingActionButton = view.findViewById(R.id.addCurriculumNewFloatingIcon)
        fab.setOnClickListener {
            showCreateCurriculumSheet()
        }
    }

    private fun setupRecycler() {
        adapter = CurriculumNewAdapter(emptyList(),
            onItemClick = { curriculum ->
                val action = CurriculumNewListFragmentDirections
                    .actionCurriculumListFragmentToSubTopicListFragment(
                        curriculum.id
                    )
                findNavController().navigate(action)
            },
            onDeleteClick = { curriculum ->
                confirmDelete(curriculum.id)
            },
            onEditClick = { curriculum ->
                showEditCurriculumSheet(curriculum)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeData() {

        viewModel.curriculums.observe(viewLifecycleOwner) {
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

        // ✅ SUCCESS HANDLING + REFRESH
        viewModel.createSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Curriculum Created!", Toast.LENGTH_SHORT).show()

                viewModel.fetchCurriculums(token, apiKey, standardId)

                viewModel.createSuccess.value = false
            }
        }

        viewModel.deleteCurriculumSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Curriculum Deleted!", Toast.LENGTH_SHORT).show()

                viewModel.fetchCurriculums(token, apiKey, standardId)

                viewModel.deleteCurriculumSuccess.value = false
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()

                viewModel.fetchCurriculums(token, apiKey, standardId)

                viewModel.updateSuccess.value = false
            }
        }
    }

    private fun showCreateCurriculumSheet() {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_curriculum, null)

        val topicInput = view.findViewById<EditText>(R.id.inputMainTopic)
        val btn = view.findViewById<Button>(R.id.btnCreateCurriculum)

        btn.setOnClickListener {

            val topic = topicInput.text.toString().trim()

            if (topic.isBlank()) {
                topicInput.error = "Required"
                return@setOnClickListener
            }

            viewModel.createCurriculum(
                token,
                apiKey,
                topic,
                standardId
            )
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmDelete(curriculumId: Int) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Curriculum")
            .setMessage("Are you sure you want to delete this curriculum?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteCurriculumNew(token, apiKey, curriculumId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditCurriculumSheet(curriculum: Curriculum) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_curriculum, null)

        val input = view.findViewById<EditText>(R.id.inputMainTopic)
        val btn = view.findViewById<Button>(R.id.btnCreateCurriculum)
        val sheetTitle = view.findViewById<TextView>(R.id.sheetTitle)

        // prefill
        input.setText(curriculum.mainTopic)

        sheetTitle.text = "Update Curriculum"
        btn.text = "Update"


        btn.setOnClickListener {

            val updatedText = input.text.toString().trim()

            if (updatedText.isBlank()) {
                input.error = "Required"
                return@setOnClickListener
            }

            viewModel.updateCurriculum(
                token,
                apiKey,
                curriculum.id,
                updatedText,
                standardId
            )

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}