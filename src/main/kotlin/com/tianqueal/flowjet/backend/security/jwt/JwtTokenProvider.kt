package com.tianqueal.flowjet.backend.security.jwt

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.JwtResponse
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.time.Instant
import java.util.Date

/**
 * Service responsible for generating and parsing JWT tokens.
 *
 * @author Christian A.
 */
@Component
class JwtTokenProvider(
  @field:Qualifier(BeanNames.JWT_SIGNING_PRIVATE_KEY)
  private val signingKey: PrivateKey,
  private val jwtDecoder: JwtDecoder
) {
  /**
   * Generates an access token for the given username.
   * @param authentication The authentication object containing user details.
   * @param extendedExpiration Whether to use extended expiration.
   * @return JwtResponse with token details.
   */
  fun generateAccessTokenDetails(
    authentication: Authentication,
    extendedExpiration: Boolean
  ): JwtResponse {
    val authorities = authentication.authorities.map { it.authority }
    val expiresInSeconds = if (extendedExpiration)
      SecurityConstants.EXTENDED_ACCESS_TOKEN_EXPIRATION_SECONDS
    else
      SecurityConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS

    return generateTokenDetails(
      subject = authentication.name,
      claims = mapOf(SecurityConstants.AUTHORITIES_CLAIM_NAME to authorities),
      expiresInSeconds = expiresInSeconds,
      purpose = SecurityConstants.TOKEN_PURPOSE_ACCESS,
      authScheme = SecurityConstants.HTTP_AUTH_SCHEME_BEARER,
    )
  }

  /**
   * Generates an email verification token for the given username.
   * @param username The username for which the token is generated.
   * @return JwtResponse with token details.
   */
  fun generateEmailVerificationTokenDetails(username: String): JwtResponse = generateTokenDetails(
    subject = username,
    expiresInSeconds = SecurityConstants.DEFAULT_EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS,
    purpose = SecurityConstants.TOKEN_PURPOSE_EMAIL_VERIFICATION,
  )

  /**
   * Extracts the subject from a JWT token.
   * @param token The JWT token.
   * @throws JwtException if the token is invalid or subject is missing.
   * @return The subject (user identifier, typically username or email) from the token.
   */
  fun getSubjectFromToken(token: String): String {
    val jwt: Jwt = jwtDecoder.decode(token)
    val subject = jwt.subject
    if (subject.isNullOrEmpty()) {
      throw JwtException("Invalid JWT token: subject is null or empty")
    }
    return subject
  }

  /**
   * Generates a password reset token for the given email.
   * @param email The email address for which the token is generated.
   * @return JwtResponse with token details.
   */
  fun generatePasswordResetTokenDetails(email: String): JwtResponse = generateTokenDetails(
    subject = email,
    expiresInSeconds = SecurityConstants.DEFAULT_PASSWORD_RESET_TOKEN_EXPIRATION_SECONDS,
    purpose = SecurityConstants.TOKEN_PURPOSE_PASSWORD_RESET,
  )

  /**
   * Generates a JWT token with custom claims.
   * @param subject The subject of the token (typically username or email).
   * @param purpose The purpose of the token (e.g., access_token, email_verification).
   * @param authScheme The HTTP Authorization scheme to use (e.g., Bearer).
   * @param issuedAt The time the token is issued. Defaults to current time.
   * @param expiresInSeconds The expiration time in seconds.
   * @param claims Additional claims to include in the token.
   * @return JwtResponse with token details.
   */
  private fun generateTokenDetails(
    subject: String,
    claims: Map<String, Any> = emptyMap(),
    issuedAt: Instant = Instant.now(),
    expiresInSeconds: Long,
    authScheme: String? = null,
    purpose: String,
  ): JwtResponse {
    val expirationDate = issuedAt.plusSeconds(expiresInSeconds)
    val token = Jwts.builder()
      .subject(subject)
      .apply { claims.forEach { claim(it.key, it.value) } }
      .issuedAt(Date.from(issuedAt))
      .expiration(Date.from(expirationDate))
      .signWith(signingKey, Jwts.SIG.RS256)
      .compact()

    return JwtResponse(
      token = token,
      tokenPurpose = purpose,
      authScheme = authScheme,
      expiresInSeconds = expiresInSeconds,
      expiresAt = expirationDate
    )
  }
}
