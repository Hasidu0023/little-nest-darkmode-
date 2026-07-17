//package com.example.nurseryapp.ui.group
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.ImageButton
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.nurseryapp.R
//import com.example.nurseryapp.ui.student.Student
//
//class GroupStudentListAdapter(
//    private val baseUrl: String,
//    private val onItemClick: (Student) -> Unit
//) : ListAdapter<Student, GroupStudentListAdapter.VH>(Diff()) {
//
//    class VH(view: View) : RecyclerView.ViewHolder(view) {
//        val profile: ImageView = view.findViewById(R.id.imageProfile)
//        val fullName: TextView = view.findViewById(R.id.textFullName)
//        val gender: TextView = view.findViewById(R.id.textGender)
//        val groupName: TextView = view.findViewById(R.id.textGroupName)
//
//        val btnEdit: ImageButton = view.findViewById(R.id.btnEditStudent)
//        val btnDelete: ImageButton = view.findViewById(R.id.btnDeletebtnEditStudent)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_student, parent, false)
//        return VH(view)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        val student = getItem(position)
//
//        holder.fullName.text =
//            student.extraData.fullName ?: student.username
//            holder.gender.text = student.gender
//            holder.groupName.text = student.extraData.groupName
//
//        Glide.with(holder.itemView)
//            .load(student.extraData.profilePicture?.let { baseUrl + it })
//            .placeholder(R.drawable.avatar_placeholder)
//            .error(R.drawable.avatar_placeholder)
//            .circleCrop()
//            .into(holder.profile)
//
//        // Optional: hide edit/delete in group context
//        holder.btnEdit.visibility = View.GONE
//        holder.btnDelete.visibility = View.GONE
//
//        holder.itemView.setOnClickListener {
//            onItemClick(student)
//        }
//    }
//
//    class Diff : DiffUtil.ItemCallback<Student>() {
//        override fun areItemsTheSame(
//            oldItem: Student,
//            newItem: Student
//        ): Boolean = oldItem.studentId == newItem.studentId
//
//        override fun areContentsTheSame(
//            oldItem: Student,
//            newItem: Student
//        ): Boolean = oldItem == newItem
//    }
//}
