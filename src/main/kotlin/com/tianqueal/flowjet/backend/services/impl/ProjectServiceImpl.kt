package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectListResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectRequest
import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity_
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity_
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity_
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.v1.ProjectMapper
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.services.ProjectService
import com.tianqueal.flowjet.backend.specifications.ProjectSpecification
import com.tianqueal.flowjet.backend.utils.enums.ProjectAccessType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val projectMapper: ProjectMapper,
    private val authenticatedUserService: AuthenticatedUserService,
    private val projectPermissionService: ProjectPermissionService,
) : ProjectService {
    @Transactional(readOnly = true)
    override fun findAll(
        accessType: ProjectAccessType,
        name: String?,
        description: String?,
        statusId: Int?,
        pageable: Pageable,
    ): Page<ProjectListResponse> {
        val userId = authenticatedUserService.getAuthenticatedUserId()
        val securitySpec =
            when (accessType) {
                ProjectAccessType.OWNED_BY_ME -> ProjectSpecification.isOwner(userId)
                ProjectAccessType.MEMBER_OF -> ProjectSpecification.isMember(userId)
                ProjectAccessType.ALL_ACCESSIBLE -> ProjectSpecification.isOwnerOrMember(userId)
            }
        val filterSpec = ProjectSpecification.filterBy(name, description, statusId)

        val projectsPage =
            projectRepository.findAll(
                securitySpec.and(filterSpec),
                pageable,
            )

        if (!projectsPage.hasContent()) {
            return Page.empty(pageable)
        }

        val projectIds = projectsPage.content.mapNotNull { it.id }

        val memberCountsProjection = projectMemberRepository.countMembersByProjectIdIn(projectIds)
        val memberCounts = memberCountsProjection.associate { it.getProjectId() to it.getMemberCount() }

        return projectsPage.map {
            projectMapper.toListDto(entity = it, memberCount = memberCounts[it.id] ?: 0)
        }
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ProjectResponse {
        val projectEntity =
            projectRepository.findWithStatusAndOwnerById(id)
                ?: throw ProjectNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRead(projectEntity, userId)) {
            throw AuthorizationDeniedException("Access Denied: You do not have permission to view this project.")
        }

        val sort =
            Sort.by(
                Sort.Order.asc("${ProjectMemberEntity_.MEMBER_ROLE}.${MemberRoleEntity_.DISPLAY_ORDER}"),
                Sort.Order.asc("${ProjectMemberEntity_.USER}.${UserEntity_.NAME}").ignoreCase(),
            )
        val members = projectMemberRepository.findWithUserAndMemberRoleByIdProjectId(projectEntity.safeId, sort)

        return projectMapper.toDto(entity = projectEntity, members = members)
    }

    override fun create(createProjectRequest: CreateProjectRequest): ProjectResponse =
        create(authenticatedUserService.getAuthenticatedUserEntity(), createProjectRequest)

    override fun create(
        userEntity: UserEntity,
        createProjectRequest: CreateProjectRequest,
    ): ProjectResponse =
        projectRepository
            .save(
                projectMapper.toEntity(dto = createProjectRequest, user = userEntity),
            ).let(projectMapper::toDto)

    override fun create(
        userId: Long,
        createProjectRequest: CreateProjectRequest,
    ): ProjectResponse =
        userRepository
            .findByIdOrNull(userId)
            ?.let { create(it, createProjectRequest) }
            ?: throw UserNotFoundException(userId)

    override fun update(
        id: Long,
        updateProjectRequest: UpdateProjectRequest,
    ): ProjectResponse {
        val projectEntity =
            projectRepository.findWithStatusAndOwnerById(id)
                ?: throw ProjectNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canUpdateProject(projectEntity, userId)) {
            throw AuthorizationDeniedException("Access Denied: You do not have permission to update this project.")
        }
        projectMapper.updateEntityFromDto(
            dto = updateProjectRequest,
            entity = projectEntity,
        )
        return projectRepository
            .save(projectEntity)
            .let(projectMapper::toDto)
    }

    override fun delete(id: Long) {
        val projectEntity =
            projectRepository.findWithOwnerById(id)
                ?: throw ProjectNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canDeleteProject(projectEntity, userId)) {
            throw AuthorizationDeniedException("Access Denied: You do not have permission to delete this project.")
        }
        projectRepository.delete(projectEntity)
    }
}
