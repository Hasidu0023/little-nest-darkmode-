package com.littlenest.nursery.ui.journal


data class JournalPost(
    val id: Int,
    val description: String,
    val images: List<String> = emptyList(),

    // ✅ NEW SYSTEM
    val curriculumId: Int? = null,
    val subTopicId: Int? = null,
    val activities: List<Int>? = emptyList(),

    val groupId: Int,
    val teacherId: Int,

    val taggedStudents: List<Int>? = emptyList(),

    val createdAt: String,
    val updatedAt: String,

    // ✅ NEW (IMPORTANT)
    val CurriculumNew: CurriculumData? = null,
    val CurriculumSubTopic: SubTopicData? = null,
    val activityDetails: List<ActivityData>? = emptyList()
)

data class CurriculumData(
    val id: Int,
    val mainTopic: String
)

data class SubTopicData(
    val id: Int,
    val name: String
)

data class ActivityData(
    val id: Int,
    val name: String
)