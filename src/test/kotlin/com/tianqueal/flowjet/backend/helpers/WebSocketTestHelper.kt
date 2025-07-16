package com.tianqueal.flowjet.backend.helpers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tianqueal.flowjet.backend.domain.dto.v1.websocket.WebSocketEvent
import org.springframework.http.HttpHeaders
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Component
class WebSocketTestHelper(
    private val objectMapper: ObjectMapper,
) {
    private val wsClient by lazy {
        WebSocketStompClient(StandardWebSocketClient()).apply {
            messageConverter = MappingJackson2MessageConverter(objectMapper)

            val scheduler = ThreadPoolTaskScheduler()
            scheduler.poolSize = 1
            scheduler.setThreadNamePrefix("test-ws-")
            scheduler.initialize()

            taskScheduler = scheduler
        }
    }

    fun createAuthenticatedSession(
        token: String,
        topic: String,
        wsUrl: String,
    ): AuthenticatedWebSocketSession {
        val queue = ArrayBlockingQueue<WebSocketEvent<*>>(10)
        val connectionLatch = CountDownLatch(1)
        val subscriptionLatch = CountDownLatch(1)

        val session =
            wsClient
                .connectAsync(
                    wsUrl,
                    WebSocketHttpHeaders(),
                    StompHeaders().apply { add(HttpHeaders.AUTHORIZATION, token) },
                    object : StompSessionHandlerAdapter() {
                        override fun afterConnected(
                            session: StompSession,
                            connectedHeaders: StompHeaders,
                        ) {
                            connectionLatch.countDown()
                            try {
                                session.subscribe(topic, WebSocketEventFrameHandler(queue))
                                subscriptionLatch.countDown()
                            } catch (e: Exception) {
                                throw WebSocketConnectionException("Subscription failed: ${e.message}", e)
                            }
                        }

                        override fun handleException(
                            session: StompSession,
                            command: StompCommand?,
                            headers: StompHeaders,
                            payload: ByteArray,
                            exception: Throwable,
                        ): Unit = throw WebSocketConnectionException("WebSocket exception: ${exception.message}", exception)
                    },
                ).get(10, TimeUnit.SECONDS)

        if (!connectionLatch.await(10, TimeUnit.SECONDS)) {
            throw WebSocketConnectionException("Failed to connect within timeout")
        }

        if (!subscriptionLatch.await(10, TimeUnit.SECONDS)) {
            throw WebSocketConnectionException("Failed to subscribe within timeout")
        }

        Thread.sleep(500)

        return AuthenticatedWebSocketSession(session, queue)
    }
}

data class AuthenticatedWebSocketSession(
    val session: StompSession,
    val eventQueue: ArrayBlockingQueue<WebSocketEvent<*>>,
) {
    fun waitForEvent(timeoutSeconds: Long = 10): WebSocketEvent<*>? = eventQueue.poll(timeoutSeconds, TimeUnit.SECONDS)

    fun waitForEvents(
        count: Int,
        timeoutSeconds: Long = 10,
    ): List<WebSocketEvent<*>> {
        val events = mutableListOf<WebSocketEvent<*>>()
        repeat(count) {
            val event =
                eventQueue.poll(timeoutSeconds, TimeUnit.SECONDS)
                    ?: throw AssertionError("Expected $count events but received only ${events.size}")
            events.add(event)
        }
        return events
    }

    fun disconnect() {
        if (session.isConnected) {
            session.disconnect()
        }
    }
}

private class WebSocketEventFrameHandler(
    private val queue: ArrayBlockingQueue<WebSocketEvent<*>>,
) : StompFrameHandler {
    override fun getPayloadType(headers: StompHeaders): Type = object : TypeReference<WebSocketEvent<*>>() {}.type

    override fun handleFrame(
        headers: StompHeaders,
        payload: Any?,
    ) {
        if (payload is WebSocketEvent<*>) {
            queue.offer(payload)
        }
    }
}

class WebSocketConnectionException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
