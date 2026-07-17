package com.littlenest.nursery.model
import com.littlenest.nursery.ui.student.ExtraData

data class RegisterRequest(
    val username: String,
    val password: String,
    val gender: String,
    val role: String = "student",
    val extraData: ExtraData
)
