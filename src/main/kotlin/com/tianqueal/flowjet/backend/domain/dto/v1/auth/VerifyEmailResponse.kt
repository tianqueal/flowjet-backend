package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import io.swagger.v3.oas.annotations.media.Schema

data class VerifyEmailResponse(
    @field:Schema(
        description = "Indicates whether the email verification was successful",
        example = "true",
        defaultValue = "true",
    )
    val success: Boolean = true,
    @field:Schema(
        description = "Message providing additional information about the verification result",
        example = "Email verified successfully",
    )
    val message: String,
)
