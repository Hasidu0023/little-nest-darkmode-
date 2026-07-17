package com.littlenest.nursery.ui.curriculumNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import android.widget.ImageButton

class CurriculumNewAdapter(
    private var list: List<Curriculum>,
    private val onItemClick: (Curriculum) -> Unit,
    private val onDeleteClick: (Curriculum) -> Unit,
    private val onEditClick: (Curriculum) -> Unit
) : RecyclerView.Adapter<CurriculumNewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curriculumnew, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.mainTopic

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
        holder.btnEdit.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Curriculum>) {
        list = newList
        notifyDataSetChanged()
    }
}