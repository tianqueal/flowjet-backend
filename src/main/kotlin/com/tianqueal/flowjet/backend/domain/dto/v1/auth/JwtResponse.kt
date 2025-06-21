package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "JWT response")
data class JwtResponse(
  @field:Schema(
    description = "JWT access token",
    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  )
  val token: String,

  @field:Schema(
    description = "Purpose of the token (e.g., access_token, refresh_token, email_verification)",
    example = "access_token",
    nullable = true,
  )
  val tokenPurpose: String? = null,

  @field:Schema(
    description = "HTTP Authorization scheme to use in the Authorization header (e.g., Bearer, Basic)",
    example = "Bearer",
    nullable = true,
  )
  val authScheme: String? = null,

  @field:Schema(description = "Expiration time in seconds", example = "3600")
  val expiresInSeconds: Long,

  @field:Schema(
    description = "Expiration time of the token",
    example = "2020-01-01T00:00:00Z"
  )
  val expiresAt: Instant,
)
