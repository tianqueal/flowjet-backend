package com.tianqueal.flowjet.backend.services

import java.util.Locale

interface EmailService {
  fun sendEmailVerification(to: String, name: String, token: String, locale: Locale, apiVersionPath: String)
}
