package com.tianqueal.flowjet.backend.domain.dto.v1.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import com.tianqueal.flowjet.backend.utils.constants.ValidationPatterns
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO for an administrator to partially update a user's profile. Only provided fields will be updated.")
data class UpdateUserSelfRequest(
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
  @field:URL(message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_INVALID)
  @field:Pattern(
    regexp = "^$|^(https?)://.*$",
    message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_NOT_BLANK
  )
  val avatarUrl: String? = null,
)
