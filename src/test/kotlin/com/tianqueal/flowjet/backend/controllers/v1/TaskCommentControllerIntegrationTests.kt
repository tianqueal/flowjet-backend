package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.core.type.TypeReference
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.common.PageResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskCommentRequest
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.TaskCommentRepository
import com.tianqueal.flowjet.backend.repositories.TaskRepository
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@DisplayName("TaskComment Controller Integration Tests")
class TaskCommentControllerIntegrationTests
    @Autowired
    constructor(
        private val taskRepository: TaskRepository,
        private val projectRepository: ProjectRepository,
        private val taskCommentRepository: TaskCommentRepository,
    ) : AbstractTaskControllerTest() {
        @BeforeEach
        fun setUp() {
            taskCommentRepository.deleteAll()
            taskRepository.deleteAll()
            projectRepository.deleteAll()
        }

        @Nested
        @DisplayName("Get Task Comments Tests")
        inner class GetTaskCommentsTests {
            @Test
            fun `getComments with project member permission returns paginated comments with replies`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")
                val project = createTestProject(ownerToken)
                inviteAndAcceptMember(ownerToken, project.id, member.id)
                val task = createTestTask(ownerToken, project.id)
                val rootComment = createTaskComment(memberToken, project.id, task.id, "Root comment")
                createTaskComment(ownerToken, project.id, task.id, "Reply to root", rootComment.id)

                // Act
                val result =
                    mockMvc
                        .get(buildTaskCommentUri(project.id, task.id)) {
                            header(HttpHeaders.AUTHORIZATION, memberToken)
                            queryParam("s", "5")
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val response: PageResponse<TaskCommentResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        object : TypeReference<PageResponse<TaskCommentResponse>>() {},
                    )
                assertEquals(1, response.totalElements)
                val returnedRoot = response.content.first()
                assertEquals("Root comment", returnedRoot.content)
                assertEquals(1, returnedRoot.replies.size)
                assertEquals("Reply to root", returnedRoot.replies.first().content)
            }

            @Test
            fun `getComments with outsider should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, outsiderToken) = createTestUserAndGetToken("outsider")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id)

                // Act & Assert
                mockMvc
                    .get(buildTaskCommentUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                    }.andExpect { status { isForbidden() } }
            }

//        @ParameterizedTest
//        @ValueSource(strings = ["awesome", "comment"])
//        fun `getComments should filter by content correctly`(keyword: String) {
//            // Arrange
//            val (_, ownerToken) = createTestUserAndGetToken("project.owner")
//            val project = createTestProject(ownerToken)
//            val task = createTestTask(ownerToken, project.id)
//            createTaskComment(ownerToken, project.id, task.id, "This is an awesome comment")
//            createTaskComment(ownerToken, project.id, task.id, "This is a different one")
//
//            // Act
//            val result = mockMvc.get(buildTaskCommentUri(project.id, task.id)) {
//                header(HttpHeaders.AUTHORIZATION, ownerToken)
//                queryParam("content", keyword)
//            }.andExpect { status { isOk() } }.andReturn()
//
//            // Assert
//            val response: PageResponse<TaskCommentResponse> = objectMapper.readValue(
//                result.response.contentAsByteArray,
//                object : TypeReference<PageResponse<TaskCommentResponse>>() {})
//            assertEquals(1, response.totalElements)
//            assertTrue(response.content.first().content.contains(keyword))
//        }
        }

        @Nested
        @DisplayName("Create Task Comment Tests")
        inner class CreateCommentTests {
            @Test
            fun `createComment with valid data by project member should return Created`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")
                val project = createTestProject(ownerToken)
                inviteAndAcceptMember(ownerToken, project.id, member.id)
                val task = createTestTask(ownerToken, project.id)
                val request = CreateTaskCommentRequest(content = "New comment from member")

                // Act
                mockMvc
                    .post(buildTaskCommentUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isCreated() } }

                // Assert
                assertEquals(1, taskCommentRepository.count())
            }

            @Test
            fun `createComment by non-project-member should return UnprocessableEntity`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, strangerToken) = createTestUserAndGetToken("stranger")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id)
                val request = CreateTaskCommentRequest(content = "Should not work")

                // Act & Assert
                mockMvc
                    .post(buildTaskCommentUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, strangerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isUnprocessableEntity() } }
            }

            @Test
            fun `createComment exceeding nesting depth should return UnprocessableEntity`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id)
                var parentId: Long? = null
                // Create a chain of comments up to the limit
                repeat(5) {
                    val parentComment =
                        createTaskComment(ownerToken, project.id, task.id, "Comment level ${it + 1}", parentId)
                    parentId = parentComment.id
                }
                val request = CreateTaskCommentRequest(content = "This comment is too deep", parentId = parentId)

                // Act & Assert
                mockMvc
                    .post(buildTaskCommentUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isUnprocessableEntity() } }
            }
        }

        @Nested
        @DisplayName("Update & Delete Task Comment Tests")
        inner class UpdateAndDeleteCommentTests {
            @Test
            fun `author can update their own comment`() {
                // Arrange
                val (_, authorToken) = createTestUserAndGetToken("comment.author")
                val project = createTestProject(authorToken)
                val task = createTestTask(authorToken, project.id)
                val comment = createTaskComment(authorToken, project.id, task.id, "Original content")
                val request = UpdateTaskCommentRequest(content = "Updated content")

                // Act
                mockMvc
                    .put(buildTaskCommentUri(project.id, task.id, comment.id)) {
                        header(HttpHeaders.AUTHORIZATION, authorToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isOk() } }

                // Assert
                val updatedComment = taskCommentRepository.findById(comment.id).orElseThrow()
                assertEquals("Updated content", updatedComment.content)
            }

            @Test
            fun `project owner can delete any comment`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (author, authorToken) = createTestUserAndGetToken("comment.author")
                val project = createTestProject(ownerToken)
                inviteAndAcceptMember(ownerToken, project.id, author.id)
                val task = createTestTask(ownerToken, project.id)
                val comment = createTaskComment(authorToken, project.id, task.id, "A comment")

                // Act & Assert
                mockMvc
                    .delete(buildTaskCommentUri(project.id, task.id, comment.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                assertFalse(taskCommentRepository.existsById(comment.id))
            }

            @Test
            fun `non-author member cannot update another member's comment`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (author, authorToken) = createTestUserAndGetToken("comment.author")
                val (otherMember, otherMemberToken) = createTestUserAndGetToken("other.member")
                val project = createTestProject(ownerToken)
                inviteAndAcceptMember(ownerToken, project.id, author.id)
                inviteAndAcceptMember(ownerToken, project.id, otherMember.id)
                val task = createTestTask(ownerToken, project.id)
                val comment = createTaskComment(authorToken, project.id, task.id, "A comment")
                val request = UpdateTaskCommentRequest(content = "Updated content")

                // Act & Assert
                mockMvc
                    .put(buildTaskCommentUri(project.id, task.id, comment.id)) {
                        header(HttpHeaders.AUTHORIZATION, otherMemberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `non-author member cannot delete another member's comment`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (author, authorToken) = createTestUserAndGetToken("comment.author")
                val (otherMember, otherMemberToken) = createTestUserAndGetToken("other.member")
                val project = createTestProject(ownerToken)
                inviteAndAcceptMember(ownerToken, project.id, author.id)
                inviteAndAcceptMember(ownerToken, project.id, otherMember.id)
                val task = createTestTask(ownerToken, project.id)
                val comment = createTaskComment(authorToken, project.id, task.id, "A comment")

                // Act & Assert
                mockMvc
                    .delete(buildTaskCommentUri(project.id, task.id, comment.id)) {
                        header(HttpHeaders.AUTHORIZATION, otherMemberToken)
                    }.andExpect { status { isForbidden() } }
            }
        }

        // =================================================================
        // Helper Methods
        // =================================================================
        private fun createTaskComment(
            token: String,
            projectId: Long,
            taskId: Long,
            taskContent: String,
            parentId: Long? = null,
        ): TaskCommentResponse {
            val request = CreateTaskCommentRequest(taskContent, parentId)
            val result =
                mockMvc
                    .post(buildTaskCommentUri(projectId, taskId)) {
                        header(HttpHeaders.AUTHORIZATION, token)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(request)
                    }.andExpect { status { isCreated() } }
                    .andReturn()
            return objectMapper.readValue(result.response.contentAsByteArray, TaskCommentResponse::class.java)
        }

        private fun buildTaskCommentUri(
            projectId: Long,
            taskId: Long,
        ): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}/$taskId${ApiPaths.COMMENTS}"

        private fun buildTaskCommentUri(
            projectId: Long,
            taskId: Long,
            commentId: Long,
        ): String = "${buildTaskCommentUri(projectId, taskId)}/$commentId"

        companion object {
            @JvmField
            @RegisterExtension
            @Suppress("unused")
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)
        }
    }
