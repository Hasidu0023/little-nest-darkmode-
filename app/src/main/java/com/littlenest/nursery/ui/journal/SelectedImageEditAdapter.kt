package com.littlenest.nursery.ui.journal

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R

//journal edit form - add and remove images and show thumb
class SelectedImageEditAdapter(
    private val images: MutableList<Any>,   // ✅ supports Uri + String
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImageEditAdapter.ImageViewHolder>() { // ✅ FIXED

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imagePreview)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder { // ✅ FIXED
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) { // ✅ FIXED

        val item = images[position]

        when (item) {
            is Uri -> {
                Glide.with(holder.image.context)
                    .load(item)
                    .centerCrop()
                    .into(holder.image)
            }

            is String -> {
                Glide.with(holder.image.context)
                    .load(item)
                    .centerCrop()
                    .into(holder.image)
            }
        }

        holder.btnRemove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onRemove(pos)
            }
        }
    }
}