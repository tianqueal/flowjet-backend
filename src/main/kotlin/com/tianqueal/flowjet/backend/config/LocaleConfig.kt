package com.tianqueal.flowjet.backend.config

import com.tianqueal.flowjet.backend.utils.constants.LanguageConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver

@Configuration
class LocaleConfig() {
  @Bean
  fun localeResolver(): LocaleResolver {
    val resolver = AcceptHeaderLocaleResolver()
    resolver.setDefaultLocale(LanguageConstants.DEFAULT_LOCALE)
    resolver.supportedLocales = LanguageConstants.SUPPORTED_LOCALES
    return resolver
  }
}
