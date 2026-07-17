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
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.GuardianViewModel
import com.littlenest.nursery.model.GuardianRequest

class AddGuardianFragment : BaseFragment(R.layout.fragment_add_guardian) {

    private var _binding: FragmentAddGuardianBinding? = null
    private val binding get() = _binding!!
    private val guardianViewModel: GuardianViewModel by viewModels()

    private var studentId: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddGuardianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studentId = getProfileId()
    }

    override fun setupUI(view: View) {
//        studentId = arguments?.getInt("studentId") ?: 0
        binding.btnSaveGuardian.setOnClickListener {
            val guardian = GuardianRequest(
                relation = binding.inputRelation.text.toString(),
                name = binding.inputName.text.toString(),
                occupation = binding.inputOccupation.text.toString().takeIf { it.isNotEmpty() },
                workPlace = binding.inputWorkplace.text.toString().takeIf { it.isNotEmpty() },
                nativeLanguage = binding.inputLanguage.text.toString().takeIf { it.isNotEmpty() },
                workPhone = binding.inputWorkPhone.text.toString().takeIf { it.isNotEmpty() },
                homePhone = binding.inputHomePhone.text.toString().takeIf { it.isNotEmpty() },
                mobilePhone = binding.inputMobilePhone.text.toString().takeIf { it.isNotEmpty() },
                email = binding.inputEmail.text.toString().ifEmpty { "" },
                pickupPermission = binding.checkboxPickupPermission.isChecked
            )


            val token = getToken()?.let { "Bearer $it" } ?: return@setOnClickListener
            val apiKey = getApiKey()

            guardianViewModel.addGuardian(studentId, guardian, token, apiKey)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        guardianViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                guardianViewModel.clearMessage()
                findNavController().popBackStack() // go back to guardian list
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
