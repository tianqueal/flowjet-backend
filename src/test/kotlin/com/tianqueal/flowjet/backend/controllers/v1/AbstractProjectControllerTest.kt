package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.services.MemberRoleRegistry
import com.tianqueal.flowjet.backend.services.ProjectMemberService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

abstract class AbstractProjectControllerTest : AbstractAuthenticatableControllerTest() {
    @Autowired
    protected lateinit var memberRoleRegistry: MemberRoleRegistry

    @Autowired
    protected lateinit var projectMemberService: ProjectMemberService

    /**
     * Creates a test project for the given user ID.
     * @param ownerToken The authentication token of the project owner
     * @return The created project response
     */
    protected fun createTestProject(ownerToken: String): ProjectResponse {
        val response =
            mockMvc
                .post(TestUris.PROJECTS_URI) {
                    header(HttpHeaders.AUTHORIZATION, ownerToken)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(TestDataUtils.createTestProjectRequest())
                }.andExpect { status { isCreated() } }
                .andReturn()
        return objectMapper.readValue(
            response.response.contentAsByteArray,
            ProjectResponse::class.java,
        )
    }

    /**
     * Invites a member to a project with the specified role.
     * @param ownerToken The authentication token of the project owner
     * @param projectId The ID of the project to which the member is being invited
     * @param userId The ID of the user being invited
     * @param memberRole The role to assign to the invited member
     */
    protected fun inviteMemberToProject(
        ownerToken: String,
        projectId: Long,
        userId: Long,
        memberRole: MemberRoleEnum,
    ) {
        val projectMemberInvitationRequest =
            ProjectMemberInvitationRequest(
                userId = userId,
                memberRoleId = memberRoleRegistry.getRoleId(memberRole),
            )
        mockMvc
            .post(buildMembersUri(projectId)) {
                header(HttpHeaders.AUTHORIZATION, ownerToken)
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(projectMemberInvitationRequest)
            }.andExpect { status { isAccepted() } }
    }

    /**
     * Accepts a member invitation for the specified project and user.
     * @param projectId The ID of the project for which the invitation is being accepted
     * @param userId The ID of the user accepting the invitation
     * @param memberRole The role assigned to the user in the project
     */
    protected fun acceptMemberInvitation(
        projectId: Long,
        userId: Long,
        memberRole: MemberRoleEnum,
    ) {
        val token =
            projectMemberService.generateInvitationToken(
                projectId = projectId,
                userId = userId,
                memberRoleId = memberRoleRegistry.getRoleId(memberRole),
            )
        mockMvc
            .get("${buildMembersUri(projectId)}/accept-invitation") {
                queryParam("token", token)
            }.andExpect { status { isOk() } }
    }

    /**
     * Builds the URI for project members collection.
     * @param projectId The ID of the project for which to build the members URI
     * @return The URI for the project members collection
     */
    protected fun buildMembersUri(projectId: Long): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.MEMBERS}"

    /**
     * Builds the URI for a specific project member.
     * @param projectId The ID of the project to which the member belongs
     * @param userId The ID of the user who is a member of the project
     * @return The URI for the specific project member
     */
    protected fun buildMemberUri(
        projectId: Long,
        userId: Long,
    ): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.MEMBERS}/$userId"
}
