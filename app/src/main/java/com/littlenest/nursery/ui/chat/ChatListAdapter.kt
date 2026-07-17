package com.littlenest.nursery.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.R
import com.bumptech.glide.Glide

class ChatListAdapter(
    private var chatList: List<ChatListItem>,
    private val baseUrl: String,
    private val onChatClick: (ChatListItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

//    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//        val chat = chatList[position]
//
//        holder.tvName.text = chat.partnerName
//
//        val lastMessage = chat.lastMessage
//        if (lastMessage.isNullOrEmpty()) {
//            holder.tvLastMessage.text = "📷 Photo"
//        }else {
//            holder.tvLastMessage.text = chat.lastMessage
//        }
//        holder.tvTime.text = chat.createdAt
//        Glide.with(holder.itemView.context)
//            .load("http://10.0.2.2:3000" + chat.partnerProfilePicture)
//            .placeholder(R.drawable.avatar_placeholder)
//            .circleCrop()
//            .into(holder.imgProfile)
//
//        holder.itemView.setOnClickListener {
//            onChatClick(chat)
//        }
//    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        if (chat.type == "group") {
            // 👥 Group chat
            holder.tvName.text = chat.groupName ?: "Group Chat"
            holder.tvLastMessage.text = chat.lastMessage.ifEmpty { "No messages yet" }

            // Default group icon
            Glide.with(holder.itemView.context)
                .load(R.drawable.ic_group_chat) // add this drawable
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(holder.imgProfile)
        } else {
            // 👤 Private chat
            holder.tvName.text = chat.partnerName
            holder.tvLastMessage.text =
                if (chat.lastMessage.isNullOrEmpty()) "📷 Photo" else chat.lastMessage

            Glide.with(holder.itemView.context)
                .load("$baseUrl${chat.partnerProfilePicture}")
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(holder.imgProfile)
        }

        holder.tvTime.text = chat.createdAt

        // 🔹 Bold if message is unread
        val typeface = if (!chat.isRead) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
        holder.tvLastMessage.setTypeface(null, typeface)

        holder.itemView.setOnClickListener {
            onChatClick(chat)
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun setData(newChats: List<ChatListItem>) {
        chatList = newChats
        notifyDataSetChanged()
    }
}
