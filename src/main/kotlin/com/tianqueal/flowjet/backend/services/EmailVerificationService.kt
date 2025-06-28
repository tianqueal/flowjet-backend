package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse

interface EmailVerificationService {
    fun generateToken(user: UserResponse): String

    fun sendEmail(
        user: UserResponse,
        apiVersionPath: String,
    )

    fun verifyTokenAndMarkAsVerified(token: String)
}
