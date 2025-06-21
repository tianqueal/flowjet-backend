package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Login response with JWT token and user message")
data class LoginResponse(
  @field:Schema(description = "User message after login", example = "Login successful")
  val message: String,

  @field:Schema(description = "JWT token and metadata", implementation = JwtResponse::class)
  val jwtResponse: JwtResponse,
)
