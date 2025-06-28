package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service(BeanNames.PROJECT_PERMISSION_SERVICE)
@Transactional(readOnly = true)
class ProjectPermissionService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
) {
    fun isOwner(
        id: Long,
        userId: Long,
    ): Boolean {
        val project = projectRepository.findByIdOrNull(id) ?: return false
        return project.projectOwner.id == userId
    }

    fun isMember(
        id: Long,
        userId: Long,
    ): Boolean = projectMemberRepository.isMember(id, userId)

    fun canAddMember(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId)

    fun canUpdateMemberRole(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId)

    fun canRemoveMember(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId)

    fun canRead(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId) || isMember(id, userId)

    fun canUpdate(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId)

    fun canDelete(
        id: Long,
        userId: Long,
    ): Boolean = isOwner(id, userId)
}
