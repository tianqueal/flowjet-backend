package com.tianqueal.flowjet.backend.config

import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @param:Value($$"${info.app.name:myapp}")
    private var apiTitle: String,
    @param:Value($$"${info.app.version:1.0.0}")
    private var apiVersion: String,
    @param:Value($$"${info.app.description:My Application}")
    private var apiDescription: String,
    @param:Value($$"${info.app.author:tianqueal}")
    private var contactName: String,
    @param:Value($$"${info.app.contactEmail:user@example.com}")
    private var contactEmail: String,
    @param:Value($$"${info.app.contactUrl:https://example.com}")
    private var contactUrl: String,
    @param:Value($$"${info.app.license:MIT}")
    private var licenseName: String,
    @param:Value($$"${info.app.licenseUrl:https://opensource.org/license/mit}")
    private var licenseUrl: String,
    @param:Value($$"${info.app.server.dev.url:http://localhost:8080}")
    private var devServerUrl: String,
    @param:Value($$"${info.app.server.dev.description:Local Development Server}")
    private var devServerDescription: String,
    @param:Value($$"${info.app.server.prod.url:http://localhost:8080}")
    private var prodServerUrl: String,
    @param:Value($$"${info.app.server.prod.description:Production Server}")
    private var prodServerDescription: String,
) {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title(apiTitle)
                    .version(apiVersion)
                    .description(apiDescription)
                    .contact(
                        Contact().name(contactName).email(contactEmail).url(contactUrl),
                    ).license(License().name(licenseName).url(licenseUrl)),
            ).servers(
                listOf(
                    Server().url(devServerUrl).description(devServerDescription),
                    Server().url(prodServerUrl).description(prodServerDescription),
                ),
            ).components(
                Components()
                    .addSecuritySchemes(
                        SecurityConstants.SECURITY_SCHEME_BEARER,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme(SecurityConstants.HTTP_AUTH_SCHEME_BEARER.lowercase())
                            .bearerFormat(SecurityConstants.BEARER_FORMAT)
                            .`in`(SecurityScheme.In.HEADER)
                            .description(
                                "JWT Authorization header using the ${SecurityConstants.HTTP_AUTH_SCHEME_BEARER} scheme. Example: '${SecurityConstants.HTTP_AUTH_SCHEME_BEARER} {token}'",
                            ),
                    ),
            )
}
