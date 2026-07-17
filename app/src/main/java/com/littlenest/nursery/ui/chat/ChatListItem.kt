package com.littlenest.nursery.ui.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class ChatListItem(
//    val partnerId: Int,
//    val partnerName: String,
//    val partnerProfilePicture: String?,
//    val lastMessage: String,
//    val createdAt: String,
//    val isRead: Boolean
//) : Parcelable


@Parcelize
data class ChatListItem(
    val messageId: Int,
    val type: String, // "private" or "group"
    val partnerId: Int,
    val partnerName: String? = null,
    val partnerProfilePicture: String? = null,
    val groupId: Int? = null,
    val groupName: String? = null,
    val lastMessage: String,
    val createdAt: String,
    val isRead: Boolean = false
) : Parcelable
