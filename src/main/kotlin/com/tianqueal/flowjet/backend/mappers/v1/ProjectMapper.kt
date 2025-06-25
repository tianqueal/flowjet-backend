package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectRequest
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.repositories.ProjectStatusRepository
import org.springframework.stereotype.Component

@Component
class ProjectMapper(
    private val projectStatusMapper: ProjectStatusMapper,
    private val userProfileMapper: UserProfileMapper,
    private val projectMemberMapper: ProjectMemberMapper,
    private val projectStatusRepository: ProjectStatusRepository,
) {
    fun toDto(entity: ProjectEntity): ProjectResponse =
        ProjectResponse(
            id = entity.id ?: -1,
            name = entity.name,
            description = entity.description,
            projectStatus = projectStatusMapper.toDto(entity.projectStatus),
            projectOwner = userProfileMapper.toDto(entity.projectOwner),
            projectMembers = entity.projectMembers.map { projectMemberMapper.toDto(it) }.toSet(),
            createdAt = entity.createdAt,
        )

    fun toEntity(
        dto: CreateProjectRequest,
        user: UserEntity,
    ): ProjectEntity =
        ProjectEntity(
            name = dto.name,
            description = dto.description,
            projectStatus = projectStatusRepository.getReferenceById(dto.projectStatusId),
            projectOwner = user,
        )

    fun updateEntityFromDto(
        dto: UpdateProjectRequest,
        entity: ProjectEntity,
    ) {
        entity.name = dto.name
        entity.description = dto.description
        entity.projectStatus = projectStatusRepository.getReferenceById(dto.projectStatusId)
    }
}
