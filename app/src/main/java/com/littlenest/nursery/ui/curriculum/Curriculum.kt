package com.littlenest.nursery.ui.curriculum
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CurriculumRequest(
    val mainTopic: String,
    val subTopics: List<String>,
    val standard: String,
)

@Parcelize
data class CurriculumResponse(
    val id: Int,
    val mainTopic: String,
    val subTopics: List<String>,
    val standard: String,
    val createdBy: Int,
    val createdAt: String,
    val updatedAt: String,
) : Parcelable

data class GenericResponse(
    val message: String,
    val data: Any? = null
)

data class GenericListResponse<T>(
    val message: String,
    val data: List<T>
)

data class GroupedCurriculumResponse(
    val groupedCurriculums: Map<String, List<CurriculumResponse>>
)

data class UpdateCurriculumResponse(
    val message: String,
    val data: CurriculumRequest
)