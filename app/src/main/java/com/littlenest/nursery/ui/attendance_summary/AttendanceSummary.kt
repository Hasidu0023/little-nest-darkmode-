package com.littlenest.nursery.ui.attendance_summary
import com.google.gson.annotations.SerializedName

data class AttendanceSummary(
    val date: String,
    val presentCount: Int,
    val totalStudents: Int,
    val absentStudents: List<StudentAttendance>
)

data class StudentAttendance(
    val id: Int,
    @SerializedName("fullName")
    val name: String,
    val profilePicture: String?
)

data class TotalStudentsResponse(
    val groupId: Int,
    val totalStudents: Int
)