package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.EmailService
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.UserService
import org.springframework.stereotype.Service

@Service
class EmailVerificationServiceImpl(
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailService: EmailService,
    private val userService: UserService,
) : EmailVerificationService {
    /**
     * Generates an email verification token for the given user.
     * @param user The user for whom the token is generated.
     * @return The generated email verification token.
     */
    override fun generateToken(user: UserResponse): String = jwtTokenProvider.generateEmailVerificationTokenDetails(user.username).token

    /**
     * Sends an email verification to the user.
     * @param user The user to whom the verification email is sent.
     * @param apiVersionPath The API version path to include in the email.
     */
    override fun sendEmail(
        user: UserResponse,
        apiVersionPath: String,
    ) = emailService.sendEmailVerification(
        to = user.email,
        name = user.username,
        token = generateToken(user),
        apiVersionPath = apiVersionPath,
    )

    /**
     * Verifies the provided token and marks the user as verified.
     * @param token The email verification token.
     */
    override fun verifyTokenAndMarkAsVerified(token: String) = userService.verify(jwtTokenProvider.getSubjectFromToken(token))
}
