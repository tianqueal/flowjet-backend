package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.tianqueal.flowjet.backend.utils.enums.TaskStatusEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "DTO for Task Status")
data class TaskStatusResponse(
    @field:Schema(description = "Unique identifier of the task status")
    val id: Int,
    @field:Schema(description = "Code of the task status")
    val code: TaskStatusEnum,
    @field:Schema(description = "Name of the task status")
    val name: String,
)
