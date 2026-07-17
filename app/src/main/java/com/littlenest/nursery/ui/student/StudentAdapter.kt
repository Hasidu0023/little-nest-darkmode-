package com.littlenest.nursery.ui.student

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

class StudentAdapter(
    private var students: List<Student>,
    private var groups: List<Group>,
    private val uploadsBaseUrl: String,
    private val userRole: String,
    private val onItemClick: (Student) -> Unit,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        val textFullName: TextView = itemView.findViewById(R.id.textFullName)
        val textGroupName: TextView = itemView.findViewById(R.id.textGroupName)
        val textGender: TextView = itemView.findViewById(R.id.textGender)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditStudent)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletebtnEditStudent)
        //val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        // Find group name by ID
        //val groupName = groups.find { it.id == student.extraData.groupId }?.name ?: "Unknown Group"
        val groupName = student.extraData.groupName

        holder.textFullName.text = student.extraData.fullName
        holder.textGender.text = student.gender
        holder.textGroupName.text = "Group Name: $groupName"
        //holder.tvTitle.text = student.extraData.groupName

        val imageUrl = "${uploadsBaseUrl}${student.extraData.profilePicture}"

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(holder.imageProfile)

        holder.itemView.setOnClickListener { onItemClick(student) }

        // Hide edit & delete for teacher role
        if (userRole == "teacher") {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick(student) }
            holder.btnDelete.setOnClickListener { onDeleteClick(student) }
        }
    }

    override fun getItemCount() = students.size

    fun updateData(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }
    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}