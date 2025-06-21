package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse

interface EmailVerificationService {
  fun generateEmailVerificationToken(user: UserResponse): String
  fun sendEmailVerification(user: UserResponse, apiVersionPath: String)
  fun verifyToken(token: String)
}
