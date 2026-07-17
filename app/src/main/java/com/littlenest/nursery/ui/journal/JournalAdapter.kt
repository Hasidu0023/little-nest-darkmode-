package com.littlenest.nursery.ui.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

import android.os.Bundle
import androidx.navigation.Navigation


class JournalAdapter(
    private var posts: List<JournalPost> = listOf(),
    private val baseUrl: String,
    private val userRole: String,
    private val onDeleteClick: (JournalPost) -> Unit,
    private val onEditClick: (JournalPost) -> Unit,
    private val onImageClick: (post: JournalPost, position: Int) -> Unit // ✅ NEW
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    // 🔥 NEW: Track expanded items by position
    private val expandedPositions = mutableSetOf<Int>()

    inner class JournalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descriptionText: TextView = view.findViewById(R.id.textDescription)
        val curriculumText: TextView = view.findViewById(R.id.textCurriculumTopic)
        val imageView: ImageView = view.findViewById(R.id.imagePost)
        val timestampText: TextView = view.findViewById(R.id.textTimestamp)
        val iconDelete: ImageView = view.findViewById(R.id.iconDelete)
        val iconEdit: ImageView = view.findViewById(R.id.iconEdit)

        // 🔥 NEW: "See more / See less"
        val textToggle: TextView = view.findViewById(R.id.textToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_post, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val post = posts[position]

        holder.descriptionText.text = post.description
        //holder.curriculumText.text =  "No Curriculum"
        holder.timestampText.text = formatDate(post.createdAt)

        // ---------------- 🔥 EXPAND / COLLAPSE LOGIC ----------------

        val isExpanded = expandedPositions.contains(position)

        holder.descriptionText.maxLines = if (isExpanded) Int.MAX_VALUE else 3
        holder.descriptionText.ellipsize =
            if (isExpanded) null else android.text.TextUtils.TruncateAt.END

        holder.textToggle.text = if (isExpanded) "See less" else "See more"

        // 🔥 Show toggle ONLY if text is actually truncated
        holder.descriptionText.post {
            val layout = holder.descriptionText.layout

            if (layout != null) {
                val lastLine = layout.lineCount - 1
                val isEllipsized = layout.getEllipsisCount(lastLine) > 0

                holder.textToggle.visibility =
                    if (isExpanded || isEllipsized) View.VISIBLE
                    else View.GONE
            }
        }

        holder.textToggle.setOnClickListener {
            if (isExpanded) {
                expandedPositions.remove(position)
            } else {
                expandedPositions.add(position)
            }
            notifyItemChanged(position)
        }

        // ---------------- CURRICULUM ----------------

        val curriculum = post.CurriculumNew?.mainTopic
        val subTopic = post.CurriculumSubTopic?.name
        val activities = post.activityDetails?.joinToString { it.name }

        // ✅ Build display text
        val displayText = buildString {

            if (!curriculum.isNullOrEmpty()) {
                append(curriculum)
            }

            if (!subTopic.isNullOrEmpty()) {
                append(" > $subTopic")
            }

            val activityList = post.activityDetails
                ?.joinToString(separator = "\n• ") { it.name }
            if (!activities.isNullOrEmpty()) {
                append(" >  $activities")
            }
        }

        // ✅ Fallback
        holder.curriculumText.text =
            if (displayText.isEmpty()) "No Curriculum"
            else displayText


        // ---------------- IMAGE ----------------

        // Load first image if available
        if (post.images.isNotEmpty()) {
            val imageUrl = "$baseUrl/${post.images[0]}" // <-- use passed base URL
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.avatar_placeholder)
        }

        holder.imageView.setOnClickListener {
            if (post.images.isNotEmpty()) {
                onImageClick(post, 0) // open starting from first image
            }
        }


        // ---------------- ACTIONS ----------------

        if (userRole == "teacher") {
            holder.iconDelete.visibility = View.VISIBLE
            holder.iconEdit.visibility = View.VISIBLE

            holder.iconDelete.setOnClickListener { onDeleteClick(post) }
            holder.iconEdit.setOnClickListener { onEditClick(post) }
        } else {
            holder.iconDelete.visibility = View.GONE
            holder.iconEdit.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int = posts.size

    fun submitList(newPosts: List<JournalPost>) {
        posts = newPosts

        // 🔥 IMPORTANT: reset expansion when new data arrives
        expandedPositions.clear()

        notifyDataSetChanged()
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val colomboTZ = TimeZone.getTimeZone("Asia/Colombo")
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = colomboTZ
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.timeZone = colomboTZ
            formatter.format(date!!)
        } catch (e: Exception) {
            isoDate
        }
    }
}