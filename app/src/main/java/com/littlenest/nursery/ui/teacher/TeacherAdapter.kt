package com.littlenest.nursery.ui.teacher

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.group.Group

class TeacherAdapter(
    private var teachers: List<Teacher>,
    private var groups: List<Group>,
    private val baseUrl: String,
    private val onItemClick: (Teacher) -> Unit,
    private val onEditClick: (Teacher) -> Unit,
    private val onDeleteClick: (Teacher) -> Unit
) : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    inner class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        val textFullName: TextView = itemView.findViewById(R.id.textFullName)
        val textGroupName: TextView = itemView.findViewById(R.id.textGroupName)
        val textGender: TextView = itemView.findViewById(R.id.textGender)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditTeacher)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteTeacher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teachers[position]

        val groupName = teacher.extraData.assignedGroups.joinToString(", ")
        // Map assigned group IDs to names
//        val groupNames = teacher.extraData.assignedGroups.mapNotNull { groupId ->
//            groups.find { it.id == groupId }?.name
//        }
//        val groupNameText = if (groupNames.isNotEmpty()) groupNames.joinToString(", ") else "No groups"


        holder.textFullName.text = teacher.extraData.name
        holder.textGender.text = teacher.gender
        holder.textGroupName.text = "Groups: $groupName"

        //val imageUrl = "${baseUrl}${teacher.extraData.profilePicture}"
        val profilePicture = teacher.extraData.profilePicture
        val imageUrl = if (!profilePicture.isNullOrEmpty()) {
            "$baseUrl$profilePicture"
        } else {
            null
        }
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(holder.imageProfile)

        holder.itemView.setOnClickListener { onItemClick(teacher) }
        holder.btnEdit.setOnClickListener { onEditClick(teacher) }
        holder.btnDelete.setOnClickListener { onDeleteClick(teacher) }
    }

    override fun getItemCount() = teachers.size

    fun updateData(newTeachers: List<Teacher>) {
        teachers = newTeachers
        notifyDataSetChanged()
    }
    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}