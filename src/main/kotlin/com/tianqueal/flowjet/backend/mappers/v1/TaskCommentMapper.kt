package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.entities.TaskCommentEntity
import org.springframework.stereotype.Component

@Component
class TaskCommentMapper(
    private val userProfileMapper: UserProfileMapper,
) {
    fun toDto(
        entity: TaskCommentEntity,
        replies: List<TaskCommentResponse>,
    ): TaskCommentResponse =
        TaskCommentResponse(
            id = entity.safeId,
            content = entity.content,
            author = entity.author.let(userProfileMapper::toDto),
            replies = replies,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toDto(entity: TaskCommentEntity): TaskCommentResponse = toDto(entity, emptyList())
}
