package com.littlenest.nursery.ui.attendance

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a single absence record fetched from the
 * student absence API.
 */
data class AbsenceEntry(
    val id: Int,
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("date")
    val date: String, // The crucial date string (e.g., "2025-10-16")
    @SerializedName("groupId")
    val groupId: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)