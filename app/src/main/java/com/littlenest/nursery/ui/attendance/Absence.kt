package com.littlenest.nursery.ui.attendance

import com.google.gson.annotations.SerializedName

/**
 * Request payload for marking a student's absence.
 * Only requires the date string in the format "YYYY-MM-DD".
 */
data class AbsenceRequest(
    @SerializedName("date")
    val date: String
)

/**
 * Represents the detailed absence record returned upon successful creation.
 */
data class AbsenceDetail(
    @SerializedName("id")
    val id: Int,
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("date")
    val date: String, // Date of absence
    @SerializedName("groupId")
    val groupId: Int,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Full response structure for the POST /api/absence endpoint.
 */
data class AbsenceResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("absence")
    val absence: AbsenceDetail
)
