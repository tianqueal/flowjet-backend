package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class PasswordResetRequest(
  @Schema(
    description =
      "The email address associated with the account. A reset link will be sent to this email.",
    example = "john.doe@example.com",
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_NOT_BLANK)
  @field:Email(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_INVALID)
  val email: String,
)
