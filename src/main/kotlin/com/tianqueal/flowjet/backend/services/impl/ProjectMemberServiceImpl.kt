package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectMemberRequest
import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import com.tianqueal.flowjet.backend.exceptions.business.CannotAddOwnerAsProjectMemberException
import com.tianqueal.flowjet.backend.exceptions.business.CannotAssignOwnerRoleException
import com.tianqueal.flowjet.backend.exceptions.business.CannotSelfManageProjectMembershipException
import com.tianqueal.flowjet.backend.exceptions.business.MemberRoleNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectMemberAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectMemberNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.v1.ProjectMemberMapper
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.EmailService
import com.tianqueal.flowjet.backend.services.ProjectMemberService
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.specifications.ProjectMemberSpecification
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectMemberServiceImpl(
    private val projectPermissionService: ProjectPermissionService,
    private val authenticatedUserService: AuthenticatedUserService,
    private val projectMemberMapper: ProjectMemberMapper,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailService: EmailService,
) : ProjectMemberService {
    @Transactional(readOnly = true)
    override fun findAll(
        projectId: Long,
        memberRoleId: Int?,
        username: String?,
        pageable: Pageable,
    ): Page<ProjectMemberResponse> {
        if (!projectRepository.existsById(projectId)) {
            throw ProjectNotFoundException(projectId)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRead(projectId, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to view members of this project.",
            )
        }

        return projectMemberRepository
            .findAll(
                ProjectMemberSpecification.filterBy(projectId, memberRoleId, username),
                pageable,
            ).map(projectMemberMapper::toDto)
    }

    override fun inviteProjectMember(
        projectId: Long,
        projectMemberInvitationRequest: ProjectMemberInvitationRequest,
        apiVersionPath: String,
    ) {
        // Phase 1: Check principal resource
        val projectEntity =
            projectRepository.findWithOwnerById(projectId)
                ?: throw ProjectNotFoundException(projectId)

        // Phase 2: Check authorization
        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canAddMember(projectEntity, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to add members to this project.",
            )
        }

        // Phase 3: Validate request and business logic
        // 3.1: Validate user and member role existence (payload data)
        val userEntity =
            userRepository.findByIdOrNull(projectMemberInvitationRequest.userId)
                ?: throw UserNotFoundException(projectMemberInvitationRequest.userId)
        val memberRoleEntity =
            memberRoleRepository.findByIdOrNull(projectMemberInvitationRequest.memberRoleId)
                ?: throw MemberRoleNotFoundException(projectMemberInvitationRequest.memberRoleId)

        // 3.2: Validate business rules
        if (projectMemberInvitationRequest.userId == authenticatedUserId) {
            throw CannotSelfManageProjectMembershipException(projectId)
        }
        if (projectMemberInvitationRequest.userId == projectEntity.owner.id) {
            throw CannotAddOwnerAsProjectMemberException(projectId, projectMemberInvitationRequest.userId)
        }
        if (memberRoleEntity.code == MemberRoleEnum.PROJECT_OWNER) {
            throw CannotAssignOwnerRoleException(projectId, projectMemberInvitationRequest.userId)
        }
        if (projectMemberRepository.isMember(projectId, projectMemberInvitationRequest.userId)) {
            throw ProjectMemberAlreadyExistsException(
                projectId = projectId,
                userId = projectMemberInvitationRequest.userId,
            )
        }

        // Phase 4: Execute the operation
        sendInvitationEmail(
            projectEntity = projectEntity,
            user = userEntity,
            memberRoleEntity = memberRoleEntity,
            apiVersionPath = apiVersionPath,
        )
//        val memberEntity = projectMemberMapper.toEntity(
//            dto = addProjectMemberRequest,
//            projectEntity = projectEntity,
//            userEntity = userEntity,
//            memberRoleEntity = memberRoleEntity
//        )
//        projectEntity.projectMembers.add(memberEntity)
//        projectRepository.save(projectEntity)
//
//        return memberEntity.let(projectMemberMapper::toDto)
    }

    override fun updateMemberRole(
        projectId: Long,
        userId: Long,
        updateProjectMemberRequest: UpdateProjectMemberRequest,
    ): ProjectMemberResponse {
        val projectMember =
            projectMemberRepository.findWithProjectUserAndMemberRoleById(
                ProjectMemberId(projectId = projectId, userId = userId),
            ) ?: throw ProjectMemberNotFoundException(projectId = projectId, userId = userId)

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canUpdateMemberRole(projectMember.project, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to update member roles in this project.",
            )
        }

        val memberRoleEntity = memberRoleRepository.getReferenceById(updateProjectMemberRequest.memberRoleId)

        if (userId == authenticatedUserId) {
            throw CannotSelfManageProjectMembershipException(projectId)
        }
        if (memberRoleEntity.code == MemberRoleEnum.PROJECT_OWNER) {
            throw CannotAssignOwnerRoleException(projectId, userId)
        }

        projectMemberMapper.updateEntityFromDto(entity = projectMember, memberRoleEntity = memberRoleEntity)
        return projectMemberRepository.save(projectMember).let(projectMemberMapper::toDto)
    }

    override fun removeProjectMember(
        projectId: Long,
        userId: Long,
    ) {
        val memberId = ProjectMemberId(projectId, userId)
        val projectMemberEntity =
            projectMemberRepository.findWithProjectById(memberId)
                ?: throw ProjectMemberNotFoundException(projectId, userId)

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRemoveMember(projectMemberEntity.project, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to remove members from this project.",
            )
        }

