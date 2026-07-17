package com.littlenest.nursery.ui.event

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

//@Parcelize
//data class Event(
//    val id: Int,
//    val eventName: String,
//    val eventDescription: String,
//    val eventLocation: String,
//    val date: String,
//    val starts: String,
//    val ends: String,
//    val repeating: String,
//    val repeatingEnds: String?,
//    @SerializedName("status")
//    val status: String?,
//    val invitedGroups: List<String> = emptyList()
//) : Parcelable


@Parcelize
data class Event(
    val id: Int = 0,
    val eventName: String? = null,
    val eventDescription: String? = null,
    val eventLocation: String? = null,
    val date: String? = null,
    val starts: String? = null,
    val ends: String? = null,
    val repeating: String? = null,
    val repeatingEnds: String? = null,
    @SerializedName("status")
    val status: String? = null,
    val invitedGroups: List<Int> = emptyList()
) : Parcelable


data class EventSummaryResponse(
    val eventId: Int,
    val title: String,
    val groups: List<GroupSummary>
)

data class GroupSummary(
    val groupName: String,
    val accepted: Int,
    val declined: Int,
    val pending: Int,
    val totalStudents: Int
)

data class EventCreateRequest(
    val eventName: String,
    val eventDescription: String,
    val eventLocation: String,
    val date: String,
    val starts: String,
    val ends: String,
    val repeating: Boolean,
    val repeatingEnds: String? = null,
    val invitedGroups: List<Int>
)