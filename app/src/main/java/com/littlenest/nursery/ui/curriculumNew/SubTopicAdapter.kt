package com.littlenest.nursery.ui.curriculumNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R


class SubTopicAdapter(
    private var list: List<SubTopic>,
    private val onItemClick: (SubTopic) -> Unit,
    private val onDeleteClick: (SubTopic) -> Unit,
    private val onEditClick: (SubTopic) -> Unit
) : RecyclerView.Adapter<SubTopicAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtopic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.name
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
        holder.btnEdit.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<SubTopic>) {
        list = newList
        notifyDataSetChanged()
    }
}