package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

interface EmailService {
    fun sendEmailVerification(
        to: String,
        name: String,
        token: String,
        locale: Locale = LocaleContextHolder.getLocale(),
    )

    fun sendPasswordResetEmail(
        to: String,
        name: String,
        token: String,
        locale: Locale = LocaleContextHolder.getLocale(),
    )

    fun sendProjectMemberInvitation(
        to: String,
        name: String,
        projectEntity: ProjectEntity,
        token: String,
        locale: Locale = LocaleContextHolder.getLocale(),
    )
}
