package com.littlenest.nursery.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R

class GroupAdapter(
    private var groups: List<Group>,
    private val userRole: String,
    private val onItemClick: (Group) -> Unit,
    private val onEditClick: (Group) -> Unit,
    private val onDeleteClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvGroupName)
        val description: TextView = view.findViewById(R.id.tvGroupDescription)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditGroup)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.name.text = group.name
        holder.description.text = group.description

//        holder.btnEdit.setOnClickListener { onEditClick(group) }
//        holder.btnDelete.setOnClickListener { onDeleteClick(group) }

        holder.itemView.setOnClickListener {
            onItemClick(group)
        }

        if (userRole == "teacher") {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick(group) }
            holder.btnDelete.setOnClickListener { onDeleteClick(group) }
        }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
