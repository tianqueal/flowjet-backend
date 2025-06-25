package com.tianqueal.flowjet.backend.services

import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

interface EmailService {
    fun sendEmailVerification(
        to: String,
        name: String,
        token: String,
        locale: Locale = LocaleContextHolder.getLocale(),
        apiVersionPath: String,
    )

    fun sendPasswordResetEmail(
        to: String,
        name: String,
        token: String,
        locale: Locale = LocaleContextHolder.getLocale(),
        apiVersionPath: String,
    )
}
