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
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO for an administrator to partially update a user's profile. Only provided fields will be updated.")
data class UpdateUserByAdminRequest(
  @field:Schema(
    description = "Username of the user. If provided, will be updated.",
    example = "john.doe",
    minLength = 3,
    maxLength = 50,
    pattern = ValidationPatterns.USERNAME,
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_USERNAME_NOT_BLANK)
  @field:Size(min = 3, max = 50, message = ValidationMessageKeys.VALIDATION_USER_USERNAME_SIZE)
  @field:Pattern(
    regexp = ValidationPatterns.USERNAME,
    message = ValidationMessageKeys.VALIDATION_USER_USERNAME_PATTERN
  )
  val username: String,

  @field:Schema(
    description = "User's email address. If provided, will be updated.",
    example = "john.doe@example.com",
    maxLength = 255,
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:Email(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_INVALID)
  @field:Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_EMAIL_SIZE)
  val email: String,

  @field:Schema(
    description = "User's full name. If provided, will be updated.",
    example = "John Doe",
    minLength = 3,
    maxLength = 100,
    pattern = ValidationPatterns.NAME,
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  @field:NotBlank(message = ValidationMessageKeys.VALIDATION_USER_NAME_NOT_BLANK)
  @field:Size(min = 3, max = 100, message = ValidationMessageKeys.VALIDATION_USER_NAME_SIZE)
  @field:Pattern(
    regexp = ValidationPatterns.NAME,
    message = ValidationMessageKeys.VALIDATION_USER_NAME_PATTERN
  )
  val name: String,

  @field:Schema(
    description = "URL of the user's avatar. If provided, will be updated.",
    example = "https://example.com/avatar.png",
    maxLength = 255,
    nullable = true,
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  @field:Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_SIZE)
  @field:URL(message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_INVALID)
  val avatarUrl: String? = null,

  //  @Schema(
  //    description = "Timestamp when the user account was verified. If provided, will be updated.",
  //    example = "2020-01-01T00:00:00Z",
  //    nullable = true,
  //    requiredMode = RequiredMode.NOT_REQUIRED
  //  )
  //  val verifiedAt: Instant? = null,

  @field:Schema(
    description = "Indicates when the user account itself expires. Used for temporary accounts (e.g., trials, contracts). If null, the account never expires. If provided, will be updated.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  val accountExpiredAt: Instant? = null,

  //  @Schema(
  //    description = "Indicates when the user account was locked, typically for security reasons like multiple failed login attempts. If null, the account is not locked. If provided, will be updated.",
  //    example = "2020-01-01T00:00:00Z",
  //    nullable = true,
  //    requiredMode = RequiredMode.NOT_REQUIRED
  //  )
  //  val lockedAt: Instant? = null,

  @field:Schema(
    description = "Indicates when the user's credentials (e.g., password) expire, forcing a change. If null, the credentials never expire. If provided, will be updated.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  val credentialsExpiredAt: Instant? = null,

  //  @Schema(
  //    description = "Master switch indicating if the user is administratively disabled. Stores the timestamp of when the disabling action occurred. If null, the user is enabled. If provided, will be updated.",
  //    example = "2020-01-01T00:00:00Z",
  //    nullable = true,
  //    requiredMode = RequiredMode.NOT_REQUIRED
  //  )
  //  val disabledAt: Instant? = null,
)
