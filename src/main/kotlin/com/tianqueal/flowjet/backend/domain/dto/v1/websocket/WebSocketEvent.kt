package com.tianqueal.flowjet.backend.domain.dto.v1.websocket

import com.tianqueal.flowjet.backend.utils.enums.WebSocketEventType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "WebSocket event payload")
data class WebSocketEvent<T>(
    @field:Schema(description = "Type of the WebSocket event")
    val eventType: WebSocketEventType,
    @field:Schema(description = "Payload of the WebSocket event")
    val payload: T,
)
