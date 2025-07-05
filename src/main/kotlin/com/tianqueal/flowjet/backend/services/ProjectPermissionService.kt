package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import com.tianqueal.flowjet.backend.exceptions.business.MemberRoleNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import jakarta.annotation.PostConstruct
import org.springframework.data.repository.findByIdOrNull
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

    fun isProjectOwner(
        projectId: Long,
        userId: Long,
    ): Boolean {
        val projectEntity = projectRepository.findByIdOrNull(projectId) ?: throw ProjectNotFoundException(projectId)
        return isProjectOwner(projectEntity, userId)
    }

    fun isProjectOwner(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = projectEntity.owner.id == userId

    fun isProjectMember(
        projectId: Long,
        userId: Long,
    ): Boolean = projectMemberRepository.isMember(projectId, userId)

//    fun isNotViewer(
//        projectEntity: ProjectEntity,
//        userId: Long,
//    ): Boolean {
//        val projectMember = projectMemberRepository.findByIdOrNull(ProjectMemberId(projectEntity.id ?: -1, userId))
//            ?: throw ProjectMemberNotFoundException(projectEntity.id ?: -1, userId)
//        return projectMember.memberRole.id != viewerRole.id
//    }

    fun isTaskOwner(
        taskEntity: TaskEntity,
        userId: Long,
    ): Boolean = taskEntity.owner.id == userId

    fun canAddMember(
        id: Long,
        userId: Long,
    ): Boolean = isProjectOwner(id, userId)

    fun canCreateTask(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean {
        if (projectEntity.owner.id == userId) {
            return true
        }

        val projectMember =
            projectMemberRepository.findByIdOrNull(
                ProjectMemberId(projectEntity.id ?: -1, userId),
            )

        if (projectMember == null) {
            return false
        }

        return projectMember.memberRole.id != viewerRole.id
    }

    fun canUpdateMemberRole(
        id: Long,
        userId: Long,
    ): Boolean = isProjectOwner(id, userId)

    fun canRemoveMember(
        id: Long,
        userId: Long,
    ): Boolean = isProjectOwner(id, userId)

    fun canRead(
        id: Long,
        userId: Long,
    ): Boolean = isProjectOwner(id, userId) || isProjectMember(id, userId)

    fun canRead(
        projectEntity: ProjectEntity,
        userId: Long,
    ): Boolean = isProjectOwner(projectEntity, userId) || isProjectMember(projectEntity.id ?: -1, userId)

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
}
