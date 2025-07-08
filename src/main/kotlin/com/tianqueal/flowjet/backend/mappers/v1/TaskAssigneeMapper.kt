package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskAssigneeResponse
import com.tianqueal.flowjet.backend.domain.entities.TaskAssigneeEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.TaskAssigneeId
import org.springframework.stereotype.Component

@Component
class TaskAssigneeMapper(
    private val userProfileMapper: UserProfileMapper,
) {
    fun toDto(entity: TaskAssigneeEntity): TaskAssigneeResponse =
        TaskAssigneeResponse(
            assignee = entity.user.let(userProfileMapper::toDto),
            assignedAt = entity.createdAt,
        )

    fun toEntity(
        taskEntity: TaskEntity,
        userEntity: UserEntity,
    ): TaskAssigneeEntity =
        TaskAssigneeEntity(
            id = TaskAssigneeId(taskEntity.safeId, userEntity.safeId),
            task = taskEntity,
            user = userEntity,
        )
}
