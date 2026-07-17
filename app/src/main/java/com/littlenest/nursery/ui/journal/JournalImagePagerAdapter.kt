package com.littlenest.nursery.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R

// 🔥 UPDATED: added onClick
class JournalImagePagerAdapter(
    private val images: List<String>,
    private val baseUrl: String,
    private val onClick: (Int) -> Unit // 🔥 NEW
) : RecyclerView.Adapter<JournalImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val imageUrl = "$baseUrl/${images[position]}"

        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.avatar_placeholder)
            .into(holder.imageView)

        // 🔥 NEW: click listener
        holder.imageView.setOnClickListener {
            onClick(position)
        }
    }

    override fun getItemCount(): Int = images.size
}
