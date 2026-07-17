package com.littlenest.nursery.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

data class GuardianResponse(
    val message: String,
    val studentProfile: StudentProfile
)

data class StudentProfile(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("student_id")
    val studentId: Int,
    val role: String,
    val guardians: List<Guardian>?
)

@Parcelize
data class Guardian(
    val id: Int,
    val relation: String,
    val name: String,
    val occupation: String?,
    val workPlace: String?,
    val nativeLanguage: String?,
    val mobilePhone: String?,
    val workPhone: String?,
    val homePhone: String?,
    val email: String?,
    val pickupPermission: Boolean
) : Parcelable

data class GuardianRequest(
    val relation: String,
    val name: String,
    val occupation: String?,
    val workPlace: String?,
    val nativeLanguage: String?,
    val workPhone: String?,
    val homePhone: String?,
    val mobilePhone: String?,
    val email: String?,
    val pickupPermission: Boolean
)