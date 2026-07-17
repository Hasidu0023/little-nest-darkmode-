package com.littlenest.nursery.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val baseUrl: String,
    private val currentUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun setMessages(newMessages: List<Message>) {
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize() = messages.size
            override fun getNewListSize() = newMessages.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // Compare by unique ID
                return messages[oldItemPosition].id == newMessages[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // Compare entire content
                return messages[oldItemPosition] == newMessages[newItemPosition]
            }
        }

        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val ivMessageImage: ImageView = itemView.findViewById(R.id.ivMessageImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: Message) {
            tvMessage.text = message.messageText ?: ""
            tvMessage.visibility = if (!message.messageText.isNullOrEmpty()) View.VISIBLE else View.GONE

            if (!message.fileUrl.isNullOrEmpty()) {
                ivMessageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load("$baseUrl${message.fileUrl}")
                    .into(ivMessageImage)
            } else {
                ivMessageImage.visibility = View.GONE
            }

            tvTime.text = formatTime(message.createdAt)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val ivMessageImage: ImageView = itemView.findViewById(R.id.ivMessageImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvSenderName: TextView? = itemView.findViewById(R.id.tvSenderName)
        private val ivSenderProfile: ImageView? = itemView.findViewById(R.id.ivSenderProfile)

        fun bind(message: Message) {
            tvMessage.text = message.messageText ?: ""
            tvMessage.visibility = if (!message.messageText.isNullOrEmpty()) View.VISIBLE else View.GONE

            if (!message.fileUrl.isNullOrEmpty()) {
                ivMessageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load("$baseUrl${message.fileUrl}")
                    .placeholder(R.drawable.avatar_placeholder) // show while loading
                    .error(R.drawable.avatar_placeholder)        // fallback on error
                    .circleCrop()                       // round avatar
                    .into(ivMessageImage)                   // round avatar                    .into(ivMessageImage)
            } else {
                ivMessageImage.visibility = View.GONE
            }

            tvTime.text = formatTime(message.createdAt)


            // Show sender info only in group chat
            if (message.groupId != null && message.senderId != currentUserId) {
                tvSenderName?.visibility = View.VISIBLE
                ivSenderProfile?.visibility = View.VISIBLE
                tvSenderName?.text = message.senderName

                if (!message.senderProfile.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load("$baseUrl${message.senderProfile}")
                        .circleCrop()
                        .into(ivSenderProfile!!)
                } else {
                    ivSenderProfile?.setImageResource(R.drawable.avatar_placeholder)
                }
            } else {
                tvSenderName?.visibility = View.GONE
                ivSenderProfile?.visibility = View.GONE
            }

        }
    }

    private fun formatTime(dateString: String?): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateString) ?: return ""
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
