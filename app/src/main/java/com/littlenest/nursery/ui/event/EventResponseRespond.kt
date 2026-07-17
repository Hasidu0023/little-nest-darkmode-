package com.littlenest.nursery.ui.event

data class EventResponseRespond(
    val message: String,
    val participant: Participant
)

data class Participant(
    val id: Int,
    val status: String,
    val eventId: Int,
    val studentId: Int,
    val createdAt: String,
    val updatedAt: String
)