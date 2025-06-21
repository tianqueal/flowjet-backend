package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO for login request")
data class LoginRequest(
  @field:Schema(
    description = "Username or email address for login",
    example = "user / user@example.com",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_AUTH_USERNAME_OR_EMAIL_NOT_BLANK)
  val usernameOrEmail: String,

  @field:Schema(
    description = "Password for login",
    example = "SecureP@ssw0rd",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotNull(message = ValidationMessageKeys.VALIDATION_AUTH_PASSWORD_NOT_NULL)
  val password: String,

  @field:Schema(
    description = "Flag to remember the user for future logins",
    example = "true",
    nullable = true,
    defaultValue = "false",
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  val rememberMe: Boolean = false,
)
