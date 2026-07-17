package com.littlenest.nursery.ui.curriculumNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import android.widget.ImageButton

class ActivityAdapter(
    private var list: List<Activity>,
    private val onItemClick: (Activity) -> Unit,
    private val onDeleteClick: (Activity) -> Unit,
    private val onEditClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val description: TextView = view.findViewById(R.id.description)

        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.name
        holder.description.text = item.description ?: ""

//        holder.itemView.setOnClickListener {
//            onItemClick(item)
//        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
        holder.btnEdit.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Activity>) {
        list = newList
        notifyDataSetChanged()
    }
}