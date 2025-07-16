package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.tianqueal.flowjet.backend.annotations.constrains.v1.task.TaskCommentContentConstraint
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to create a new task comment")
data class CreateTaskCommentRequest(
    @field:TaskCommentContentConstraint
    val content: String,
    @field:Schema(description = "Optional parent comment ID for threaded comments")
    val parentId: Long? = null,
)
