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
 * @author Christian A.
 */
@Component
class JwtTokenProvider(
  @Qualifier(BeanNames.JWT_SIGNING_PRIVATE_KEY)
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
    val now = Instant.now()
    val expiresInSeconds = if (extendedExpiration)
      SecurityConstants.EXTENDED_ACCESS_TOKEN_EXPIRATION_SECONDS
    else
      SecurityConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS
    val expirationDate = now.plusSeconds(expiresInSeconds)
    val authorities = authentication.authorities.map { it.authority }
    val token = Jwts.builder()
      .subject(authentication.name)
      .claim(SecurityConstants.AUTHORITIES_CLAIM_NAME, authorities)
      .issuedAt(Date.from(now))
      .expiration(Date.from(expirationDate))
      .signWith(signingKey, Jwts.SIG.RS256)
      .compact()

    return JwtResponse(
      token = token,
      tokenPurpose = SecurityConstants.TOKEN_PURPOSE_ACCESS,
      authScheme = SecurityConstants.HTTP_AUTH_SCHEME_BEARER,
      expiresInSeconds = expiresInSeconds,
      expiresAt = expirationDate
    )
  }

  /**
   * Generates an email verification token for the given username.
   * @param username The username for which the token is generated.
   * @return JwtResponse with token details.
   */
  fun generateEmailVerificationTokenDetails(username: String): JwtResponse {
    val now = Instant.now()
    val expiresInSeconds = SecurityConstants.DEFAULT_EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS
    val expirationDate = now.plusSeconds(expiresInSeconds)

    val token = Jwts.builder()
      .subject(username)
      .issuedAt(Date.from(now))
      .expiration(Date.from(expirationDate))
      .signWith(signingKey, Jwts.SIG.RS256)
      .compact()

    return JwtResponse(
      token = token,
      tokenPurpose = SecurityConstants.TOKEN_PURPOSE_EMAIL_VERIFICATION,
      expiresInSeconds = expiresInSeconds,
      expiresAt = expirationDate
    )
  }

  /**
   * Extracts the username from a JWT token.
   * @param token The JWT token.
   * @throws JwtException if the token is invalid or subject is missing.
   * @return The username (subject) from the token.
   */
  fun getUsernameFromToken(token: String): String {
    val jwt: Jwt = jwtDecoder.decode(token)
    val username = jwt.subject
    if (username.isNullOrEmpty()) {
      throw JwtException("Invalid JWT token: subject is null or empty")
    }
    return username
  }
}
