package com.tianqueal.flowjet.backend.domain.dto.v1.error

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(
    description = "Standardised response object for representing API errors",
)
data class ErrorResponse(
    @field:Schema(
        description = "Short, machine-readable error code",
        example = MessageKeys.ERROR_USER_NOT_FOUND,
        nullable = true,
    )
    val code: String? = null,
    @field:Schema(
        description = "Name of the exception or error type",
        example = "UserNotFoundException",
        nullable = true,
    )
    val error: String? = null,
    @field:Schema(
        description = "Human-readable error message describing the problem",
        example = "User not found with username: 'john.doe'",
    )
    val message: String,
    @field:Schema(
        description = "Timestamp when the error occurred in ISO-8601 format",
        example = "2020-01-01T00:00:00Z",
    )
    val timestamp: Instant = Instant.now(),
    @field:Schema(
        description = "Request path where the error occurred",
        example = "/api/v1/users/1",
        nullable = true,
    )
    val path: String? = null,
    @field:Schema(
        description = "Additional details or context about the error",
        example = "{\"field\": \"email\", \"reason\": \"invalid format\"}",
        nullable = true,
    )
    val details: Any? = null,
)
