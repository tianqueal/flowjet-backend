package com.tianqueal.flowjet.backend.utils

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.websocket.WebSocketEvent
import com.tianqueal.flowjet.backend.utils.enums.WebSocketEventType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun <T> assertThat(actual: List<T>): ListAssert<T> = ListAssert(actual)

class ListAssert<T>(
    private val actual: List<T>,
) {
    fun hasSize(expected: Int): ListAssert<T> {
        assertEquals(expected, actual.size, "Expected list size $expected but was ${actual.size}")
        return this
    }

    fun containsEventTypes(vararg expectedTypes: WebSocketEventType): ListAssert<T> {
        require(actual.all { it is WebSocketEvent<*> }) { "All items must be WebSocketEvent instances" }

        val actualTypes = actual.map { (it as WebSocketEvent<*>).eventType }
        val expectedTypesList = expectedTypes.toList()

        assertEquals(
            expectedTypesList.size,
            actualTypes.size,
            "Expected ${expectedTypesList.size} events but received ${actualTypes.size}",
        )

        expectedTypesList.forEachIndexed { index, expectedType ->
            assertEquals(
                expectedType,
                actualTypes[index],
                "Event at index $index: expected $expectedType but was ${actualTypes[index]}",
            )
        }

        return this
    }

    fun containsEventsWithIds(vararg expectedIds: Long): ListAssert<T> {
        require(actual.all { it is WebSocketEvent<*> }) { "All items must be WebSocketEvent instances" }

        val actualIds =
            actual.mapNotNull { event ->
                val payload = (event as WebSocketEvent<*>).payload
                when (payload) {
                    is Map<*, *> -> payload["id"]?.toString()?.toLongOrNull()
                    is TaskCommentResponse -> payload.id
                    else -> null
                }
            }

        expectedIds.forEach { expectedId ->
            assertTrue(
                actualIds.contains(expectedId),
                "Expected to find event with ID $expectedId but found IDs: $actualIds",
            )
        }

        return this
    }
}
