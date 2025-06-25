package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.auth.UsernameOrEmailConstraint
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.constraints.NotNull

@Schema(description = "DTO for login request")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginRequest(
  @field:UsernameOrEmailConstraint
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
