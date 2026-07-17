package com.littlenest.nursery.ui.teacher

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ------------------ API RESPONSE ------------------

@Parcelize
data class SingleTeacherByIdResponse(
    val teacher: SingleTeacher
) : Parcelable


// ------------------ TEACHER ------------------

@Parcelize
data class SingleTeacher(
    @SerializedName("teacher_id")
    val teacherId: Int,

    @SerializedName("user_id")
    val userId: Int,

    val username: String,
    val gender: String,
    val role: String,

    val extraData: SingleTeacherExtraData
) : Parcelable


// ------------------ EXTRA DATA ------------------

@Parcelize
data class SingleTeacherExtraData(
    val name: String,
    val profilePicture: String?,
    val nursery: NurseryMini,
    val assignedGroups: List<GroupMini>
) : Parcelable {

    // ✅ optional helper (no backend change needed)
    val nurseryName: String
        get() = nursery.name
}


// ------------------ NURSERY ------------------

@Parcelize
data class NurseryMini(
    val id: Int,
    val name: String
) : Parcelable


// ------------------ GROUP ------------------

@Parcelize
data class GroupMini(
    val id: Int,
    val name: String
) : Parcelable
