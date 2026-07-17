package com.littlenest.nursery.ui.group

data class Group(
    val id: Int,
    val userId: Int,
    val nurseryId: Int,
    val curriculumId: Int?,
    val name: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String
)

data class GetGroupResponse(
    val groups: List<Group>
)

data class CreateGroupRequest(
    val name: String,
    val description: String
)