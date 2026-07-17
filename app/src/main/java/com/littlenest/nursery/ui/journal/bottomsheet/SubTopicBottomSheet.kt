package com.littlenest.nursery.ui.journal.bottomsheet

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.littlenest.nursery.databinding.BottomsheetSubtopicBinding
import com.littlenest.nursery.ui.curriculumNew.SubTopicDetail

class SubTopicBottomSheet(
    private val subTopics: List<SubTopicDetail>,
    private val onSelected: (SubTopicDetail) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetSubtopicBinding? = null
    private val binding get() = _binding!!

    private var selectedIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetSubtopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = SubTopicAdapter(subTopics) { index ->
            selectedIndex = index
        }

        binding.recyclerSubtopics.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSubtopics.adapter = adapter

        binding.btnNext.setOnClickListener {
            if (selectedIndex == -1) return@setOnClickListener

            onSelected(subTopics[selectedIndex])
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}