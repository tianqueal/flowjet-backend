package com.tianqueal.flowjet.backend.domain.dto.v1.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.task.TaskDescriptionConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.task.TaskNameConstraint
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.Instant

@Schema(description = "Request DTO for updating an existing task")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateTaskRequest(
    @field:TaskNameConstraint
    val name: String,
    @field:TaskDescriptionConstraint
    val description: String? = null,
    @field:NotNull(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_NOT_NULL)
    @field:Positive(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_POSITIVE)
    val statusId: Int,
    @field:Schema(
        description = "Due date for the task in ISO-8601 format",
        example = "2020-01-01T00:00:00Z",
        nullable = true,
    )
    val dueDate: Instant? = null,
)
