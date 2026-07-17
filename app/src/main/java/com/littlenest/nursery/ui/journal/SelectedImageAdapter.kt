package com.littlenest.nursery.ui.journal
import android.net.Uri
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R


//journal add form - add and remove images and show thumb
class SelectedImageAdapter(
    private val images: MutableList<Uri>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imagePreview)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = images[position]

        Glide.with(holder.image.context)
            .load(uri)
            .into(holder.image)

        holder.btnRemove.setOnClickListener {
            onRemove(position)
        }
    }
}