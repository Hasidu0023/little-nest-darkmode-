package com.littlenest.nursery.ui.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.databinding.ItemEventSummaryBinding

class EventSummaryAdapter(
    private var summaryList: List<GroupSummary>
) : RecyclerView.Adapter<EventSummaryAdapter.SummaryViewHolder>() {

    inner class SummaryViewHolder(val binding: ItemEventSummaryBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroupSummary) {
            binding.textGroupName.text = "Group: ${item.groupName}"
            binding.textAccepted.text = "Accepted: ${item.accepted}"
            binding.textDeclined.text = "Declined: ${item.declined}"
            binding.textPending.text = "Pending: ${item.pending}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemEventSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(summaryList[position])
    }

    override fun getItemCount(): Int = summaryList.size

    fun submitList(newList: List<GroupSummary>) {
        summaryList = newList
        notifyDataSetChanged()
    }
}
