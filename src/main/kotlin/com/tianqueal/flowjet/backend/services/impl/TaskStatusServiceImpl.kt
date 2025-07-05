package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskStatusResponse
import com.tianqueal.flowjet.backend.mappers.v1.TaskStatusMapper
import com.tianqueal.flowjet.backend.repositories.TaskStatusRepository
import com.tianqueal.flowjet.backend.services.TaskStatusService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TaskStatusServiceImpl(
    private val taskStatusRepository: TaskStatusRepository,
    private val taskStatusMapper: TaskStatusMapper,
) : TaskStatusService {
    override fun findAll(): List<TaskStatusResponse> =
        taskStatusRepository
            .findAll()
            .map(taskStatusMapper::toDto)
}
