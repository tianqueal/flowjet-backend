package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.EmailService
import com.tianqueal.flowjet.backend.services.PasswordResetService
import com.tianqueal.flowjet.backend.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PasswordResetServiceImpl(
  private val jwtTokenProvider: JwtTokenProvider,
  private val emailService: EmailService,
  private val userService: UserService,
) : PasswordResetService {
  /**
   * Generates a password reset token for the user with the provided email address.
   * @param email The email address of the user requesting a password reset.
   * @return The generated password reset token.
   */
  override fun generatePasswordResetToken(email: String) =
    jwtTokenProvider.generatePasswordResetTokenDetails(email).token

  /**
   * Sends a password reset email to the user with the provided email address.
   * If the user is not found, it silently returns without throwing an error.
   * @param email The email address of the user requesting a password reset.
   * @param apiVersionPath The API version path to include in the email.
   */
  override fun sendPasswordResetEmail(email: String, apiVersionPath: String) {
    try {
      val user = userService.findByEmail(email)
      val token = generatePasswordResetToken(email)
      emailService.sendPasswordResetEmail(
        to = user.email,
        name = user.username,
        token = token,
        apiVersionPath = apiVersionPath
      )
    } catch (ex: UserNotFoundException) {
      log.warn(
        "{}: User with email '{}' not found. Password reset email not sent.",
        this::class.simpleName,
        email,
        ex
      )
      return
    }
  }

  /**
   * Verifies the provided token and resets the user's password.
   * @param token The password reset token.
   * @param newPassword The new password to set for the user.
   */
  override fun verifyTokenAndResetPassword(token: String, newPassword: String) =
    userService.resetPasswordByEmail(jwtTokenProvider.getSubjectFromToken(token), newPassword)

  companion object {
    private val log = LoggerFactory.getLogger(PasswordResetServiceImpl::class.java)
  }
}
