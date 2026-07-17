package com.littlenest.nursery.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlenest.nursery.R

class UserAdapter(
    private var users: List<UserItem>,
    private val baseUrl: String,
    private val onClick: (UserItem) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.fullName
        holder.tvRole.text = user.role

        Glide.with(holder.itemView.context)
            .load("$baseUrl" + (user.profilePicture ?: ""))
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(holder.imgProfile)

        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun setData(newUsers: List<UserItem>) {
        users = newUsers
        notifyDataSetChanged()
    }
}