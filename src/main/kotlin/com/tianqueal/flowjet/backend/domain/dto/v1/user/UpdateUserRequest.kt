package com.tianqueal.flowjet.backend.domain.dto.v1.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserAvatarUrlConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserEmailConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserNameConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserUsernameConstraint
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.Instant

@Schema(description = "DTO for an administrator to partially update a user's profile. Only provided fields will be updated.")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateUserRequest(
  @field:UserUsernameConstraint
  val username: String,

  @field:UserEmailConstraint
  val email: String,

  @field:UserNameConstraint
  val name: String,

  @field:UserAvatarUrlConstraint
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
