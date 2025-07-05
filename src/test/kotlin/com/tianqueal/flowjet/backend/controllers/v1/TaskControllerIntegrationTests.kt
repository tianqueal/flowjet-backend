package com.tianqueal.flowjet.backend.controllers.v1

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskRequest
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.TaskRepository
import com.tianqueal.flowjet.backend.repositories.TaskStatusRepository
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("Task Controller Integration Tests")
class TaskControllerIntegrationTests
    @Autowired
    constructor(
        private val taskRepository: TaskRepository,
        private val projectRepository: ProjectRepository,
        private val taskStatusRepository: TaskStatusRepository,
    ) : AbstractProjectControllerTest() {
        @BeforeEach
        fun setUp() {
            taskRepository.deleteAll()
            projectRepository.deleteAll()
        }

        // ===================================
        // GET /projects/{projectId}/tasks
        // ===================================

        @Nested
        @DisplayName("Get All Tasks Tests")
        inner class GetAllTasksTests {
            @Test
            fun `getAllTasks with project owner should return OK and paginated tasks`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                repeat(3) { i ->
                    createTestTask(ownerToken, project.id, "Task $i", "Description $i")
                }

                // Act
                val result =
                    mockMvc
                        .get(buildTasksUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":3"))
                assertTrue(responseContent.contains("\"content\""))
            }

            @Test
            fun `getAllTasks with project member should return OK and task list`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                assertEquals(1, greenMail.receivedMessages.size, "Expected one invitation email.")
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                createTestTask(ownerToken, project.id, "Task 1", "Description 1")

                // Act
                mockMvc
                    .get(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isOk() } }
            }

            @Test
            fun `getAllTasks with project viewer should return OK and task list`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (viewer, viewerToken) = createTestUserAndGetToken("project.viewer")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                createTestTask(ownerToken, project.id, "Task 1", "Description 1")

                // Act
                mockMvc
                    .get(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, viewerToken)
                    }.andExpect { status { isOk() } }
            }

            @Test
            fun `getAllTasks with outsider should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, outsiderToken) = createTestUserAndGetToken("outsider")
                val project = createTestProject(ownerToken)

                // Act & Assert
                mockMvc
                    .get(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                    }.andExpect { status { isForbidden() } }
            }

            @ParameterizedTest
            @ValueSource(strings = ["Task Alpha", "Task Beta", "Task Gamma"])
            fun `getAllTasks should filter by task name correctly`(taskName: String) {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                createTestTask(ownerToken, project.id, taskName, "Description")
                createTestTask(ownerToken, project.id, "Other Task", "Other Description")

                // Act
                val result =
                    mockMvc
                        .get(buildTasksUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("name", taskName)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains(taskName))
                assertTrue(responseContent.contains("\"totalElements\":1"))
            }

            @Test
            fun `getAllTasks with pagination should respect page parameters`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                repeat(10) { i ->
                    createTestTask(ownerToken, project.id, "Task $i", "Description $i")
                }

                // Act
                val result =
                    mockMvc
                        .get(buildTasksUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("p", "0")
                            param("s", "5")
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"size\":5"))
                assertTrue(responseContent.contains("\"totalElements\":10"))
            }
        }

        // ===================================
        // GET /projects/{projectId}/tasks/{id}
        // ===================================

        @Nested
        @DisplayName("Get Task By ID Tests")
        inner class GetTaskByIdTests {
            @Test
            fun `getTaskById with valid task and owner permission should return OK`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task", "Test Description")

                // Act
                val result =
                    mockMvc
                        .get(buildTaskUri(project.id, task.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val taskResponse =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        TaskResponse::class.java,
                    )
                assertEquals("Test Task", taskResponse.name)
                assertEquals("Test Description", taskResponse.description)
            }

            @Test
            fun `getTaskById with invalid task ID should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                // Act & Assert
                mockMvc
                    .get(buildTaskUri(project.id, 99999L)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `getTaskById with task from different project should return NotFound`() {
                // Arrange
                val (_, owner1Token) = createTestUserAndGetToken("project.owner1")
                val (_, owner2Token) = createTestUserAndGetToken("project.owner2")
                val project1 = createTestProject(owner1Token)
                val project2 = createTestProject(owner2Token)
                val task = createTestTask(owner1Token, project1.id, "Task", "Description")

                // Act & Assert - Trying to access task from project1 using project2 ID
                mockMvc
                    .get(buildTaskUri(project2.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, owner2Token)
                    }.andExpect { status { isNotFound() } }
            }
        }

        // ===================================
        // POST /projects/{projectId}/tasks
        // ===================================

        @Nested
        @DisplayName("Create Task Tests")
        inner class CreateTaskTests {
            @Test
            fun `createTask with valid data and owner permission should return Created`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val createRequest =
                    CreateTaskRequest(
                        name = "New Task",
                        description = "New Task Description",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act
                val result =
                    mockMvc
                        .post(buildTasksUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(createRequest)
                        }.andExpect { status { isCreated() } }
                        .andReturn()

                // Assert
                val taskResponse =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        TaskResponse::class.java,
                    )
                assertEquals("New Task", taskResponse.name)
                assertEquals("New Task Description", taskResponse.description)

                // Verify task was saved to database
                val savedTask = taskRepository.findByIdOrNull(taskResponse.id)
                assertTrue(savedTask != null)
                assertEquals("New Task", savedTask.name)
            }

            @Test
            fun `createTask with project member permission should return Created`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                val createRequest =
                    CreateTaskRequest(
                        name = "Member Task",
                        description = "Task created by member",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act
                mockMvc
                    .post(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isCreated() } }
            }

            @Test
            fun `createTask with project viewer permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (viewer, viewerToken) = createTestUserAndGetToken("project.viewer")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                val createRequest =
                    CreateTaskRequest(
                        name = "Viewer Task",
                        description = "Task attempted by viewer",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act & Assert
                mockMvc
                    .post(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, viewerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `createTask with outsider should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, outsiderToken) = createTestUserAndGetToken("outsider")
                val project = createTestProject(ownerToken)
                val createRequest =
                    CreateTaskRequest(
                        name = "Outsider Task",
                        description = "Task attempted by outsider",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act & Assert
                mockMvc
                    .post(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `createTask with invalid data should return BadRequest`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val invalidRequest =
                    CreateTaskRequest(
                        name = "", // Invalid empty name
                        description = null,
                        statusId = -1,
                    )

                // Act & Assert
                mockMvc
                    .post(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(invalidRequest)
                    }.andExpect { status { isBadRequest() } }
            }
        }

        // ===================================
        // PUT /projects/{projectId}/tasks/{id}
        // ===================================

        @Nested
        @DisplayName("Update Task Tests")
        inner class UpdateTaskTests {
            @Test
            fun `updateTask with task owner permission should return OK`() {
                // Arrange
                val (taskOwner, taskOwnerToken) = createTestUserAndGetToken("task.owner")
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskOwnerToken, project.id, "Original Task", "Original Description")

                val updateRequest =
                    UpdateTaskRequest(
                        name = "Updated Task",
                        description = "Updated Description",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act
                val result =
                    mockMvc
                        .put(buildTaskUri(project.id, task.id)) {
                            header(HttpHeaders.AUTHORIZATION, taskOwnerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(updateRequest)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val updatedTask =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        TaskResponse::class.java,
                    )
                assertEquals("Updated Task", updatedTask.name)
                assertEquals("Updated Description", updatedTask.description)
            }

            @Test
            fun `updateTask with project owner permission should return OK`() {
                // Arrange
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val (taskCreator, taskCreatorToken) = createTestUserAndGetToken("task.creator")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskCreatorToken, project.id, "Original Task", "Original Description")

                val updateRequest =
                    UpdateTaskRequest(
                        name = "Updated by Owner",
                        description = "Updated by project owner",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act
                mockMvc
                    .put(buildTaskUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, projectOwnerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isOk() } }
            }

            @Test
            fun `updateTask with other member permission should return Forbidden`() {
                // Arrange
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val (taskCreator, taskCreatorToken) = createTestUserAndGetToken("task.creator")
                val (otherMember, otherMemberToken) = createTestUserAndGetToken("other.member")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(projectOwnerToken, project.id, otherMember.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, otherMember.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskCreatorToken, project.id, "Original Task", "Original Description")

                val updateRequest =
                    UpdateTaskRequest(
                        name = "Unauthorized Update",
                        description = "This should not work",
                        statusId = getDefaultTaskStatusId(),
                    )

                // Act & Assert
                mockMvc
                    .put(buildTaskUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, otherMemberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isForbidden() } }
            }
        }

        // ===================================
        // DELETE /projects/{projectId}/tasks/{id}
        // ===================================

        @Nested
        @DisplayName("Delete Task Tests")
        inner class DeleteTaskTests {
            @Test
            fun `deleteTask with task owner permission should return NoContent`() {
                // Arrange
                val (taskOwner, taskOwnerToken) = createTestUserAndGetToken("task.owner")
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskOwnerToken, project.id, "Task to Delete", "Description")

                // Verify task exists
                assertTrue(taskRepository.existsById(task.id))

                // Act
                mockMvc
                    .delete(buildTaskUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, taskOwnerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert - Verify task was deleted
                assertFalse(taskRepository.existsById(task.id))
            }

            @Test
            fun `deleteTask with project owner permission should return NoContent`() {
                // Arrange
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val (taskCreator, taskCreatorToken) = createTestUserAndGetToken("task.creator")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskCreatorToken, project.id, "Task to Delete", "Description")

                // Act
                mockMvc
                    .delete(buildTaskUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, projectOwnerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert
                assertFalse(taskRepository.existsById(task.id))
            }

            @Test
            fun `deleteTask with other member permission should return Forbidden`() {
                // Arrange
                val (_, projectOwnerToken) = createTestUserAndGetToken("project.owner")
                val (taskCreator, taskCreatorToken) = createTestUserAndGetToken("task.creator")
                val (otherMember, otherMemberToken) = createTestUserAndGetToken("other.member")
                val project = createTestProject(projectOwnerToken)
                inviteMemberToProject(projectOwnerToken, project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskCreator.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(projectOwnerToken, project.id, otherMember.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, otherMember.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(taskCreatorToken, project.id, "Protected Task", "Description")

                // Act & Assert
                mockMvc
                    .delete(buildTaskUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, otherMemberToken)
                    }.andExpect { status { isForbidden() } }

                // Verify task still exists
                assertTrue(taskRepository.existsById(task.id))
            }
        }

        // ================================
        // Cross-cutting Concerns & Edge Cases
        // ================================

        @Nested
        @DisplayName("Cross-cutting Concerns and Edge Cases")
        inner class CrossCuttingConcernsTests {
            @Test
            fun `all endpoints should require authentication`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task", "Description")
                val createRequest = CreateTaskRequest("New Task", "Description", getDefaultTaskStatusId())
                val updateRequest = UpdateTaskRequest("Updated Task", "Description", getDefaultTaskStatusId())

                // Act & Assert - All endpoints should return 401 without token
                mockMvc
                    .get(buildTasksUri(project.id))
                    .andExpect { status { isUnauthorized() } }

                mockMvc
                    .get(buildTaskUri(project.id, task.id))
                    .andExpect { status { isUnauthorized() } }

                mockMvc
                    .post(buildTasksUri(project.id)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isUnauthorized() } }

                mockMvc
                    .put(buildTaskUri(project.id, task.id)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isUnauthorized() } }

                mockMvc
                    .delete(buildTaskUri(project.id, task.id))
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `task management should maintain data consistency`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val createRequest = CreateTaskRequest("Consistency Task", "Description", getDefaultTaskStatusId())

                // Act - Create, update, then delete task
                val createResult =
                    mockMvc
                        .post(buildTasksUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(createRequest)
                        }.andExpect { status { isCreated() } }
                        .andReturn()

                val createdTask =
                    objectMapper.readValue(
                        createResult.response.contentAsByteArray,
                        TaskResponse::class.java,
                    )

                val updateRequest = UpdateTaskRequest("Updated Task", "Updated Description", getDefaultTaskStatusId())
                mockMvc
                    .put(buildTaskUri(project.id, createdTask.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isOk() } }

                mockMvc
                    .delete(buildTaskUri(project.id, createdTask.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert - Final state should be clean
                assertFalse(taskRepository.existsById(createdTask.id))
                assertTrue(projectRepository.existsById(project.id))
            }

            @ParameterizedTest
            @EnumSource(value = MemberRoleEnum::class, names = ["PROJECT_OWNER"], mode = EnumSource.Mode.EXCLUDE)
            fun `task permissions should be enforced across all operations for different roles`(memberRole: MemberRoleEnum) {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("member.with.role")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, memberRole)
                acceptMemberInvitation(project.id, member.id, memberRole)
                val createRequest = CreateTaskRequest("Role Test Task", "Description", getDefaultTaskStatusId())

                // Act & Assert based on role
                when (memberRole) {
                    MemberRoleEnum.PROJECT_MEMBER -> {
                        // Members can create tasks
                        mockMvc
                            .post(buildTasksUri(project.id)) {
                                header(HttpHeaders.AUTHORIZATION, memberToken)
                                contentType = MediaType.APPLICATION_JSON
                                content = objectMapper.writeValueAsBytes(createRequest)
                            }.andExpect { status { isCreated() } }
                    }

                    MemberRoleEnum.PROJECT_VIEWER -> {
                        // Viewers cannot create tasks
                        mockMvc
                            .post(buildTasksUri(project.id)) {
                                header(HttpHeaders.AUTHORIZATION, memberToken)
                                contentType = MediaType.APPLICATION_JSON
                                content = objectMapper.writeValueAsBytes(createRequest)
                            }.andExpect { status { isForbidden() } }
                    }

                    else -> {
                        // Should not reach here based on enum exclusion
                    }
                }

                // All can read tasks
                mockMvc
                    .get(buildTasksUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isOk() } }
            }
        }

        // ================================
        // Helper Methods
        // ================================

        fun createTestTask(
            creatorToken: String,
            projectId: Long,
            name: String,
            description: String,
        ): TaskResponse {
            val createRequest =
                CreateTaskRequest(
                    name = name,
                    description = description,
                    statusId = getDefaultTaskStatusId(),
                )

            val response =
                mockMvc
                    .post(buildTasksUri(projectId)) {
                        header(HttpHeaders.AUTHORIZATION, creatorToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isCreated() } }
                    .andReturn()

            return objectMapper.readValue(
                response.response.contentAsByteArray,
                TaskResponse::class.java,
            )
        }

        private fun getDefaultTaskStatusId(): Int {
            return taskStatusRepository.findAll().first().id ?: 1
        }

        private fun buildTasksUri(projectId: Long): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}"

        private fun buildTaskUri(
            projectId: Long,
            taskId: Long,
        ): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}/$taskId"

        companion object {
            @JvmField
            @RegisterExtension
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)
        }
    }
