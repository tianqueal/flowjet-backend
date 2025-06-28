package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.services.EmailService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import com.tianqueal.flowjet.backend.utils.constants.TemplatePaths
import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.Locale

@Service
class EmailServiceImpl(
    private val javaMailSender: JavaMailSender,
    private val freeMarkerConfig: Configuration,
    private val messageSource: MessageSource,
    @Value("\${info.app.name:myapp}")
    private var appName: String,
    @Value("\${info.app.mail.from:noreply@example.com}")
    private var from: String,
    @Value("\${info.app.mail.fromName:Sender Name}")
    private var fromName: String,
    @Value("\${info.app.frontend.base-url:http://localhost:3000}")
    private var frontendBaseUrl: String,
) : EmailService {
    /**
     * Sends an email verification message to the user.
     * @param to The recipient's email address.
     * @param name The name of the user.
     * @param token The verification token.
     * @param locale The locale for the email content.
     * @param apiVersionPath The API version path to use in the verification URL.
     */
    override fun sendEmailVerification(
        to: String,
        name: String,
        token: String,
        locale: Locale,
        apiVersionPath: String,
    ) {
        val verificationUrl =
            UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("${apiVersionPath}${ApiPaths.AUTH}${ApiPaths.VERIFY_EMAIL}")
                .queryParam("token", token)
                .toUriString()

        val model =
            mutableMapOf<String, Any>(
                "appName" to appName,
                "name" to name,
                "verificationUrl" to verificationUrl,
            )
        val subject = messageSource.getMessage(MessageKeys.EMAIL_VERIFICATION_SUBJECT, null, locale)

        sendEmailFromTemplate(to, subject, TemplatePaths.EMAIL_VERIFICATION, model, locale)
    }

    /**
     * Sends a password reset email to the user.
     * @param to The recipient's email address.
     * @param name The name of the user.
     * @param token The password reset token.
     * @param locale The locale for the email content.
     * @param apiVersionPath The API version path to use in the password reset URL.
     */
    override fun sendPasswordResetEmail(
        to: String,
        name: String,
        token: String,
        locale: Locale,
        apiVersionPath: String,
    ) {
        val passwordResetUrl =
            UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("${apiVersionPath}${ApiPaths.AUTH}${ApiPaths.PASSWORD_RESET}")
                .pathSegment("confirm")
                .queryParam("token", token)
                .toUriString()

        val model =
            mutableMapOf<String, Any>(
                "appName" to appName,
                "name" to name,
                "passwordResetUrl" to passwordResetUrl,
            )
        val subject = messageSource.getMessage(MessageKeys.EMAIL_PASSWORD_RESET_SUBJECT, null, locale)

        sendEmailFromTemplate(to, subject, TemplatePaths.EMAIL_PASSWORD_RESET, model, locale)
    }

    /**
     * Sends a project member invitation email.
     * @param to The recipient's email address.
     * @param name The name of the user being invited.
     * @param projectEntity The project entity to which the user is being invited.
     * @param token The invitation token.
     * @param locale The locale for the email content.
     * @param apiVersionPath The API version path to use in the invitation URL.
     */
    override fun sendProjectMemberInvitation(
        to: String,
        name: String,
        projectEntity: ProjectEntity,
        token: String,
        locale: Locale,
        apiVersionPath: String,
    ) {
        val projectMemberInvitationUrl =
            UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("${apiVersionPath}${ApiPaths.PROJECTS}/${projectEntity.id}${ApiPaths.MEMBERS}")
                .pathSegment("/accept-invitation")
                .queryParam("token", token)
                .toUriString()

        val model =
            mutableMapOf<String, Any>(
                "appName" to appName,
                "name" to name,
                "projectName" to projectEntity.name,
                "projectMemberInvitationUrl" to projectMemberInvitationUrl,
            )
        val subject = messageSource.getMessage(MessageKeys.EMAIL_PROJECT_MEMBER_INVITATION_SUBJECT, null, locale)

        sendEmailFromTemplate(to, subject, TemplatePaths.EMAIL_PROJECT_MEMBER_INVITATION, model, locale)
    }

    /**
     * Sends an email using a FreeMarker template.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param templateName The name of the FreeMarker template to use.
     * @param model The model data to populate the template.
     * @param locale The locale for the email content.
     */
    private fun sendEmailFromTemplate(
        to: String,
        subject: String,
        templateName: String,
        model: MutableMap<String, Any>,
        locale: Locale,
    ) {
        model["locale"] = locale
        model["subject"] = subject
        model["frontendBaseUrl"] = frontendBaseUrl

        val template = freeMarkerConfig.getTemplate(templateName, locale)
        val stringWriter = StringWriter()
        template.process(model, stringWriter)
        val htmlBody = stringWriter.toString()

        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, StandardCharsets.UTF_8.name())
        helper.setFrom(from, fromName)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlBody, true)

        javaMailSender.send(message)
    }
}
