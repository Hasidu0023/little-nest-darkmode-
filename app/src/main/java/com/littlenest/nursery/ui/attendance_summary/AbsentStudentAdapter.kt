package com.littlenest.nursery.ui.attendance_summary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R

class AbsentStudentAdapter (
    private val uploadsBaseUrl: String
) : ListAdapter<StudentAttendance, AbsentStudentAdapter.VH>(Diff()) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val image: ImageView = view.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absent_student, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val student = getItem(position)

        holder.name.text = student.name ?: "Unknown"
        val imageUrl = "${uploadsBaseUrl}${student.profilePicture}"
        Glide.with(holder.itemView)
            .load(imageUrl)
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(holder.image)
    }

    class Diff : DiffUtil.ItemCallback<StudentAttendance>() {
        override fun areItemsTheSame(a: StudentAttendance, b: StudentAttendance) =
            a.id == b.id

        override fun areContentsTheSame(a: StudentAttendance, b: StudentAttendance) =
            a == b
    }
}