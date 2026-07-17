package com.littlenest.nursery.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.databinding.ItemEventBinding

/**
 * Adapter for displaying a list of events with a tabbed structure.
 * Note: The visual date separator logic is typically handled in the activity/fragment
 * using DiffUtil or a custom layout manager, but basic setup is provided here.
 */
class EventAdapter(
    private var events: List<Event>,
    private val userRole: String,
    private val onItemClick: (Event) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
    ) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event, position: Int) {
            binding.textEventName.text = event.eventName
            binding.textEventTime.text = "${event.starts} - ${event.ends}"

            // Clicking the item = open details
            itemView.setOnClickListener { onItemClick(event) }

            // Show edit/delete only if they are admin or teacher
            //val isPast = isPastEvent(event)
            if (userRole == "admin") {
                binding.iconEdit.visibility = View.VISIBLE
                binding.iconDelete.visibility = View.VISIBLE

                binding.iconEdit.setOnClickListener { onEditClick(event) }
                binding.iconDelete.setOnClickListener { onDeleteClick(event) }

            }  else {
                binding.iconEdit.visibility = View.GONE
                binding.iconDelete.visibility = View.GONE
            }

            // Simplified Participant Badge: Only showing one badge for demonstration
            // In a real app, you would dynamically add TextViews to layoutParticipants based on a list of attendees.
            // Example:
            // binding.textParticipantBadge.text = event.participants.firstOrNull()?.initial ?: "P"

            // Logic to display the Date Separator (e.g., "05-05-2020")
            // This is complex and usually requires a grouping mechanism outside the adapter,
            // but we'll show a basic logic check for demonstration.
            if (position == 0 || events[position].date != events[position - 1].date) {
                binding.textDateSeparator.text = event.date
                binding.textDateSeparator.visibility = View.VISIBLE
            } else {
                binding.textDateSeparator.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position], position)
    }

    override fun getItemCount(): Int = events.size

    fun submitList(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

}