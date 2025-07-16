package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.task.TaskCommentContentConstraint
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request DTO to update an existing task comment")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateTaskCommentRequest(
    @field:TaskCommentContentConstraint
    val content: String,
)
