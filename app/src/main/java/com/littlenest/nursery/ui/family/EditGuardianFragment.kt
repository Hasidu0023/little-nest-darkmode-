package com.littlenest.nursery.ui.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentAddGuardianBinding
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.model.GuardianRequest
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.GuardianViewModel

class EditGuardianFragment : BaseFragment(R.layout.fragment_add_guardian) {

    private var _binding: FragmentAddGuardianBinding? = null
    private val binding get() = _binding!!
    private val guardianViewModel: GuardianViewModel by viewModels()

    private lateinit var guardian: Guardian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guardian = arguments?.getParcelable("guardian")
            ?: throw IllegalStateException("Guardian is required")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddGuardianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        prefillGuardianData()
        binding.btnSaveGuardian.text = "Update Guardian"

        binding.btnSaveGuardian.setOnClickListener {
            val updatedGuardian = GuardianRequest(
                name = binding.inputName.text.toString(),
                relation = binding.inputRelation.text.toString(),
                occupation = binding.inputOccupation.text.toString(),
                workPlace = binding.inputWorkplace.text.toString(),
                nativeLanguage = binding.inputLanguage.text.toString(),
                workPhone = binding.inputWorkPhone.text.toString(),
                homePhone = binding.inputHomePhone.text.toString(),
                mobilePhone = binding.inputMobilePhone.text.toString(),
                email = binding.inputEmail.text.toString(),
                pickupPermission = binding.checkboxPickupPermission.isChecked
            )

            // Check if there are any changes
            if (!hasChanges(updatedGuardian)) {
                Toast.makeText(requireContext(), "No changes detected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = getToken()?.let { "Bearer $it" } ?: return@setOnClickListener
            val apiKey = getApiKey()

            guardianViewModel.updateGuardian(guardian.id, updatedGuardian, token, apiKey)
        }

        observeViewModel()
    }

    private fun hasChanges(updatedGuardian: GuardianRequest): Boolean {
        return updatedGuardian.name != guardian.name ||
                updatedGuardian.relation != guardian.relation ||
                updatedGuardian.occupation != guardian.occupation ||
                updatedGuardian.workPlace != guardian.workPlace ||
                updatedGuardian.nativeLanguage != guardian.nativeLanguage ||
                updatedGuardian.workPhone != guardian.workPhone ||
                updatedGuardian.homePhone != guardian.homePhone ||
                updatedGuardian.mobilePhone != guardian.mobilePhone ||
                updatedGuardian.email != guardian.email ||
                updatedGuardian.pickupPermission != guardian.pickupPermission
    }

    private fun prefillGuardianData() {
        binding.inputRelation.setText(guardian.relation)
        binding.inputName.setText(guardian.name)
        binding.inputOccupation.setText(guardian.occupation ?: "")
        binding.inputWorkplace.setText(guardian.workPlace ?: "")
        binding.inputLanguage.setText(guardian.nativeLanguage ?: "")
        binding.inputWorkPhone.setText(guardian.workPhone ?: "")
        binding.inputHomePhone.setText(guardian.homePhone ?: "")
        binding.inputMobilePhone.setText(guardian.mobilePhone ?: "")
        binding.inputEmail.setText(guardian.email ?: "")
        binding.checkboxPickupPermission.isChecked = guardian.pickupPermission
    }

    private fun observeViewModel() {
        guardianViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                guardianViewModel.clearMessage()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
