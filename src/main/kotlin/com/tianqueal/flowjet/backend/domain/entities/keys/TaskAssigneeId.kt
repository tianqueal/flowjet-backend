package com.tianqueal.flowjet.backend.domain.entities.keys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.Objects

@Embeddable
class TaskAssigneeId(
    @Column(name = "task_id")
    var taskId: Long,
    @Column(name = "user_id")
    var userId: Long,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskAssigneeId

        return taskId == other.taskId && userId == other.userId
    }

    override fun hashCode(): Int = Objects.hash(taskId, userId)
}
