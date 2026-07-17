package com.littlenest.nursery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.databinding.ItemSlideBinding
import com.littlenest.nursery.model.PostImage
import com.littlenest.nursery.utils.ImageUtils
import android.util.Log

class SlideshowAdapter(
    private val images: List<PostImage>,
    private val uploadsBaseUrl: String
) : RecyclerView.Adapter<SlideshowAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(val binding: ItemSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val image = images[position]
       //Log.d("slideshow adapter", "${resolveImageUrl(image.imageUrl)}")
        Glide.with(holder.itemView.context)
            .load(ImageUtils.resolveImageUrl(uploadsBaseUrl, image.imageUrl))
            .placeholder(android.R.color.darker_gray)
            .into(holder.binding.imageSlide)
    }

    override fun getItemCount(): Int = images.size
}
