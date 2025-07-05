package com.tianqueal.flowjet.backend.controllers.v1

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectMemberRequest
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import com.tianqueal.flowjet.backend.exceptions.business.MemberRoleNotFoundException
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode
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

@DisplayName("ProjectMember Controller Integration Tests")
class ProjectMemberControllerIntegrationTests
    @Autowired
    constructor(
        private val projectRepository: ProjectRepository,
        private val projectMemberRepository: ProjectMemberRepository,
    ) : AbstractProjectControllerTest() {
        @BeforeEach
        fun setUp() {
            projectRepository.deleteAll()
        }

        // ===================================
        // GET /projects/{projectId}/members
        // ===================================

        @Nested
        @DisplayName("Get Project Members Tests")
        inner class GetProjectMembersTests {
            @Test
            fun `getProjectMembers with valid projectId and owner should return OK and paginated members`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member1, _) = createTestUserAndGetToken("member.1")
                val (member2, _) = createTestUserAndGetToken("member.2")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, member2.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member2.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":2"))
                assertTrue(responseContent.contains(member1.username))
                assertTrue(responseContent.contains(member2.username))
                assertTrue(responseContent.contains("\"content\""))
            }

            @Test
            fun `getProjectMembers with member permission should return OK and member list`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("project.member")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, memberToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":1"))
                assertTrue(responseContent.contains(member.username))
            }

            @Test
            fun `getProjectMembers with viewer permission should return OK and member list`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (viewer, viewerToken) = createTestUserAndGetToken("project.viewer")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)

                // Act
                mockMvc
                    .get(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, viewerToken)
                    }.andExpect { status { isOk() } }
            }

            @Test
            fun `getProjectMembers with no permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (_, outsiderToken) = createTestUserAndGetToken("outsider")

                val project = createTestProject(ownerToken)

                // Act & Assert
                mockMvc
                    .get(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `getProjectMembers with invalid projectId should return NotFound`() {
                // Arrange
                val (_, token) = createTestUserAndGetToken()
                val nonExistentProjectId = 99999L

                // Act & Assert
                mockMvc
                    .get(buildMembersUri(nonExistentProjectId)) {
                        header(HttpHeaders.AUTHORIZATION, token)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `getProjectMembers without token should return Unauthorized`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken()
                val project = createTestProject(ownerToken)

                // Act & Assert
                mockMvc
                    .get(buildMembersUri(project.id))
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `getProjectMembers with empty member list should return OK and empty content`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken()
                val project = createTestProject(ownerToken)

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":0"))
                assertTrue(responseContent.contains("\"content\":[]"))
            }

            @ParameterizedTest
            @ValueSource(strings = ["test.member1", "test.member2", "test.member3"])
            fun `getProjectMembers should find members by username filter`(memberUsername: String) {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken(memberUsername)

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("username", memberUsername)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains(memberUsername))
            }

            @Test
            fun `getProjectMembers with pagination should respect page parameters`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                // Create 5 members
                repeat(5) { index ->
                    val (member, _) = createTestUserAndGetToken("member$index")
                    inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                    acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                }

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("p", "0")
                            param("s", "2")
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"size\":2"))
                assertTrue(responseContent.contains("\"totalElements\":5"))
            }

            @ParameterizedTest
            @EnumSource(value = MemberRoleEnum::class, names = ["PROJECT_OWNER"], mode = Mode.EXCLUDE)
            fun `getProjectMembers should filter by member role correctly`(roleToSet: MemberRoleEnum) {
                // Arrange
                val (memberRoleId, expectedRole) =
                    memberRoleRegistry
                        .getRole(roleToSet)
                        .let { it.id to it.code }

                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("filtered.member")

                val project = createTestProject(ownerToken)

                inviteMemberToProject(ownerToken, project.id, member.id, expectedRole)
                acceptMemberInvitation(project.id, member.id, expectedRole)

                val anotherRole = MemberRoleEnum.entries.first { it != MemberRoleEnum.PROJECT_OWNER && it != roleToSet }
                val (anotherMember, _) = createTestUserAndGetToken("another.member")
                inviteMemberToProject(ownerToken, project.id, anotherMember.id, anotherRole)
                acceptMemberInvitation(project.id, anotherMember.id, anotherRole)

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("memberRoleId", memberRoleId.toString())
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":1"), "Expected 1 member, got: $responseContent")
                assertTrue(
                    responseContent.contains(expectedRole.name),
                    "Expected role $expectedRole not found in response, got: $responseContent",
                )
                assertFalse(
                    responseContent.contains(anotherMember.username),
                    "The member with the wrong role should not be in the response",
                )
            }
        }

        // ====================================
        // POST /projects/{projectId}/members
        // ====================================

        @Nested
        @DisplayName("Invite Project Member Tests")
        inner class InviteProjectMemberTests {
            @Test
            fun `inviteProjectMember with valid data and owner permission should return Accepted and receive email invitation`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)
                val projectMemberInvitationRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act
                val inviteResult =
                    mockMvc
                        .post(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(projectMemberInvitationRequest)
                        }.andExpect { status { isAccepted() } }
                        .andReturn()

                // Assert: Verify response message
                val invitationResponse =
                    objectMapper.readValue(
                        inviteResult.response.contentAsByteArray,
                        ProjectMemberInvitationResponse::class.java,
                    )
                assertTrue(invitationResponse.message.contains("invitation sent successfully", ignoreCase = true))

                // Assert: Verify email was sent
                val receivedMessages = greenMail.receivedMessages
                assertEquals(1, receivedMessages.size)
                val body = GreenMailUtil.getBody(receivedMessages[0])
                assertTrue(body.contains(project.name))
                assertTrue(body.contains(newMember.name))
                assertTrue(body.contains("?token="))
//            val memberResponse = objectMapper.readValue(
//                result.response.contentAsByteArray,
//                ProjectMemberResponse::class.java
//            )
//            assertEquals(newMember.id, memberResponse.member.id)
//            assertEquals(MemberRoleEnum.PROJECT_MEMBER, memberResponse.memberRole.code)
//
//            // Verify member was actually added to database
//            val memberExists = projectMemberRepository.existsById(
//                ProjectMemberId(project.id, newMember.id)
//            )
//            assertTrue(memberExists)
            }

            @Test
            fun `accept invitation with valid token should return OK and add member to project`() {
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val newMember = createTestUser("new.member")

                val project = createTestProject(ownerToken)
                val invitationToken =
                    projectMemberService.generateInvitationToken(
                        userId = newMember.id,
                        projectMemberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                val result =
                    mockMvc
                        .get("${buildMembersUri(project.id)}/accept-invitation") {
                            queryParam("token", invitationToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                val invitationAcceptResponse =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        ProjectMemberInvitationResponse::class.java,
                    )

                assertTrue(invitationAcceptResponse.message.contains("invitation accepted successfully", ignoreCase = true))
                assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, newMember.id)))
            }

            @Test
            fun `inviteProjectMember with member permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("existing.member")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_VIEWER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `inviteProjectMember with viewer permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (viewer, viewerToken) = createTestUserAndGetToken("project.viewer")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, viewerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `inviteProjectMember trying to add project owner should return BadRequest`() {
                // Arrange
                val (projectOwner, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = projectOwner.id, // Trying to add owner as member
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isBadRequest() } }
            }

            @Test
            fun `inviteProjectMember with already existing member should return Conflict`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (existingMember, _) = createTestUserAndGetToken("existing.member")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, existingMember.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, existingMember.id, MemberRoleEnum.PROJECT_VIEWER)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = existingMember.id, // Already a member
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isConflict() } }
            }

            @Test
            fun `inviteProjectMember with invalid userId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = 99999L, // Non-existent user
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `inviteProjectMember with invalid memberRoleId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = 99999, // Non-existent role
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `inviteProjectMember with invalid projectId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(99999L)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `inviteProjectMember without authentication should return Unauthorized`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `inviteProjectMember with invalid JSON should return BadRequest`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val malformedJson = """{"userId": "invalid", "memberRoleId": }"""

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = malformedJson
                    }.andExpect { status { isBadRequest() } }
            }

            @Test
            fun `inviteProjectMember with missing required fields should return BadRequest`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val incompleteRequest = """{"userId": null}"""

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = incompleteRequest
                    }.andExpect { status { isBadRequest() } }
            }

            @ParameterizedTest
            @EnumSource(value = MemberRoleEnum::class, names = ["PROJECT_OWNER"], mode = Mode.EXCLUDE)
            fun `inviteProjectMember should correctly assign different roles`(roleToSet: MemberRoleEnum) {
                // Arrange
                val memberRole = memberRoleRegistry.getRole(roleToSet)
                val memberRoleId = memberRole.id ?: throw MemberRoleNotFoundException(roleToSet)
                val expectedRole = memberRole.code

                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleId,
                    )

                // Act
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isAccepted() } }
                    .andReturn()

                val receivedMessages = greenMail.receivedMessages
                assertEquals(1, receivedMessages.size)

                val body = GreenMailUtil.getBody(receivedMessages[0])
                assertTrue(body.contains("?token="))

                val invitationToken =
                    projectMemberService.generateInvitationToken(
                        userId = addMemberRequest.userId,
                        projectMemberRoleId = addMemberRequest.memberRoleId,
                    )

                mockMvc
                    .get("${buildMembersUri(project.id)}/accept-invitation") {
                        queryParam("token", invitationToken)
                    }.andExpect { status { isOk() } }

                // Assert
                val projectMemberEntity =
                    projectMemberRepository.findByIdOrNull(
                        ProjectMemberId(project.id, newMember.id),
                    ) ?: throw IllegalStateException("Member not found in database")

                assertTrue(projectMemberEntity.memberRole.id == memberRoleId)
                assertEquals(expectedRole, projectMemberEntity.memberRole.code)
