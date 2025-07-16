package com.tianqueal.flowjet.backend.config

import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class JwtChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
) : ChannelInterceptor {
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*>? {
        val accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                ?: return message

        when (accessor.command) {
            StompCommand.CONNECT -> handleConnect(accessor)
            StompCommand.SUBSCRIBE -> handleSubscribe(accessor)
            else -> {}
        }
        return message
    }

    private fun handleConnect(accessor: StompHeaderAccessor) {
        val authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.startsWith("${SecurityConstants.HTTP_AUTH_SCHEME_BEARER} ")) {
            val jwt = authHeader.substring(7)
            if (jwtTokenProvider.validateToken(jwt)) {
                val authentication = jwtTokenProvider.getAuthentication(jwt)
                accessor.user = authentication
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
    }

    private fun handleSubscribe(accessor: StompHeaderAccessor) {
        if (accessor.user is Authentication) {
            SecurityContextHolder.getContext().authentication = accessor.user as Authentication
        }
    }
}