//        if (userId == authenticatedUserId) {
//            throw CannotSelfManageProjectMembershipException(projectId)
//        }

        projectMemberRepository.deleteById(memberId)
        projectPermissionService.evictUserPermissionCache(projectId, userId)
    }

    override fun generateInvitationToken(
        projectId: Long,
        userId: Long,
        memberRoleId: Int,
    ): String =
        jwtTokenProvider
            .generateProjectMemberInvitationTokenDetails(
                projectId = projectId,
                userId = userId,
                memberRoleId = memberRoleId,
            ).token

    override fun sendInvitationEmail(
        projectEntity: ProjectEntity,
        user: UserEntity,
        memberRoleEntity: MemberRoleEntity,
        apiVersionPath: String,
    ) = emailService.sendProjectMemberInvitation(
        to = user.email,
        name = user.name,
        token =
            generateInvitationToken(
                projectId = projectEntity.safeId,
                userId = user.safeId,
                memberRoleId = memberRoleEntity.safeId,
            ),
        projectEntity = projectEntity,
        apiVersionPath = apiVersionPath,
    )

    override fun verifyTokenAndAcceptInvitation(
        projectId: Long,
        token: String,
    ) {
        val subject = jwtTokenProvider.getSubjectFromToken(token)

        val (tokenProjectId, userId, memberRoleId) =
            try {
                val parts = subject.split(":")
                if (parts.size < 3) {
                    throw IllegalArgumentException("Invalid token format: $subject. Expected 'projectId:userId'")
                }
                Triple(first = parts[0].toLong(), second = parts[1].toLong(), third = parts[2].toInt())
            } catch (ex: NumberFormatException) {
                throw IllegalArgumentException("Invalid token format: $subject", ex)
            } catch (ex: IndexOutOfBoundsException) {
                throw IllegalArgumentException("Invalid token format: $subject. Expected 'projectId:userId'", ex)
            }

        if (tokenProjectId != projectId) {
            throw AuthorizationDeniedException("Token does not belong to project $projectId")
        }

        acceptInvitation(projectId, userId, memberRoleId)
        projectPermissionService.evictUserPermissionCache(projectId, userId)
    }

    override fun acceptInvitation(
        projectId: Long,
        userId: Long,
        memberRoleId: Int,
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw ProjectNotFoundException(projectId)
        }
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        if (!memberRoleRepository.existsById(memberRoleId)) {
            throw MemberRoleNotFoundException(memberRoleId)
        }

        if (projectMemberRepository.isMember(projectId, userId)) {
            throw ProjectMemberAlreadyExistsException(projectId, userId)
        }

        val projectProxy = projectRepository.getReferenceById(projectId)
        val userProxy = userRepository.getReferenceById(userId)
        val memberRoleProxy = memberRoleRepository.getReferenceById(memberRoleId)

        val memberEntity =
            projectMemberMapper.toEntity(
                projectEntity = projectProxy,
                userEntity = userProxy,
                memberRoleEntity = memberRoleProxy,
            )

        projectMemberRepository.save(memberEntity)
    }
}
