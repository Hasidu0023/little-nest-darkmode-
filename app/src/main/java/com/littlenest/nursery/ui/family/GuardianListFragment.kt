package com.littlenest.nursery.ui.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyGuardianListBinding
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.GuardianViewModel
import androidx.navigation.fragment.findNavController

class GuardianListFragment : BaseFragment(R.layout.fragment_family_guardian_list) {

    private var _binding: FragmentFamilyGuardianListBinding? = null
    private val binding get() = _binding!!

    private val guardianViewModel: GuardianViewModel by viewModels()
    private lateinit var guardianAdapter: GuardianAdapter

    private var studentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studentId = getProfileId()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyGuardianListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        // Setup RecyclerView
        guardianAdapter = GuardianAdapter(
            mutableListOf(),
            onEditClick = { guardian -> editGuardian(guardian) },
            onDeleteClick = { guardian -> deleteGuardian(guardian) }
        )
        binding.recyclerGuardians.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerGuardians.adapter = guardianAdapter

        // Add guardian button
        binding.fabAddGuardian.setOnClickListener { addGuardian() }

        // Observe LiveData
        observeViewModel()

        // ✅ Auto-fetch guardians using token and apiKey from BaseFragment
        val token = getToken()?.let { "Bearer $it" } ?: return
        val apiKey = getApiKey()
        guardianViewModel.fetchGuardians(token, apiKey)
    }

    private fun observeViewModel() {
        guardianViewModel.guardians.observe(viewLifecycleOwner) { guardians ->
            guardianAdapter.updateList(guardians)
        }

        guardianViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarGuardians.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        guardianViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                // Optional: show toast
                // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                guardianViewModel.clearMessage()
            }
        }
    }

    private fun addGuardian() {
        findNavController().navigate(R.id.action_guardianListFragment_to_addGuardianFragment)
    }

    private fun editGuardian(guardian: Guardian) {
        // Navigate to EditGuardianFragment and pass the guardian object
        val bundle = Bundle().apply { putParcelable("guardian", guardian) }
        findNavController().navigate(R.id.action_guardianListFragment_to_editGuardianFragment, bundle)
    }

    private fun deleteGuardian(guardian: Guardian) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Guardian")
            .setMessage("Are you sure you want to delete ${guardian.name}?")
            .setPositiveButton("Yes") { _, _ ->
                val token = getToken()?.let { "Bearer $it" } ?: return@setPositiveButton
                val apiKey = getApiKey()

                guardianViewModel.deleteGuardian(guardian.id, token, apiKey)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
