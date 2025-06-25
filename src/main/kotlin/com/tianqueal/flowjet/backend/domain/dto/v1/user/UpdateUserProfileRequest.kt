package com.tianqueal.flowjet.backend.domain.dto.v1.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserAvatarUrlConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserNameConstraint
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "DTO for a user to update their profile")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateUserProfileRequest(
  @field:UserNameConstraint
  val name: String,

  @field:UserAvatarUrlConstraint
  val avatarUrl: String? = null,
)
