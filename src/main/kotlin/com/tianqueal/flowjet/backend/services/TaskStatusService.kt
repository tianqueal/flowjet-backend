package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskStatusResponse

interface TaskStatusService {
    fun findAll(): List<TaskStatusResponse>
}
