//package com.example.nurseryapp.ui.group
//
//import android.os.Bundle
//import android.view.View
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.nurseryapp.R
//import com.example.nurseryapp.databinding.FragmentGroupStudentListBinding
//import com.example.nurseryapp.ui.common.BaseFragment
//import com.example.nurseryapp.ui.student.StudentAdapter
//import com.example.nurseryapp.ui.student.StudentsFragmentDirections
//import com.example.nurseryapp.viewmodel.group.GroupViewModel
//
//class GroupStudentListFragment :
//    BaseFragment(R.layout.fragment_group_student_list) {
//
//    private lateinit var binding: FragmentGroupStudentListBinding
//    private val viewModel: GroupViewModel by viewModels()
//    private lateinit var adapter: GroupStudentListAdapter
//
//    override fun setupUI(view: View) {
//        binding = FragmentGroupStudentListBinding.bind(view)
//
//        //adapter = GroupStudentListAdapter(uploadsBaseUrl())
//
//        adapter = GroupStudentListAdapter(
//            uploadsBaseUrl()
//        ) { clickedStudent ->
//            val action =
//                GroupStudentListFragmentDirections
//                    .actionGroupStudentListFragmentToStudentDetailFragment(
//                        clickedStudent
//                    )
//            findNavController().navigate(action)
//        }
//
//        binding.recyclerViewStudents.layoutManager =
//            LinearLayoutManager(requireContext())
//        binding.recyclerViewStudents.adapter = adapter
//
//        val args = GroupStudentListFragmentArgs.fromBundle(requireArguments())
//        binding.tvTitle.text = args.groupName.uppercase()
//
//        loadStudents(args.groupId)
//        observeViewModel()
//    }
//
//    private fun loadStudents(groupId: Int) {
//        val token = getToken() ?: return
//        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }
//
//        viewModel.fetchStudentsByGroup(token, apiKey, groupId)
//    }
//
//    private fun observeViewModel() {
//        viewModel.students.observe(viewLifecycleOwner) { students ->
//            adapter.submitList(students)
//            binding.tvEmpty.visibility =
//                if (students.isEmpty()) View.VISIBLE else View.GONE
//        }
//
//        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility =
//                if (isLoading) View.VISIBLE else View.GONE
//        }
//
//        viewModel.error.observe(viewLifecycleOwner) { error ->
//            error?.let {
//                binding.tvEmpty.text = it
//                binding.tvEmpty.visibility = View.VISIBLE
//            }
//        }
//    }
//}
