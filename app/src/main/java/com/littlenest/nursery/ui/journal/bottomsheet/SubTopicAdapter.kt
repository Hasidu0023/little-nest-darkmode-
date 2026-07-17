package com.littlenest.nursery.ui.journal.bottomsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.databinding.ItemSimpleRadioBinding
import com.littlenest.nursery.ui.curriculumNew.SubTopicDetail

class SubTopicAdapter(
    private val items: List<SubTopicDetail>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SubTopicAdapter.VH>() {

    private var selectedIndex = -1

    inner class VH(val binding: ItemSimpleRadioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSimpleRadioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = items[position]

        holder.binding.radioText.text = item.name
        holder.binding.radio.isChecked = position == selectedIndex

        holder.binding.root.setOnClickListener {
            selectedIndex = position
            onClick(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = items.size
}