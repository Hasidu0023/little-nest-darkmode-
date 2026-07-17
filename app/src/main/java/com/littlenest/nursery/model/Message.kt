package com.littlenest.nursery.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: Int = 0,
    val senderId: Int,
    val receiverId: Int? = null, // nullable for group messages
    val groupId: Int? = null,    // nullable for personal messages
    val messageText: String? = null,
    val fileUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val senderName: String? = null,     // NEW (for group chat)
    val senderProfile: String? = null,  // NEW, image URL (for group chat)
) : Parcelable

data class MarkReadResponse(
    val message: String
)