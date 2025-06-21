package com.tianqueal.flowjet.backend.services.impl

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

  @Value(value = "\${info.app.name:myapp}")
  private var appName: String,
  @Value("\${info.app.mail.from:noreply@example.com}")
  private var from: String,
  @Value("\${info.app.mail.fromName:Sender Name}")
  private var fromName: String,
  @Value("\${info.app.frontend.base-url:http://localhost:3000}")
  private var frontendBaseUrl: String,
) : EmailService {
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

  override fun sendEmailVerification(to: String, name: String, token: String, locale: Locale, apiVersionPath: String) {
    val verificationUrl = UriComponentsBuilder
      .fromUriString(frontendBaseUrl)
      .path("${apiVersionPath}${ApiPaths.AUTH}${ApiPaths.VERIFY_EMAIL}")
      .queryParam("token", token)
      .toUriString()

    val model = mutableMapOf<String, Any>(
      "appName" to appName,
      "name" to name,
      "verificationUrl" to verificationUrl
    )
    val subject = messageSource.getMessage(MessageKeys.EMAIL_VERIFICATION_SUBJECT, null, locale)

    sendEmailFromTemplate(to, subject, TemplatePaths.EMAIL_VERIFICATION, model, locale)
  }
}
