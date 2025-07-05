package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskRequest
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TaskService {
    fun findAll(
        projectId: Long,
        name: String?,
        description: String?,
        statusId: Int?,
        pageable: Pageable,
    ): Page<TaskResponse>

    fun findById(
        projectId: Long,
        id: Long,
    ): TaskResponse

    fun create(
        projectId: Long,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse

    fun create(
        projectEntity: ProjectEntity,
        userEntity: UserEntity,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse

    fun create(
        projectId: Long,
        userId: Long,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse

    fun update(
        projectId: Long,
        id: Long,
        updateTaskRequest: UpdateTaskRequest,
    ): TaskResponse

    fun delete(
        projectId: Long,
        id: Long,
    )
}
