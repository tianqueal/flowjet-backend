package com.tianqueal.flowjet.backend.utils.constants

object SecurityConstants {
  const val DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS: Long = 24 * 60 * 60L // 1 day
  const val EXTENDED_ACCESS_TOKEN_EXPIRATION_SECONDS: Long = 7 * 24 * 60 * 60L // 7 days
  const val DEFAULT_EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS: Long = 60 * 60L // 1 hour
  const val DEFAULT_PASSWORD_RESET_TOKEN_EXPIRATION_SECONDS: Long = 60 * 60L // 1 hour

  const val BEARER_FORMAT = "JWT"
  const val SECURITY_SCHEME_BEARER = "bearerAuth"
  const val HTTP_AUTH_SCHEME_BEARER = "Bearer"

  const val TOKEN_PURPOSE_ACCESS = "access_token"
  const val TOKEN_PURPOSE_EMAIL_VERIFICATION = "email_verification"
  const val TOKEN_PURPOSE_PASSWORD_RESET = "password_reset"

  const val AUTHORITIES_CLAIM_NAME = "authorities"
  const val AUTHORITIES_PREFIX = ""
}
