package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.EmailService
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.UserService
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class EmailVerificationServiceImpl(
  private val jwtTokenProvider: JwtTokenProvider,
  private val emailService: EmailService,
  private val userService: UserService,
) : EmailVerificationService {
  override fun generateEmailVerificationToken(user: UserResponse): String =
    jwtTokenProvider.generateEmailVerificationTokenDetails(user.username).token

  override fun sendEmailVerification(user: UserResponse, apiVersionPath: String) {
    val token = generateEmailVerificationToken(user)
    val locale = LocaleContextHolder.getLocale()
    emailService.sendEmailVerification(
      user.email,
      user.username,
      token,
      locale,
      apiVersionPath
    )
  }

  override fun verifyToken(token: String) {
    val username = jwtTokenProvider.getUsernameFromToken(token)
    userService.markUserAsVerifiedByUsername(username)
  }
}
