package com.littlenest.nursery.ui.student

data class StudentResponse(
    val students: List<Student>
)

data class GetStudentResponse(
    val student: Student
)

//use in StudentViewModel for update student
data class UpdateStudentRequest(
    val profilePicture: String?,
    val fullName: String,
    val nickname: String,
    val address: String?,
    val city: String?,
    val nativeLanguage: String?,
    val allergies: String?,
    val comment: String?,
    val dateOfBirth: String,
    val dropOffTime: String?,
    val pickupTime: String?,
    val photoConsent: Boolean?,
)

data class UpdateStudentJsonRequest(
    val extraData: UpdateStudentRequest
)
data class MessageResponse(
    val message: String
)