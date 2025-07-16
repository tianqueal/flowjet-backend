package com.tianqueal.flowjet.backend.config

import com.tianqueal.flowjet.backend.config.properties.CorsProperties
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val corsProperties: CorsProperties,
    private val projectPermissionService: ProjectPermissionService,
    private val jwtChannelInterceptor: JwtChannelInterceptor,
) : WebSocketMessageBrokerConfigurer {
    @Bean
    fun messageAuthorizationManager(): AuthorizationManager<Message<*>> {
        val messages = MessageMatcherDelegatingAuthorizationManager.builder()
        return messages
            .simpTypeMatchers(
                SimpMessageType.CONNECT,
                SimpMessageType.HEARTBEAT,
                SimpMessageType.UNSUBSCRIBE,
                SimpMessageType.DISCONNECT,
            ).permitAll()
            .simpSubscribeDestMatchers("${ApiPaths.WS_TOPIC_PREFIX}${ApiPaths.PROJECTS}/**")
            .access { authentication, context ->
                val auth = authentication.get()
                val message = context.message

                val simpDestination =
                    message.headers["simpDestination"] as? String
                        ?: return@access AuthorizationDecision(false)

                val projectId =
                    PROJECT_TOPIC_PATTERN
                        .find(simpDestination)
                        ?.groupValues
                        ?.get(1)
                        ?.toLongOrNull()
                        ?: return@access AuthorizationDecision(false)

                val userId =
                    auth.name?.toLongOrNull()
                        ?: return@access AuthorizationDecision(false)

                val hasPermission = projectPermissionService.canRead(projectId, userId)

                AuthorizationDecision(hasPermission)
            }.anyMessage()
            .denyAll()
            .build()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            jwtChannelInterceptor,
            SecurityContextChannelInterceptor(),
            AuthorizationChannelInterceptor(messageAuthorizationManager()),
        )
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint(ApiPaths.WS_ENDPOINT)
            .setAllowedOriginPatterns(*corsProperties.allowedOrigins.toTypedArray())
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker(ApiPaths.WS_TOPIC_PREFIX)
        registry.setApplicationDestinationPrefixes(ApiPaths.WS_APP_PREFIX)
    }

    companion object {
        private val PROJECT_TOPIC_PATTERN =
            "${ApiPaths.WS_TOPIC_PREFIX}${ApiPaths.PROJECTS}/(\\d+)${ApiPaths.TASKS}/(\\d+)${ApiPaths.COMMENTS}".toRegex()
    }
}
