package com.tianqueal.flowjet.backend.domain.dto.v1.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import com.tianqueal.flowjet.backend.utils.constants.ValidationPatterns
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO for creating a new user")
data class CreateUserRequest(
  @field:Schema(
    description = "Username for the user",
    example = "john.doe",
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_USERNAME_NOT_BLANK)
  @field:Size(
    min = 3,
    max = 50,
    message = ValidationMessageKeys.VALIDATION_USER_USERNAME_SIZE
  )
  @field:Pattern(
    regexp = ValidationPatterns.USERNAME,
    message = ValidationMessageKeys.VALIDATION_USER_USERNAME_PATTERN
  )
  val username: String,

  @field:Schema(
    description = "User email address",
    example = "john.doe@example.com",
    maxLength = 255,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_NOT_BLANK)
  @field:Email(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_INVALID)
  @field:Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_EMAIL_SIZE)
  val email: String,

  @field:Schema(
    description = "Full name of the user",
    example = "John Doe",
    minLength = 3,
    maxLength = 100,
    pattern = ValidationPatterns.NAME,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_NAME_NOT_BLANK)
  @field:Size(
    min = 3,
    max = 100,
    message = ValidationMessageKeys.VALIDATION_USER_NAME_SIZE
  )
  @field:Pattern(
    regexp = ValidationPatterns.NAME,
    message = ValidationMessageKeys.VALIDATION_USER_NAME_PATTERN
  )
  val name: String,

  @field:Schema(
    description = "Password for the user",
    example = "SecureP@ssw0rd",
    minLength = 8,
    maxLength = 64,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_NOT_BLANK)
  @field:Size(min = 8, max = 64, message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_SIZE)
  @field:Pattern(
    regexp = ValidationPatterns.PASSWORD,
    message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_PATTERN
  )
  val password: String,

  @field:Schema(
    description = "URL of the user's avatar image",
    example = "https://example.com/avatar.jpg",
    maxLength = 255,
    nullable = true
  )
  @field:Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_SIZE)
  @field:URL(message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_INVALID)
  val avatarUrl: String? = null,
)
