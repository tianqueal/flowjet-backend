package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectMemberRequest
import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectMemberService {
    fun findAll(
        projectId: Long,
        memberRoleId: Int?,
        username: String?,
        pageable: Pageable,
    ): Page<ProjectMemberResponse>

    fun inviteProjectMember(
        projectId: Long,
        projectMemberInvitationRequest: ProjectMemberInvitationRequest,
        apiVersionPath: String,
    )

    fun updateMemberRole(
        projectId: Long,
        userId: Long,
        updateProjectMemberRequest: UpdateProjectMemberRequest,
    ): ProjectMemberResponse

    fun removeProjectMember(
        projectId: Long,
        userId: Long,
    )

    fun isMember(
        projectId: Long,
        userId: Long,
    ): Boolean

    fun generateInvitationToken(
        projectId: Long,
        userId: Long,
        memberRoleId: Int,
    ): String

    fun sendInvitationEmail(
        projectEntity: ProjectEntity,
        user: UserEntity,
        memberRoleEntity: MemberRoleEntity,
        apiVersionPath: String,
    )

    fun verifyTokenAndAcceptInvitation(
        projectId: Long,
        token: String,
    )

    fun acceptInvitation(
        projectId: Long,
        userId: Long,
        memberRoleId: Int,
    )
}
