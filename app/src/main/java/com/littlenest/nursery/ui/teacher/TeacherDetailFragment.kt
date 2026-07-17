package com.littlenest.nursery.ui.teacher

import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentTeacherDetailBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import androidx.lifecycle.ViewModelProvider

class TeacherDetailFragment : BaseFragment(R.layout.fragment_teacher_detail) {

    private var _binding: FragmentTeacherDetailBinding? = null
    private val binding get() = _binding!!

    private val args: TeacherDetailFragmentArgs by navArgs()
    private lateinit var groupViewModel: GroupViewModel
    private var groupList: List<Group> = emptyList()

    override fun setupUI(view: View) {
        _binding = FragmentTeacherDetailBinding.bind(view)
        val teacher = args.teacher

        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        // Initialize GroupViewModel
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)

        // Observe and bind
        observeGroups(teacher)
    }

    private fun observeGroups(teacher: Teacher) {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupList = groups
            bindTeacher(teacher)
        }

        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        groupViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.root.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun bindTeacher(teacher: Teacher) {
        val extra = teacher.extraData
        binding.apply {
            tvName.text = extra.name
            tvUsername.text = teacher.username
            tvGender.text = teacher.gender

            // Nursery name (if available)
            tvNursery.text = extra.nurseryName ?: "Unknown Nursery"

            val assignedGroups = if (!extra.assignedGroups.isNullOrEmpty()) {
                extra.assignedGroups.joinToString(", ")
            } else {
                "No Groups Assigned"
            }
            tvGroups.text = assignedGroups

            // Profile Picture
            val imageUrl = getBaseUrl() + extra.profilePicture
            if (!extra.profilePicture.isNullOrEmpty()) {
                Glide.with(this@TeacherDetailFragment)
                    .load(imageUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(ivProfilePicture)
            } else {
                ivProfilePicture.setImageResource(R.drawable.avatar_placeholder)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//package com.example.nurseryapp.ui.teacher
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.bumptech.glide.Glide
//import com.example.nurseryapp.R
//import com.example.nurseryapp.databinding.FragmentTeacherDetailBinding
//
//class TeacherDetailFragment : Fragment() {
//
//    private var _binding: FragmentTeacherDetailBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTeacherDetailBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Get passed teacher using Safe Args
//        val teacher = TeacherDetailFragmentArgs.fromBundle(requireArguments()).teacher
//
//        // Bind data
//        binding.tvName.text = teacher.extraData.name
//        binding.tvUsername.text = teacher.username
//        binding.tvGender.text = teacher.gender.capitalize()
//        binding.tvNursery.text = teacher.extraData.nurseryName
//        binding.tvGroups.text = teacher.extraData.assignedGroups.joinToString(", ")
//
//        if (!teacher.extraData.profilePicture.isNullOrEmpty()) {
//            Glide.with(this)
//                .load("http://localhost:3000/uploads/${teacher.extraData.profilePicture}")
//                .into(binding.ivProfilePicture)
//        } else {
//            binding.ivProfilePicture.setImageResource(R.drawable.ic_baseline_person_add_24)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
