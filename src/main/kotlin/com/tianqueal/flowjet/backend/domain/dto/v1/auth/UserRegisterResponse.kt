package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User registration response")
data class UserRegisterResponse(
    @field:Schema(description = "User message after login", example = "Login successful")
    val message: String,
    @field:Schema(description = "User data", implementation = UserResponse::class)
    val userResponse: UserResponse,
)
