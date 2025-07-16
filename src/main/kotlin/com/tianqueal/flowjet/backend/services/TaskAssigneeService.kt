package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskAssigneeResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TaskAssigneeService {
    fun findAssigneesForTask(
        projectId: Long,
        taskId: Long,
        username: String?,
        name: String?,
        pageable: Pageable,
    ): Page<TaskAssigneeResponse>

    fun assignUser(
        projectId: Long,
        taskId: Long,
        userId: Long,
    ): TaskAssigneeResponse

    fun unassignUser(
        projectId: Long,
        taskId: Long,
        userId: Long,
    )
}
