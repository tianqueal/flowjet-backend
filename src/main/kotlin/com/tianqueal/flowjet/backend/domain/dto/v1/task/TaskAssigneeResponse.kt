package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "DTO for Task Assignee Response")
data class TaskAssigneeResponse(
    @field:Schema(description = "User profile of the task assignee")
    val assignee: UserProfileResponse,
    @field:Schema(description = "Timestamp when the user was assigned to the task", example = "2020-01-01T00:00:00Z")
    val assignedAt: Instant,
)
