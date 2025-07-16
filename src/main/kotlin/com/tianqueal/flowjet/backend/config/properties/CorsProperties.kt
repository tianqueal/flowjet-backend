package com.tianqueal.flowjet.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "info.app.cors")
class CorsProperties {
    var allowedOrigins: List<String> = mutableListOf()
}
