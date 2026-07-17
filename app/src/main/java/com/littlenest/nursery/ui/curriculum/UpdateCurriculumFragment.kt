package com.littlenest.nursery.ui.curriculum

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentCurriculumAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.curriculum.CurriculumViewModel

class UpdateCurriculumFragment : BaseFragment(R.layout.fragment_curriculum_add) {

    private var _binding: FragmentCurriculumAddBinding? = null
    private val binding get() = _binding!!

    private val args: UpdateCurriculumFragmentArgs by navArgs()
    private val viewModel: CurriculumViewModel by viewModels()

    override fun setupUI(view: View) {
        _binding = FragmentCurriculumAddBinding.bind(view)

        // Pre-fill the form with the existing curriculum data
        args.curriculum?.let { curriculum ->
            binding.editMainTopic.setText(curriculum.mainTopic)
            binding.etStandard.setText(curriculum.standard)
            binding.editSubTopic.setText(curriculum.subTopics.joinToString(", "))
        }

        binding.btnAddCurriculum.text = "Update Curriculum"

        binding.btnAddCurriculum.setOnClickListener {
            updateCurriculum()
        }

        // Handle ViewModel Observers
        observeViewModel()
    }

    private fun updateCurriculum() {
        val token = getToken()
        val apiKey = getApiKey()

        if (token == null) {
            handleLogout()
            return
        }

        val mainTopic = binding.editMainTopic.text.toString().trim()
        val subTopics = binding.editSubTopic.text.toString().split(",").map { it.trim() }
        val standard = binding.etStandard.text.toString().trim()

        if (mainTopic.isEmpty() || standard.isEmpty()) {
            showToast("Please fill in all required fields.")
            return
        }

        val request = CurriculumRequest(
            mainTopic = mainTopic,
            subTopics = subTopics,
            standard = standard
        )

        viewModel.updateCurriculum(
            token = token,
            apiKey = apiKey,
            curriculumId = args.curriculum!!.id,
            request = request
        )
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnAddCurriculum.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.d("updateerror", "$error")
                showToast("Error: $it")
            }
        }

        viewModel.curriculums.observe(viewLifecycleOwner) {
            showToast("Curriculum updated successfully!")
            findNavController().popBackStack()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
