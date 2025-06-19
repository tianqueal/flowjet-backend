package com.tianqueal.flowjet.backend.utils.constants

object SecurityConstants {
  const val DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS: Long = 24 * 60 * 60L // 1 day
  const val EXTENDED_ACCESS_TOKEN_EXPIRATION_SECONDS: Long = 7 * 24 * 60 * 60L // 7 days
  const val DEFAULT_EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS: Long = 60 * 60L // 1 hour
  const val ACCESS_TOKEN_TYPE = "Bearer"
  const val SECURITY_SCHEME_BEARER = "bearerAuth"
}
