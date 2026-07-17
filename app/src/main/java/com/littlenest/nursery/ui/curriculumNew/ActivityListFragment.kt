package com.littlenest.nursery.ui.curriculumNew

import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.*
import androidx.appcompat.app.AlertDialog

class ActivityListFragment :
    BaseFragment(R.layout.fragment_activity_list) {

    private val viewModel: ActivityViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ActivityAdapter

    private lateinit var token: String
    private lateinit var apiKey: String

    override fun setupUI(view: View) {

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        token = "Bearer " + (getToken() ?: "")
        apiKey = getApiKey()

        val subTopicId = arguments?.getInt("subTopicId") ?: 0

        setupRecycler()
        observeData()

        viewModel.fetchActivities(token, apiKey, subTopicId)

        val fab: FloatingActionButton = view.findViewById(R.id.addActivityFab)
        fab.setOnClickListener {
            showCreateActivitySheet(subTopicId)
        }
    }

    private fun setupRecycler() {
        adapter = ActivityAdapter(
            emptyList(),
            onItemClick = { activity ->
                //Toast.makeText(requireContext(), activity.name, Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { activity ->
                confirmDelete(activity.id)
            },
            onEditClick = { activity ->
                showEditActivitySheet(activity, activity.id)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.activities.observe(viewLifecycleOwner) {
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

                val subTopicId = arguments?.getInt("subTopicId") ?: 0
                viewModel.fetchActivities(token, apiKey, subTopicId)

                viewModel.createSuccess.value = false // 🔥 reset
            }
        }

        viewModel.deleteSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()

                val subTopicId = arguments?.getInt("subTopicId") ?: 0
                viewModel.fetchActivities(token, apiKey, subTopicId)

                viewModel.deleteSuccess.value = false
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Activity Updated!", Toast.LENGTH_SHORT).show()

                val subTopicId = arguments?.getInt("subTopicId") ?: 0
                viewModel.fetchActivities(token, apiKey, subTopicId)

                viewModel.updateSuccess.value = false
            }
        }
    }

    private fun showCreateActivitySheet(subTopicId: Int) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_activity, null)

        val nameInput = view.findViewById<EditText>(R.id.inputActivityName)
        val descInput = view.findViewById<EditText>(R.id.inputActivityDesc)
        val btn = view.findViewById<Button>(R.id.btnCreateActivity)

        btn.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val desc = descInput.text.toString().trim()

            if (name.isBlank()) {
                nameInput.error = "Required"
                return@setOnClickListener
            }

            viewModel.createActivity(token, apiKey, name, desc, subTopicId)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmDelete(activityId: Int) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(token, apiKey, activityId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditActivitySheet(activity: Activity, subTopicId: Int) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_activity, null)

        val nameInput = view.findViewById<EditText>(R.id.inputActivityName)
        val descInput = view.findViewById<EditText>(R.id.inputActivityDesc)
        val btn = view.findViewById<Button>(R.id.btnCreateActivity)
        val sheetTitle = view.findViewById<TextView>(R.id.sheetTitle)

        // ✅ Prefill
        nameInput.setText(activity.name)
        descInput.setText(activity.description)

        sheetTitle.text = "Update Activity"
        btn.text = "Update"

        btn.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val desc = descInput.text.toString().trim()

            if (name.isBlank()) {
                nameInput.error = "Required"
                return@setOnClickListener
            }

            viewModel.updateActivity(
                token,
                apiKey,
                activity.id,
                name,
                desc,
                subTopicId
            )

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}