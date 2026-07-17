package com.littlenest.nursery.ui.journal

//data class CreatePostRequest(
//    val description: String,
//    val groupId: List<Int>,
//    val curriculumId: Int?,
//    val topicIds: List<List<String>>,
//    val taggedStudentIds: List<Int>
//)

data class CreatePostRequest(
    val description: String,
    val groupId: Int,
    val curriculumId: Int?,
    val selectedSubTopics: List<List<String>>,
    val taggedStudents: List<Int>
)