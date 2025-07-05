package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskStatusResponse
import com.tianqueal.flowjet.backend.domain.entities.TaskStatusEntity
import org.springframework.stereotype.Component

@Component
class TaskStatusMapper {
    fun toDto(entity: TaskStatusEntity): TaskStatusResponse =
        TaskStatusResponse(id = entity.id ?: -1, code = entity.code, name = entity.name)
}
