package com.littlenest.nursery.model

data class User(
    val id: Int,
    val username: String,
    val gender: String,
    val role: String,
    val nurseryId: Int,
    val profileId: Int
)