//            val memberResponse = objectMapper.readValue(
//                result.response.contentAsByteArray,
//                ProjectMemberResponse::class.java
//            )
//            assertEquals(expectedRole, memberResponse.memberRole.code)
            }

            @Test
            fun `inviteProjectMember as PROJECT_OWNER should return BadRequest`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (newMember, _) = createTestUserAndGetToken("new.member")

                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = newMember.id,
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_OWNER), // Trying to add as OWNER
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isBadRequest() } }
            }

            @Test
            fun `inviteProjectMember trying to add themselves should return BadRequest`() {
                // Arrange
                val (projectOwner, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val addMemberRequest =
                    ProjectMemberInvitationRequest(
                        userId = projectOwner.id, // Owner trying to add themselves
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isBadRequest() } }
            }
        }

        // ==========================================
        // PUT /projects/{projectId}/members/{userId}
        // ==========================================

        @Nested
        @DisplayName("Update Project Member Tests")
        inner class UpdateProjectMemberTests {
            @Test
            fun `updateProjectMemberRole with valid data and owner permission should return OK`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER), // Promote from VIEWER to MEMBER
                    )

                // Act
                val result =
                    mockMvc
                        .put(buildMemberUri(project.id, member.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(updateRequest)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val updatedMember =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        ProjectMemberResponse::class.java,
                    )
                assertEquals(MemberRoleEnum.PROJECT_MEMBER, updatedMember.memberRole.code)

                // Verify role was actually updated in database
                val memberEntity =
                    projectMemberRepository
                        .findById(
                            ProjectMemberId(project.id, member.id),
                        ).orElseThrow()
                assertEquals(memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER), memberEntity.memberRole.id)
            }

            @Test
            fun `updateProjectMemberRole with member permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member1, member1Token) = createTestUserAndGetToken("member1")
                val (member2, _) = createTestUserAndGetToken("member2")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, member2.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member2.id, MemberRoleEnum.PROJECT_VIEWER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(project.id, member2.id)) {
                        header(HttpHeaders.AUTHORIZATION, member1Token)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `updateProjectMemberRole by non-owner should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("member.self.update")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `updateProjectMemberRole with non-existent member should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(project.id, 99999L)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `updateProjectMemberRole with invalid memberRoleId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = 99999, // Non-existent role
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `updateProjectMemberRole with invalid projectId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(99999L, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `updateProjectMemberRole without authentication should return Unauthorized`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_VIEWER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER),
                    )

                // Act & Assert
                mockMvc
                    .put(buildMemberUri(project.id, member.id)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isUnauthorized() } }
            }

            @ParameterizedTest
            @EnumSource(value = MemberRoleEnum::class, names = ["PROJECT_OWNER"], mode = Mode.EXCLUDE)
            fun `updateProjectMemberRole should correctly update to different roles`(roleToSet: MemberRoleEnum) {
                // Arrange
                val memberRole = memberRoleRegistry.getRole(roleToSet)
                val memberRoleId = memberRole.id ?: throw MemberRoleNotFoundException(roleToSet)
                val expectedRole = memberRole.code

                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val project = createTestProject(ownerToken)

                val initialRole = MemberRoleEnum.entries.first { it != MemberRoleEnum.PROJECT_OWNER && it != roleToSet }
                inviteMemberToProject(ownerToken, project.id, member.id, initialRole)
                acceptMemberInvitation(project.id, member.id, initialRole)

                val updateRequest = UpdateProjectMemberRequest(memberRoleId = memberRoleId)

                // Act
                val result =
                    mockMvc
                        .put(buildMemberUri(project.id, member.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(updateRequest)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val updatedMember =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        ProjectMemberResponse::class.java,
                    )
                assertEquals(expectedRole, updatedMember.memberRole.code)
            }

            @Test
            fun `updateProjectMemberRole with same role should return OK and no changes`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.update")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                val updateRequest =
                    UpdateProjectMemberRequest(
                        memberRoleId = memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER), // Same role
                    )

                // Act
                val result =
                    mockMvc
                        .put(buildMemberUri(project.id, member.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(updateRequest)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val updatedMember =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        ProjectMemberResponse::class.java,
                    )
                assertEquals(MemberRoleEnum.PROJECT_MEMBER, updatedMember.memberRole.code)
            }
        }

        // =============================================
        // DELETE /projects/{projectId}/members/{userId}
        // =============================================

        @Nested
        @DisplayName("Remove Project Member Tests")
        inner class RemoveProjectMemberTests {
            @Test
            fun `deleteProjectMember with valid data and owner permission should return NoContent`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.remove")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Verify member exists before deletion
                assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, member.id)))

                // Act
                mockMvc
                    .delete(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert - Verify member was actually removed from database
                assertFalse(projectMemberRepository.existsById(ProjectMemberId(project.id, member.id)))
            }

            @Test
            fun `deleteProjectMember with member permission should return Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member1, member1Token) = createTestUserAndGetToken("member1")
                val (member2, _) = createTestUserAndGetToken("member2")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, member2.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member2.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .delete(buildMemberUri(project.id, member2.id)) {
                        header(HttpHeaders.AUTHORIZATION, member1Token)
                    }.andExpect { status { isForbidden() } }

                // Verify member was NOT removed
                assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, member2.id)))
            }

            @Test
            fun `deleteProjectMember by a non-owner should be Forbidden`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("member.self.remove")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .delete(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, memberToken)
                    }.andExpect { status { isForbidden() } }

                // Verify member was NOT removed
                // assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, member.id)))
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":1"))
                assertTrue(responseContent.contains(member.username))
            }

            @Test
            fun `deleteProjectMember with non-existent member should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                // Act & Assert
                mockMvc
                    .delete(buildMemberUri(project.id, 99999L)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `deleteProjectMember with invalid projectId should return NotFound`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.remove")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .delete(buildMemberUri(99999L, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNotFound() } }
            }

            @Test
            fun `deleteProjectMember without authentication should return Unauthorized`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.remove")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act & Assert
                mockMvc
                    .delete(buildMemberUri(project.id, member.id))
                    .andExpect { status { isUnauthorized() } }

                // Verify member was NOT removed
                assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, member.id)))
            }

            @Test
            fun `deleteProjectMember should clean up member completely`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.to.remove")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)

                // Act
                mockMvc
                    .delete(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert - Verify member completely removed
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                val responseContent = result.response.contentAsString
                assertTrue(
                    responseContent.contains("\"totalElements\":0"),
                    "Expected no members in response, but found: $responseContent",
                )

                // Verify the user still exists (not cascaded)
                assertTrue(userRepository.existsById(member.id))
            }

            @Test
            fun `deleteProjectMember should not affect other project members`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member1, _) = createTestUserAndGetToken("member1")
                val (member2, _) = createTestUserAndGetToken("member2")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member1.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, member2.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, member2.id, MemberRoleEnum.PROJECT_VIEWER)

                // Act - Remove only member1
                mockMvc
                    .delete(buildMemberUri(project.id, member1.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert
                assertFalse(projectMemberRepository.existsById(ProjectMemberId(project.id, member1.id)))
                assertTrue(projectMemberRepository.existsById(ProjectMemberId(project.id, member2.id)))
            }

            @ParameterizedTest
            @EnumSource(value = MemberRoleEnum::class, names = ["PROJECT_OWNER"], mode = Mode.EXCLUDE)
            fun `deleteProjectMember should work for all member roles as a project owner`(memberRole: MemberRoleEnum) {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("member.with.role")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, memberRole)
                acceptMemberInvitation(project.id, member.id, memberRole)

                // Act
                mockMvc
                    .delete(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"totalElements\":0"))

                // Verify the user still exists (not cascaded)
                assertTrue(userRepository.existsById(member.id))
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

                val addMemberRequest = ProjectMemberInvitationRequest(1L, 1)
                val updateMemberRequest = UpdateProjectMemberRequest(1)

                // Act & Assert - All endpoints should return 401 without token
                mockMvc
                    .get(buildMembersUri(project.id))
                    .andExpect { status { isUnauthorized() } }

                mockMvc
                    .post(buildMembersUri(project.id)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isUnauthorized() } }

                mockMvc
                    .put(buildMemberUri(project.id, 1L)) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateMemberRequest)
                    }.andExpect { status { isUnauthorized() } }

                mockMvc
                    .delete(buildMemberUri(project.id, 1L))
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `all endpoints should reject invalid JWT tokens`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)
                val invalidToken = "Bearer invalid-jwt-token-123"

                val addMemberRequest = ProjectMemberInvitationRequest(1L, 1)

                // Act & Assert
                mockMvc
                    .get(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, invalidToken)
                    }.andExpect { status { isUnauthorized() } }

                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, invalidToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addMemberRequest)
                    }.andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `endpoints should handle concurrent access properly`() {
                // Arrange
                val (_, owner1Token) = createTestUserAndGetToken("project.owner1")
                val (_, owner2Token) = createTestUserAndGetToken("project.owner2")

                val project1 = createTestProject(owner1Token)
                val project2 = createTestProject(owner2Token)

                // Act & Assert - Owner1 can access project1, but not project2
                mockMvc
                    .get(buildMembersUri(project1.id)) {
                        header(HttpHeaders.AUTHORIZATION, owner1Token)
                    }.andExpect { status { isOk() } }

                mockMvc
                    .get(buildMembersUri(project2.id)) {
                        header(HttpHeaders.AUTHORIZATION, owner1Token)
                    }.andExpect { status { isForbidden() } }
            }

            @Test
            fun `large member list should handle pagination correctly`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val project = createTestProject(ownerToken)

                // Create 25 members
                repeat(25) { index ->
                    val (member, _) = createTestUserAndGetToken("member$index")
                    inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                    acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                }

                // Act
                val result =
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, ownerToken)
                            param("p", "1")
                            param("s", "10")
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val responseContent = result.response.contentAsString
                assertTrue(responseContent.contains("\"size\":10"))
                assertTrue(responseContent.contains("\"number\":1"))
                assertTrue(responseContent.contains("\"totalElements\":25"))
            }

            @Test
            fun `member management should maintain data consistency`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, _) = createTestUserAndGetToken("consistency.member")

                val project = createTestProject(ownerToken)

                // Act - Invite member, accept invitation, update role, then remove
                val addRequest =
                    ProjectMemberInvitationRequest(member.id, memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_VIEWER))

                mockMvc
                    .post(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(addRequest)
                    }.andExpect { status { isAccepted() } }

                val invitationToken =
                    projectMemberService.generateInvitationToken(
                        userId = member.id,
                        projectMemberRoleId = addRequest.memberRoleId,
                    )

                mockMvc
                    .get("${buildMembersUri(project.id)}/accept-invitation") {
                        queryParam("token", invitationToken)
                    }.andExpect { status { isOk() } }

                val updateRequest = UpdateProjectMemberRequest(memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_MEMBER))

                mockMvc
                    .put(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isOk() } }

                mockMvc
                    .delete(buildMemberUri(project.id, member.id)) {
                        header(HttpHeaders.AUTHORIZATION, ownerToken)
                    }.andExpect { status { isNoContent() } }

                // Assert - Final state should be clean
                assertFalse(projectMemberRepository.existsById(ProjectMemberId(project.id, member.id)))
                assertTrue(userRepository.existsById(member.id))
                assertTrue(projectRepository.existsById(project.id))
            }

            @Test
            fun `member permissions should be enforced across all operations`() {
                // Arrange
                val (_, ownerToken) = createTestUserAndGetToken("project.owner")
                val (member, memberToken) = createTestUserAndGetToken("limited.member")
                val (viewer, viewerToken) = createTestUserAndGetToken("limited.viewer")
                val (outsider, outsiderToken) = createTestUserAndGetToken("complete.outsider")

                val project = createTestProject(ownerToken)
                inviteMemberToProject(ownerToken, project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                acceptMemberInvitation(project.id, member.id, MemberRoleEnum.PROJECT_MEMBER)
                inviteMemberToProject(ownerToken, project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)
                acceptMemberInvitation(project.id, viewer.id, MemberRoleEnum.PROJECT_VIEWER)

                val addRequest =
                    ProjectMemberInvitationRequest(outsider.id, memberRoleRegistry.getRoleId(MemberRoleEnum.PROJECT_VIEWER))

                // Act & Assert - Only owner can modify membership
                listOf(memberToken, viewerToken, outsiderToken).forEach { token ->
                    mockMvc
                        .post(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, token)
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsBytes(addRequest)
                        }.andExpect { status { isForbidden() } }
                }

                // But all can read (except outsider)
                listOf(memberToken, viewerToken).forEach { token ->
                    mockMvc
                        .get(buildMembersUri(project.id)) {
                            header(HttpHeaders.AUTHORIZATION, token)
                        }.andExpect { status { isOk() } }
                }

                mockMvc
                    .get(buildMembersUri(project.id)) {
                        header(HttpHeaders.AUTHORIZATION, outsiderToken)
                    }.andExpect { status { isForbidden() } }
            }
        }

        companion object {
            @JvmField
            @RegisterExtension
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)
        }
    }
