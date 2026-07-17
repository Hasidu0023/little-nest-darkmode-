package com.littlenest.nursery.ui.curriculumNew

data class Standard(
    val id: Int,
    val name: String,
    val description: String?
)

data class StandardResponse(
    val data: List<Standard>
)


//---Curriculum---------------------------
data class Curriculum(
    val id: Int,
    val mainTopic: String,
    val standardId: Int,
    val nurseryId: Int
)

data class CurriculumNewResponse(
    val data: List<Curriculum>
)

data class CurriculumNewRequest(
    val mainTopic: String,
    val standardId: Int
)

//--Fetch Curriculum by Id----------------------
data class CurriculumByIdResponse(
    val data: CurriculumDetail
)

data class CurriculumDetail(
    val id: Int,
    val mainTopic: String,
    val standardId: Int,
    val nurseryId: Int,
    val subTopics: List<SubTopicDetail>
)

data class SubTopicDetail(
    val id: Int,
    val name: String,
    val curriculumId: Int,
    val activities: List<ActivityDetail>
)

data class ActivityDetail(
    val id: Int,
    val name: String,
    val description: String?
)


//---SubTopics---------------------------
data class SubTopic(
    val id: Int,
    val name: String,
    val curriculumId: Int,
)
data class SubTopicResponse(
    val data: List<SubTopic>
)

data class SubTopicRequest(
    val name: String,
    val curriculumId: Int
)


//---Activities----------------------------
data class Activity(
    val id: Int,
    val name: String,
    val description: String?,
    val subTopicId: Int
)

data class ActivityResponse(
    val data: List<Activity>
)

data class ActivityRequest(
    val name: String,
    val description: String,
    val subTopicId: Int
)
