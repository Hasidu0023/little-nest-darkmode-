package com.littlenest.nursery.ui.student

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentStudentDetailBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import androidx.lifecycle.ViewModelProvider

class StudentDetailFragment : BaseFragment(R.layout.fragment_student_detail) {

    private var _binding: FragmentStudentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: StudentDetailFragmentArgs by navArgs()
    private val viewModel: StudentViewModel by viewModels()
    private lateinit var groupViewModel: GroupViewModel

    private var groupList: List<Group> = emptyList()


    override fun setupUI(view: View) {
        _binding = FragmentStudentDetailBinding.bind(view)
        val student = args.student
        bindStudent(student)

        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // Initialize and fetch groups
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        // Observe groups and bind student after groups are ready
        observeGroups(student)

        observeViewModel()
    }

    private fun observeGroups(student: Student) {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupList = groups

            // ✅ Now we have the group list, safe to bind student details
            bindStudent(student)
        }

        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun observeViewModel() {
        viewModel.studentDetail.observe(viewLifecycleOwner) { updatedStudent ->
            updatedStudent?.let { bindStudent(it) }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun bindStudent(student: Student) {
        val extra = student.extraData
        binding.apply {
            val groupName = groupList.find { it.id == extra.groupId }?.name ?: "Unknown Group"
            //textGroupName.text = groupName
            textGroupName.text = extra.groupName

            textFullName.text = extra.fullName
            //textGroupName.text = extra.groupId?.toString() ?: "N/A"
            textNickname.text = extra.nickname
            //textNickname.text = getString(R.string.label_nickname, extra.nickname)
            textGender.text = student.gender
            textDateOfBirth.text = extra.dateOfBirth
            textDropOff.text = extra.dropOffTime ?: ""
            textPickup.text = extra.pickupTime ?: ""
            textNativeLanguage.text = extra.nativeLanguage
            textAddress.text = extra.address
            textCity.text =  extra.city ?: ""
            textAllergies.text = extra.allergies
            textComment.text = extra.comment ?: "None"
            textPhotoConsent.text = if (extra.photoConsent == true) "✓" else "✘"

            val imageUrl = getBaseUrl() +  extra.profilePicture
            if (!extra.profilePicture.isNullOrEmpty()) {
                Glide.with(this@StudentDetailFragment)
                    .load(imageUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(imageProfile)
            } else {
                imageProfile.setImageResource(R.drawable.avatar_placeholder)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}