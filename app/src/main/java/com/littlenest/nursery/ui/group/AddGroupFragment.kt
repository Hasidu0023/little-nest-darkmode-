package com.littlenest.nursery.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentAddGroupBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import androidx.navigation.fragment.findNavController

class AddGroupFragment : BaseFragment(R.layout.fragment_add_group) {

    private var _binding: FragmentAddGroupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        binding.btnSaveGroup.setOnClickListener {
            val name = binding.etGroupName.text.toString().trim()
            val description = binding.etGroupDescription.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Enter group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createGroup(token, apiKey, name, description)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.createdGroup.observe(viewLifecycleOwner) { group ->
            group?.let {
                Toast.makeText(requireContext(), "Group created: ${it.name}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // ✅ Proper back navigation
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
