package com.littlenest.nursery.ui.student
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.littlenest.nursery.model.Guardian
import com.google.gson.annotations.SerializedName


@Parcelize
data class Student(
    @SerializedName("student_id") val studentId: Int,
    val username: String,
    val gender: String,
    val role: String,
    val extraData: ExtraData
) : Parcelable

@Parcelize
data class ExtraData(
    val profilePicture: String? = null,
    val groupId: Int? = null,
    @SerializedName("groupname")
    val groupName: String? = "",
    val nurseryId: Int? = null,
    val fullName: String? = "",
    val nickname: String? = "",
    val dropOffTime: String? = null,
    val pickupTime: String? = null,
    val photoConsent: Boolean? = false,
    val dateOfBirth: String? = "",
    val address: String? = "",
    val city: String? = "",
    val nativeLanguage: String? = "",
    val allergies: String? = "",
    val comment: String? = "",
    val guardians: List<Guardian>? = emptyList()
) : Parcelable


//data class ExtraData(
//    val profilePicture: String?,
//    val groupId: Int,
//    val nurseryId: Int,
//    val fullName: String,
//    val nickname: String?,
//    val dropOffTime: String?,
//    val pickupTime: String?,
//    val photoConsent: Boolean?,
//    val dateOfBirth: String,
//    val address: String,
//    val city: String?,
//    val nativeLanguage: String,
//    val allergies: String,
//    val comment: String?,
//    val guardians: List<Guardian>?
//) : Parcelable
//

