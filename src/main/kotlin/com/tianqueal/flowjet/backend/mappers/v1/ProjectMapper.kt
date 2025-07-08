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
            id = entity.safeId,
            name = entity.name,
            description = entity.description,
            status = projectStatusMapper.toDto(entity.status),
            owner = userProfileMapper.toDto(entity.owner),
            members = entity.members.map { projectMemberMapper.toDto(it) }.toSet(),
            createdAt = entity.createdAt,
        )

    fun toEntity(
        dto: CreateProjectRequest,
        user: UserEntity,
    ): ProjectEntity =
        ProjectEntity(
            name = dto.name,
            description = dto.description,
            status = projectStatusRepository.getReferenceById(dto.statusId),
            owner = user,
        )

    fun updateEntityFromDto(
        dto: UpdateProjectRequest,
        entity: ProjectEntity,
    ) {
        entity.name = dto.name
        entity.description = dto.description
        entity.status = projectStatusRepository.getReferenceById(dto.statusId)
    }
}
