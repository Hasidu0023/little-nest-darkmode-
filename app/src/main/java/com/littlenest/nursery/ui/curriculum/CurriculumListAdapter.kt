package com.littlenest.nursery.ui.curriculum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.databinding.ItemCurriculumBinding

class CurriculumListAdapter(
    private var curriculumList: List<CurriculumResponse>,
    private val onEditClick: (CurriculumResponse) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CurriculumListAdapter.CurriculumViewHolder>() {

    inner class CurriculumViewHolder(val binding: ItemCurriculumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(curriculum: CurriculumResponse) {
            binding.tvMainTopic.text = curriculum.mainTopic
            binding.tvStandard.text = curriculum.standard
            binding.tvSubTopics.text = curriculum.subTopics.joinToString(", ")

            binding.btnEditCurriculum.setOnClickListener {
                onEditClick(curriculum)
            }
            binding.btnDeleteCurriculum.setOnClickListener {
                onDeleteClick(curriculum.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurriculumViewHolder {
        val binding = ItemCurriculumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurriculumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurriculumViewHolder, position: Int) {
        holder.bind(curriculumList[position])
    }

    override fun getItemCount() = curriculumList.size

    fun updateData(newList: List<CurriculumResponse>) {
        curriculumList = newList
        notifyDataSetChanged()
    }
}


//package com.example.nurseryapp.ui.curriculum
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.nurseryapp.R
//import android.widget.ImageButton
//import com.example.nurseryapp.databinding.ItemCurriculumBinding
//
//class CurriculumListAdapter(
//    private var items: List<CurriculumResponse> = listOf(),
//    private val onDeleteClick: (Int) -> Unit
//) : RecyclerView.Adapter<CurriculumListAdapter.ItemViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
//        val binding = ItemCurriculumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ItemViewHolder(binding)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        val item = items[position]
//        holder.bind(item)
//        holder.itemView.findViewById<ImageButton>(R.id.btnDeleteCurriculum).setOnClickListener {
//            onDeleteClick(item.id)
//        }
//    }
//
//    class ItemViewHolder(private val binding: ItemCurriculumBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: CurriculumResponse) {
//            binding.tvStandard.text = item.standard
//            binding.tvMainTopic.text = item.mainTopic
//            binding.tvSubTopics.text = item.subTopics.joinToString(", ")
//        }
//    }
//
//    fun updateData(newItems: List<CurriculumResponse>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
//}
