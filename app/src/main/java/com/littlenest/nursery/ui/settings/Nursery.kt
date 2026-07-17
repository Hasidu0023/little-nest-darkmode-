package com.littlenest.nursery.ui.settings

data class Nursery(
    val id: Int,
    val name: String,
    val address: String,
    val description: String,
    val language: String,
    val image: String?,
    val nursery_email: String,
)

data class NurseryResponse(
    val nursery: Nursery
)

data class UpdateNurseryRequest(
    val name: String,
    val description: String,
    val nursery_email: String,
    val address: String,
    val language: String
)
