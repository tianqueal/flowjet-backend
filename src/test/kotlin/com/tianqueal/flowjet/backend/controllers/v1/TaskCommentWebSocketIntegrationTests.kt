package com.tianqueal.flowjet.backend.controllers.v1

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.builders.TaskCommentTestData
import com.tianqueal.flowjet.backend.builders.TaskCommentTestDataBuilder
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.websocket.WebSocketEvent
import com.tianqueal.flowjet.backend.helpers.WebSocketTestHelper
import com.tianqueal.flowjet.backend.utils.assertThat
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import com.tianqueal.flowjet.backend.utils.enums.WebSocketEventType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DisplayName("WebSocket Task Comment Topic Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = ["/cleanup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TaskCommentWebSocketIntegrationTests
    @Autowired
    constructor(
        private val webSocketTestHelper: WebSocketTestHelper,
    ) : AbstractTaskControllerTest() {
        @LocalServerPort
        private var port: Int = 0

        private lateinit var wsUrl: String
        private lateinit var ownerToken: String
        private lateinit var memberToken: String
        private lateinit var project: ProjectResponse
        private lateinit var task: TaskResponse
        private lateinit var member: UserResponse

        @BeforeEach
        fun taskCommentWebSocketIntegrationTestsSetUp() {
            wsUrl = "ws://localhost:$port${ApiPaths.WS_ENDPOINT}/websocket"

            val ownerPair = createTestUserAndGetToken("project.owner")
            ownerToken = ownerPair.second

            val memberPair = createTestUserAndGetToken("project.member")
            member = memberPair.first
            memberToken = memberPair.second

            project = createTestProject(ownerToken)
            inviteAndAcceptMember(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
            task = createTestTask(ownerToken, project.id)

            TestTransaction.flagForCommit()
            TestTransaction.end()
            TestTransaction.start()
        }

        @Test
        fun `authorized member receives all comment events`() {
            // Arrange
            val testData =
                TaskCommentTestDataBuilder()
                    .withProject(project.id)
                    .withTask(task.id)
                    .withContent("Live comment")
                    .build()

            val wsSession = webSocketTestHelper.createAuthenticatedSession(memberToken, testData.topic, wsUrl)

            // Act & Capture
            val createdComment = createTaskComment(memberToken, testData)
            updateTaskComment(memberToken, testData, createdComment.id)
            deleteTaskComment(memberToken, testData, createdComment.id)

            // Assert
            val events = wsSession.waitForEvents(3)

            assertThat(events)
                .hasSize(3)
                .containsEventTypes(
                    WebSocketEventType.TASK_COMMENT_CREATED,
                    WebSocketEventType.TASK_COMMENT_UPDATED,
                    WebSocketEventType.TASK_COMMENT_DELETED,
                ).containsEventsWithIds(createdComment.id, createdComment.id, createdComment.id)

            // Verify specific event content
            val (createdEvent, updatedEvent, deletedEvent) = events
            assertEquals("Live comment", extractCommentContent(createdEvent))
            assertEquals("Updated content", extractCommentContent(updatedEvent))
            assertEquals(createdComment.id, extractCommentId(deletedEvent))

            wsSession.disconnect()
        }

        @Test
        fun `outsider does not receive events when others publish`() {
            // Arrange
            val (_, outsiderToken) = createTestUserAndGetToken("outsider")
            val testData =
                TaskCommentTestDataBuilder()
                    .withProject(project.id)
                    .withTask(task.id)
                    .build()

            val outsiderSession = webSocketTestHelper.createAuthenticatedSession(outsiderToken, testData.topic, wsUrl)

            // Act
            createTaskComment(memberToken, testData)

            // Assert
            val receivedEvent = outsiderSession.waitForEvent(5)
            assertNull(receivedEvent, "Outsider should not have received any event, but received: $receivedEvent")

            outsiderSession.disconnect()
        }

        private fun createTaskComment(
            token: String,
            testData: TaskCommentTestData,
            parentId: Long? = null,
        ): TaskCommentResponse {
            val request = CreateTaskCommentRequest(testData.taskCommentContent, parentId)
            val result =
                mockMvc
                    .post(testData.createUri) {
                        header(HttpHeaders.AUTHORIZATION, token)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isCreated() } }
                    .andReturn()

            return objectMapper.readValue(result.response.contentAsByteArray, TaskCommentResponse::class.java)
        }

        private fun updateTaskComment(
            token: String,
            testData: TaskCommentTestData,
            commentId: Long,
            taskContent: String = "Updated content",
        ) {
            val request = UpdateTaskCommentRequest(taskContent)
            mockMvc
                .put(testData.buildTaskCommentUri(commentId)) {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(request)
                }.andExpect { status { isOk() } }
        }

        private fun deleteTaskComment(
            token: String,
            testData: TaskCommentTestData,
            commentId: Long,
        ) {
            mockMvc
                .delete(testData.buildTaskCommentUri(commentId)) {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isNoContent() } }
        }

        private fun extractCommentContent(event: WebSocketEvent<*>): String? =
            when (val payload = event.payload) {
                is Map<*, *> -> payload["content"]?.toString()
                is TaskCommentResponse -> payload.content
                else -> null
            }

        private fun extractCommentId(event: WebSocketEvent<*>): Long? =
            when (val payload = event.payload) {
                is Map<*, *> -> payload["id"]?.toString()?.toLongOrNull()
                is TaskCommentResponse -> payload.id
                else -> null
            }

        companion object {
            @JvmField
            @RegisterExtension
            @Suppress("unused")
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)
        }
    }
