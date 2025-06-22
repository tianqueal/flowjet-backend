package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import com.tianqueal.flowjet.backend.utils.constants.ValidationPatterns
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PasswordResetConfirmRequest(
  @Schema(
    description = "Token for password reset confirmation",
    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  )
  val token: String,

  @field:Schema(
    description = "New password to set for the user",
    example = "SecureP@ssw0rd",
    minLength = 8,
    maxLength = 64,
    requiredMode = Schema.RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_NOT_BLANK)
  @field:Size(
    min = 8,
    max = 64,
    message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_SIZE
  )
  @field:Pattern(
    regexp = ValidationPatterns.PASSWORD,
    message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_PATTERN
  )
  val newPassword: String,
)
