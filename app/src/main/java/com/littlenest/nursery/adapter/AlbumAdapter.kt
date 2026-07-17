package com.littlenest.nursery.ui.family

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.littlenest.nursery.R
import com.littlenest.nursery.model.PostImage
import android.util.Log

class AlbumAdapter(
    private val context: Context,
    private val baseUrl: String,
    private var images: List<PostImage>,
    private val onImageClick: (position: Int) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagePost: ImageView = view.findViewById(R.id.imagePost)

        init {
            imagePost.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val item = images[position]
        //Log.d("ALBUM_URL", resolveImageUrl(item.imageUrl))
        Glide.with(context)
            .load(resolveImageUrl(item.imageUrl))
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imagePost)
    }

    override fun getItemCount(): Int = images.size

    fun updateData(newImages: List<PostImage>) {
        images = newImages
        notifyDataSetChanged()
    }

    /**
     * Helper to handle full and relative URLs
     */
    private fun resolveImageUrl(url: String): String {
        return when {
            url.startsWith("http") -> url
            url.startsWith("/uploads") -> baseUrl + url
            else -> "${baseUrl}/uploads/$url"
        }
    }
}
