package com.tianqueal.flowjet.backend.domain.dto.v1.user

import com.tianqueal.flowjet.backend.domain.dto.v1.role.RoleResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "DTO for User")
data class UserResponse(
  @field:Schema(description = "Unique identifier of the user", example = "1")
  val id: Long,

  @field:Schema(description = "Username of the user", example = "john.doe")
  val username: String,

  @field:Schema(description = "User email address", example = "jdoe@example.com")
  val email: String,

  @field:Schema(description = "Full name of the user", example = "John Doe")
  val name: String,

  @field:Schema(
    description = "URL of the user's avatar image",
    example = "https://example.com/avatar.jpg",
    nullable = true
  )
  val avatarUrl: String? = null,

  @field:Schema(description = "User's roles")
  val roles: Set<RoleResponse> = emptySet(),

  @field:Schema(
    description = "Timestamp when the user account was verified",
    example = "2020-01-01T00:00:00Z",
    nullable = true
  )
  val verifiedAt: Instant? = null,

  @field:Schema(
    description = "Indicates when the user account itself expires",
    example = "2020-01-01T00:00:00Z",
    nullable = true
  )
  val accountExpiredAt: Instant? = null,

  @field:Schema(
    description = "Indicates when the user account was locked",
    example = "2020-01-01T00:00:00Z",
    nullable = true
  )
  val lockedAt: Instant? = null,

  @field:Schema(
    description = "Indicates when the user's credentials (e.g., password) expire, forcing a change",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
  )
  val credentialsExpiredAt: Instant? = null,

  @field:Schema(
    description = "Timestamp when the user was administratively disabled",
    example = "2020-01-01T00:00:00Z",
    nullable = true
  )
  val disabledAt: Instant? = null,

  @field:Schema(description = "Creation timestamp", example = "2020-01-01T00:00:00Z")
  val createdAt: Instant,

  @field:Schema(description = "Last update timestamp", example = "2020-01-01T00:00:00Z")
  val updatedAt: Instant,

  @field:Schema(
    description = "Logical deletion timestamp (null if not deleted)",
    example = "2020-01-01T00:00:00Z",
    nullable = true
  )
  val deletedAt: Instant? = null,
)
