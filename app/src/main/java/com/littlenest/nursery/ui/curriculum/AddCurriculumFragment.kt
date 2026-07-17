package com.littlenest.nursery.ui.curriculum

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentCurriculumAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.curriculum.CurriculumViewModel

class AddCurriculumFragment : BaseFragment(R.layout.fragment_curriculum_add) {

    private var _binding: FragmentCurriculumAddBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurriculumViewModel by viewModels()

    override fun setupUI(view: View) {
        _binding = FragmentCurriculumAddBinding.bind(view)

//        // Setup Spinner
//        val standards = listOf("Cambridge", "ESL", "SEB", "FES")
//        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, standards)
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinnerStandard.adapter = spinnerAdapter

        // Submit Button
        binding.btnAddCurriculum.setOnClickListener {
            val mainTopic = binding.editMainTopic.text.toString().trim()
            val subTopics = binding.editSubTopic.text.toString().trim().split(",").map { it.trim() }
            val standard = binding.etStandard.text.toString().trim()

            if (mainTopic.isEmpty() || subTopics.isEmpty()) {
                showToast("Please enter all fields")
                return@setOnClickListener
            }

            val request = CurriculumRequest(
                mainTopic = mainTopic,
                subTopics = subTopics,
                standard = standard
            )

            postCurriculum(request)
        }

        setupObservers()
    }

    private fun postCurriculum(request: CurriculumRequest) {
        val token = getToken()
        if (token.isNullOrEmpty()) {
            handleLogout()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        viewModel.addCurriculum("Bearer $token", getApiKey(), request)
    }

    private fun setupObservers() {
        viewModel.response.observe(viewLifecycleOwner) { response ->
            binding.progressBar.visibility = View.GONE
            if (response != null) {
                showToast("Curriculum added successfully")
                findNavController().navigate(R.id.action_addCurriculumFragment_to_curriculumListFragment)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            binding.progressBar.visibility = View.GONE
            showToast("Error: $errorMsg")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
