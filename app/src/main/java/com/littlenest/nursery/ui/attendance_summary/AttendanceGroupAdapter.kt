package com.littlenest.nursery.ui.attendance_summary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.group.Group

class AttendanceGroupAdapter(
    private var groups: List<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<AttendanceGroupAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvGroupName)
        val description: TextView = view.findViewById(R.id.tvGroupDescription)
        init {
            view.setOnClickListener {
                onClick(groups[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_simple, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = groups.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = groups[position].name
        holder.description.text = groups[position].description
    }

    fun updateData(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
