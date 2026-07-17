package com.littlenest.nursery.ui.chat

data class UserItem(
    val id: Int,
    val role: String,
    val profileId: Int?,
    val profilePicture: String?,
    val fullName: String
)