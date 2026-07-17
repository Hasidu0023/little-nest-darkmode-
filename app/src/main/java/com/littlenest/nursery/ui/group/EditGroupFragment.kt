package com.littlenest.nursery.ui.group

import android.view.View
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.group.GroupViewModel

class EditGroupFragment : BaseFragment(R.layout.fragment_add_group) {

    private val viewModel: GroupViewModel by viewModels()
    private var groupId: Int = 0
    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        etName = view.findViewById(R.id.etGroupName)
        etDescription = view.findViewById(R.id.etGroupDescription)
        btnSave = view.findViewById(R.id.btnSaveGroup)

        btnSave.text = "Update Group"

        // ✅ Get arguments from Safe Args
        val args = EditGroupFragmentArgs.fromBundle(requireArguments())
        groupId = args.groupId

        // ✅ Prefill fields
        etName.setText(args.groupName)
        etDescription.setText(args.groupDescription)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Group name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateGroup(token, apiKey, groupId, name, description)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.updatedGroup.observe(viewLifecycleOwner) { group ->
            group?.let {
                Toast.makeText(requireContext(), "Group updated: ${it.name}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
