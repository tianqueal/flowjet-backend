package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "DTO for Task Response")
data class TaskResponse(
    @field:Schema(description = "Unique identifier of the task")
    val id: Long,
    @field:Schema(description = "Name of the task")
    val name: String,
    @field:Schema(description = "Description of the task", nullable = true)
    val description: String? = null,
    @field:Schema(description = "Status of the task")
    val status: TaskStatusResponse,
    @field:Schema(description = "User who owns the task")
    val owner: UserProfileResponse,
    @field:Schema(description = "User assigned to the task", nullable = true)
    val dueDate: Instant? = null,
    @field:Schema(description = "Timestamp when the task was created")
    val createdAt: Instant,
    @field:Schema(description = "Timestamp when the task was last updated")
    val updatedAt: Instant,
)
