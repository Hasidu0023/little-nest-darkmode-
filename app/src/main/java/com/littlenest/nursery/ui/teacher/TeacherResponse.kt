package com.littlenest.nursery.ui.teacher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName


@Parcelize
data class TeacherResponse(
    val teachers: List<Teacher>
) : Parcelable

@Parcelize
data class TeacherByIdResponse(
    val teacher: Teacher
) : Parcelable

@Parcelize
data class Teacher(
    //val teacherId: Int,
    @SerializedName("teacher_id") val teacherId: Int,
    val userId: Int?,
    val username: String,
//    val password: String,
    val gender: String,
    val role: String,
    val extraData: TeacherExtraData
) : Parcelable

@Parcelize
data class TeacherExtraData(
    val profilePicture: String?,
    val nurseryName: String,
    val assignedGroups: List<String>,
    val name: String
) : Parcelable


@Parcelize
data class RegisterRequestTeacher(
    val username: String,
    val password: String,
    val gender: String,
    val role: String = "teacher",
    val extraData: RegisterTeacherExtraData
) : Parcelable

@Parcelize
data class RegisterTeacherExtraData(
    val profilePicture: String?,
    val nurseryId: Int,
    val assignedGroups: List<Int>,
    val name: String
) : Parcelable

@Parcelize
data class UpdateTeacherRequest(
    val username: String,
    val password: String?,
    val gender: String,
    val name: String,
    val nurseryId: Int,
    val assignedGroups: List<Int>,
    val profilePicture: String?
) : Parcelable

@Parcelize
data class UpdateTeacherResponse(
    val message: String,
    val teacher: UpdatedTeacherData
) : Parcelable

@Parcelize
data class UpdatedTeacherData(
    val id: Int,
    val name: String,
    val profilePicture: String?,
    val userId: Int,
    val assignedGroups: List<Int>,
    val nurseryId: Int,
    val createdAt: String,
    val updatedAt: String
) : Parcelable




