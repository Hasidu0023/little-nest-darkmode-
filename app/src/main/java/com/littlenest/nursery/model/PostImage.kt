package com.littlenest.nursery.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostImage(
    val postId: Int,
    val imageUrl: String,
    val description: String,
    val createdAt: String
) : Parcelable
