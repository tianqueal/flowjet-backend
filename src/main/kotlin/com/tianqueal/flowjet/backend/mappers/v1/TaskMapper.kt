package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskRequest
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.repositories.TaskStatusRepository
import org.springframework.stereotype.Component

@Component
class TaskMapper(
    private val taskStatusMapper: TaskStatusMapper,
    private val userProfileMapper: UserProfileMapper,
    private val taskStatusRepository: TaskStatusRepository,
) {
    fun toDto(entity: TaskEntity): TaskResponse =
        TaskResponse(
            id = entity.id ?: -1,
            name = entity.name,
            description = entity.description,
            status = entity.status.let(taskStatusMapper::toDto),
            owner = entity.owner.let(userProfileMapper::toDto),
            dueDate = entity.dueDate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(
        dto: CreateTaskRequest,
        project: ProjectEntity,
        user: UserEntity,
    ): TaskEntity =
        TaskEntity(
            name = dto.name,
            description = dto.description,
            status = taskStatusRepository.getReferenceById(dto.statusId),
            project = project,
            owner = user,
            dueDate = dto.dueDate,
        )

    fun updateEntityFromDto(
        dto: UpdateTaskRequest,
        entity: TaskEntity,
    ) {
        entity.name = dto.name
        entity.description = dto.description
        entity.status = taskStatusRepository.getReferenceById(dto.statusId)
        entity.dueDate = dto.dueDate
    }
}
