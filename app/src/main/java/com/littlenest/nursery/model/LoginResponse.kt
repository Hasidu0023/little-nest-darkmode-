package com.littlenest.nursery.model

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)
