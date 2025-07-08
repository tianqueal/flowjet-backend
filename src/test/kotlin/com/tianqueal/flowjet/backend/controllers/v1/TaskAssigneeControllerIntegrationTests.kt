package com.tianqueal.flowjet.backend.controllers.v1

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.entities.keys.TaskAssigneeId
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.TaskAssigneeRepository
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("TaskAssignee Controller Integration Tests")
class TaskAssigneeControllerIntegrationTests
    @Autowired
    constructor(
        private val taskRepository: TaskRepository,
        private val projectRepository: ProjectRepository,
        private val taskAssigneeRepository: TaskAssigneeRepository,
        private val taskStatusRepository: TaskStatusRepository,
    ) : AbstractProjectControllerTest() {
        @BeforeEach
        fun setUp() {
            taskAssigneeRepository.deleteAll()
            taskRepository.deleteAll()
            projectRepository.deleteAll()
        }

        // ===========================================
        // GET /projects/{projectId}/tasks/{taskId}/assignees
        // ===========================================
        @Nested
        @DisplayName("Get Task Assignees Tests")
        inner class GetTaskAssigneesTests {
            @Test
            fun `getAssignees with project owner should return OK and paginated assignees`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (assignee1, _) = createTestUserAndGetToken("assignee.1")
                val (assignee2, _) = createTestUserAndGetToken("assignee.2")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")

                // Invite and accept project members
                inviteMemberToProject(ownerToken, project.id, assignee1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, assignee1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, assignee2.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, assignee2.id, MemberRoleEnum.PROJECT_MEMBER)

                // Assign users to the task
                assignUserToTask(ownerToken, project.id, task.id, assignee1.id)
                assignUserToTask(ownerToken, project.id, task.id, assignee2.id)

                // Act
                val result =
                    mockMvc
                        .get(buildAssigneesUri(project.id, task.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":2"))
                assertTrue(responseContent.contains(assignee1.username))
                assertTrue(responseContent.contains(assignee2.username))
            }

            @Test
            fun `getAssignees with project member permission should return OK`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")
                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                assertEquals(1, greenMail.receivedMessages.size, "Expected one invitation email.")
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                assignUserToTask(ownerToken, project.id, task.id, member.id)

                // Act & Assert
                mockMvc
                    .get(buildAssigneesUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isOk() } }
            }

            @Test
            fun `getAssignees with no permission (outsider) should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, outsiderToken) = createTestUserAndGetToken("outsider")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")

                // Act & Assert
                mockMvc
                    .get(buildAssigneesUri(project.id, task.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `getAssignees with invalid taskId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val nonExistentTaskId = 99999L

                // Act & Assert
                mockMvc
                    .get(buildAssigneesUri(project.id, nonExistentTaskId)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `getAssignees without authentication should return Unauthorized`() {
                // Act & Assert
                mockMvc
                    .get(buildAssigneesUri(1L, 1L))
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `getAssignees with username filter should return filtered results`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (assignee1, _) = createTestUserAndGetToken("find.this.user")
                val (assignee2, _) = createTestUserAndGetToken("ignore.this.user")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Filter Task")

                // Invite and accept project members
                inviteMemberToProject(ownerToken, project.id, assignee1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, assignee1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, assignee2.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, assignee2.id, MemberRoleEnum.PROJECT_MEMBER)

                assignUserToTask(ownerToken, project.id, task.id, assignee1.id)
                assignUserToTask(ownerToken, project.id, task.id, assignee2.id)

                // Act
                val result =
                    mockMvc
                        .get(buildAssigneesUri(project.id, task.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("username", "find.this")
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":1"))
                assertTrue(responseContent.contains("find.this.user"))
                assertFalse(responseContent.contains("ignore.this.user"))
            }
        }

        // ==================================================
        // POST /projects/{projectId}/tasks/{taskId}/assignees/{userId}
        // ==================================================
        @Nested
        @DisplayName("Assign User To Task Tests")
        inner class AssignUserTests {
            @Test
            fun `assignUser with project owner permission should return Created`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (userToAssign, _) = createTestUserAndGetToken("user.to.assign")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                inviteMemberToProject(ownerToken, project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, userToAssign.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isCreated() } }

                assertTrue(taskAssigneeRepository.existsById(TaskAssigneeId(task.id, userToAssign.id)))
            }

            @Test
            fun `assignUser with task owner permission should return Created`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (taskOwner, taskOwnerToken) = createTestUserAndGetToken("task.owner")
                val (userToAssign, _) = createTestUserAndGetToken("user.to.assign")
                val project = createTestProject(ownerToken)

                inviteMemberToProject(ownerToken, project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)

                val task = createTestTask(taskOwnerToken, project.id, "Owned Task")

                // Act & Assert
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, userToAssign.id)) {
                        header(HttpHeaders.AUTHORIZATION, taskOwnerToken)
                    }.andExpect { status { isCreated() } }
            }

            @Test
            fun `assignUser with member (not owner) permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("regular.member")
                val (userToAssign, _) = createTestUserAndGetToken("user.to.assign")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToAssign.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, userToAssign.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `assignUser with already assigned user should return Conflict`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (user, _) = createTestUserAndGetToken("user.to.assign")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                inviteMemberToProject(ownerToken, project.id, user.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, user.id, MemberRoleEnum.PROJECT_MEMBER)

                // First assignment (should succeed)
                assignUserToTask(ownerToken, project.id, task.id, user.id)

                // Act & Assert - Second assignment (should fail)
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, user.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isConflict() } }
            }

            @Test
            fun `assign non-existent user should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                val nonExistentUserId = 99999L

                // Act & Assert
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, nonExistentUserId)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `assign user who is not a project member should return UnprocessableEntity`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (notAMember, _) = createTestUserAndGetToken("not.a.member")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")

                // Act & Assert
                mockMvc
                    .post(buildAssigneeUri(project.id, task.id, notAMember.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isUnprocessableEntity() } }
            }
        }

        // ======================================================
        // DELETE /projects/{projectId}/tasks/{taskId}/assignees/{userId}
        // ======================================================
        @Nested
        @DisplayName("Remove User From Task Tests")
        inner class RemoveUserTests {
            @Test
            fun `removeUser with project owner permission should return NoContent`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (userToRemove, _) = createTestUserAndGetToken("user.to.remove")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                inviteMemberToProject(ownerToken, project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)
                assignUserToTask(ownerToken, project.id, task.id, userToRemove.id)
                assertTrue(taskAssigneeRepository.existsById(TaskAssigneeId(task.id, userToRemove.id)))

                // Act & Assert
                mockMvc
                    .delete(buildAssigneeUri(project.id, task.id, userToRemove.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                assertFalse(taskAssigneeRepository.existsById(TaskAssigneeId(task.id, userToRemove.id)))
            }

            @Test
            fun `removeUser with task owner permission should return NoContent`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (taskOwner, taskOwnerToken) = createTestUserAndGetToken("task.owner")
                val (userToRemove, _) = createTestUserAndGetToken("user.to.remove")
                val project = createTestProject(ownerToken)

                inviteMemberToProject(ownerToken, project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, taskOwner.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)

                val task = createTestTask(taskOwnerToken, project.id, "Owned Task")
                assignUserToTask(taskOwnerToken, project.id, task.id, userToRemove.id)

                // Act & Assert
                mockMvc
                    .delete(buildAssigneeUri(project.id, task.id, userToRemove.id)) {
                        header(HttpHeaders.AUTHORIZATION, taskOwnerToken)
                    }.andExpect { status { isNoContent() } }
            }

            @Test
            fun `removeUser with non-owner member permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("regular.member")
                val (userToRemove, _) = createTestUserAndGetToken("user.to.remove")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")

                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, userToRemove.id, MemberRoleEnum.PROJECT_MEMBER)

                assignUserToTask(ownerToken, project.id, task.id, userToRemove.id)

                // Act & Assert
                mockMvc
                    .delete(buildAssigneeUri(project.id, task.id, userToRemove.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `remove non-existent assignee should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val task = createTestTask(ownerToken, project.id, "Test Task")
                val nonExistentUserId = 99999L

                // Act & Assert
                mockMvc
                    .delete(buildAssigneeUri(project.id, task.id, nonExistentUserId)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }
        }

        // ================================
        // Helper Methods
        // ================================
        private fun createTestTask(
            creatorToken: String,
            projectId: Long,
            name: String,
            description: String? = "Test task description",
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

        private fun assignUserToTask(
            assignerToken: String,
            projectId: Long,
            taskId: Long,
            userIdToAssign: Long,
        ) {
            mockMvc
                .post(buildAssigneeUri(projectId, taskId, userIdToAssign)) {
                    header(HttpHeaders.AUTHORIZATION, assignerToken)
                }.andExpect { status { isCreated() } }
        }

        private fun getDefaultTaskStatusId(): Int = taskStatusRepository.findAll().first().safeId

        private fun buildTasksUri(projectId: Long): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}"

        private fun buildAssigneesUri(
            projectId: Long,
            taskId: Long,
        ): String = "${buildTasksUri(projectId)}/$taskId/assignees"

        private fun buildAssigneeUri(
            projectId: Long,
            taskId: Long,
            userId: Long,
        ): String = "${buildAssigneesUri(projectId, taskId)}/$userId"

        companion object {
            @JvmField
            @RegisterExtension
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)
        }
    }
