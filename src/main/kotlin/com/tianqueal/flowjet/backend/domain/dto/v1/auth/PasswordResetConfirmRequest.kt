package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserPasswordConstraint
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request for confirming password reset")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PasswordResetConfirmRequest(
  @field:Schema(
    description = "Token for password reset confirmation",
    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_AUTH_TOKEN_NOT_BLANK)
  val token: String,

  @field:UserPasswordConstraint
  val newPassword: String,
)
