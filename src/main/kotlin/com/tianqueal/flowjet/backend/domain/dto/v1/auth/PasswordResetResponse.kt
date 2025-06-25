package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response for password reset request")
data class PasswordResetResponse(
    @field:Schema(description = "Message indicating the result of the password reset request")
    val message: String,
)
