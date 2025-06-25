package com.tianqueal.flowjet.backend.domain.dto.v1.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "DTO for User Profile")
data class UserProfileResponse(
  @field:Schema(description = "Unique identifier of the user", example = "1")
  var id: Long,

  @field:Schema(description = "Username of the user", example = "john.doe")
  var username: String,

  @field:Schema(description = "Email of the user", example = "john.doe@example.com")
  var name: String,

  @field:Schema(
    description = "URL of the user's avatar image",
    example = "https://example.com/avatar.jpg"
  )
  var avatarUrl: String,
)
