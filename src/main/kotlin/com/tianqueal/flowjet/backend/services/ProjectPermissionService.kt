package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import com.tianqueal.flowjet.backend.exceptions.business.MemberRoleNotFoundException
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import jakarta.annotation.PostConstruct
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service(BeanNames.PROJECT_PERMISSION_SERVICE)
@Transactional(readOnly = true)
class ProjectPermissionService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
) {
    private lateinit var viewerRole: MemberRoleEntity

    @PostConstruct
    fun init() {
        viewerRole = memberRoleRepository.findByCode(MemberRoleEnum.PROJECT_VIEWER)
            ?: throw MemberRoleNotFoundException(MemberRoleEnum.PROJECT_VIEWER)
    }

//    fun isProjectOwner(
//        projectId: Long,
//        userId: Long,
//    ): Boolean = projectRepository.isOwner(projectId, userId)

    fun isProjectOwner(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = projectEntity.owner.id == userId

//    fun isProjectMember(
//        projectId: Long,
//        userId: Long,
//    ): Boolean = projectMemberRepository.isMember(projectId, userId)

//    fun isNotViewer(
//        projectEntity: ProjectEntity,
//        userId: Long,
//    ): Boolean {
//        val projectMember = projectMemberRepository.findByIdOrNull(ProjectMemberId(projectEntity.safeId, userId))
//            ?: throw ProjectMemberNotFoundException(projectEntity.safeId, userId)
//        return projectMember.memberRole.id != viewerRole.id
//    }

    fun isTaskOwner(
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = taskEntity.owner.id == userId

    fun canAddMember(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId)

    fun canCreateTask(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean {
        if (projectEntity.owner.id == userId) {
            return true
        }

        val projectMember =
            projectMemberRepository.findWithMemberRoleById(
                ProjectMemberId(projectEntity.safeId, userId),
            ) ?: return false

        return projectMember.memberRole.id != viewerRole.id
    }

    fun canUpdateMemberRole(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId)

    fun canRemoveMember(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId)

    @Cacheable(
        value = ["project-permissions"],
        key = "'canRead_' + #projectId + '_' + #userId",
        condition = "#projectId > 0 && #userId > 0",
        unless = "#result == false",
    )
    fun canRead(
        projectId: Long,
        userId: Long,
    ): Boolean = projectRepository.hasReadAccess(projectId, userId)

    fun canUpdateProject(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId)

    fun canUpdateTask(
        projectEntity: ProjectEntity,
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId) || isTaskOwner(taskEntity, userId)

    fun canDeleteProject(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId)

    fun canDeleteTask(
        projectEntity: ProjectEntity,
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId) || isTaskOwner(taskEntity, userId)

    fun canAddTaskAssignee(
        projectEntity: ProjectEntity,
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId) || isTaskOwner(taskEntity, userId)

    fun canRemoveTaskAssignee(
        projectEntity: ProjectEntity,
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId) || isTaskOwner(taskEntity, userId)

    fun canCreateTaskComment(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean {
        if (isProjectOwner(projectEntity, userId)) {
            return true
        }

        val projectMember =
            projectMemberRepository.findWithMemberRoleById(
                ProjectMemberId(projectEntity.safeId, userId),
            ) ?: return false

        return projectMember.memberRole.id != viewerRole.id
    }

    @Suppress("unused")
    @CacheEvict(
        value = ["project-permissions"],
        key = "'canRead_' + #projectId + '_' + #userId",
    )
    fun evictUserPermissionCache(
        projectId: Long,
        userId: Long,
    ) {
        // Spring AOP will handle the eviction
    }

    @CacheEvict(
        value = ["project-permissions"],
        allEntries = true,
    )
    fun evictAllProjectPermissions() {
        // Spring AOP will handle the eviction
    }
}
