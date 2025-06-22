package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import io.swagger.v3.oas.annotations.media.Schema

data class PasswordResetResponse(
  @Schema(description = "Message indicating the result of the password reset request")
  val message: String,
)
