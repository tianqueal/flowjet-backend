package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class TaskCommentResponse(
    @field:Schema(description = "Unique identifier of the task comment")
    val id: Long,
    @field:Schema(description = "User who authored the task comment")
    val author: UserProfileResponse,
    @field:Schema(description = "Content of the comment", example = "This is a comment on the task.")
    val content: String,
//    @field:Schema(description = "Parent comment id if this is a reply to another comment", nullable = true)
//    val parentId: Long? = null,
    val replies: List<TaskCommentResponse> = emptyList(),
    @field:Schema(description = "Timestamp when the comment was created")
    val createdAt: Instant,
    @field:Schema(description = "Timestamp when the comment was last updated")
    val updatedAt: Instant,
)
