package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskCommentRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TaskCommentService {
    fun findAll(
        projectId: Long,
        taskId: Long,
        pageable: Pageable,
    ): Page<TaskCommentResponse>

    fun create(
        projectId: Long,
        taskId: Long,
        request: CreateTaskCommentRequest,
    ): TaskCommentResponse

    fun update(
        projectId: Long,
        taskId: Long,
        id: Long,
        request: UpdateTaskCommentRequest,
    ): TaskCommentResponse

    fun delete(
        projectId: Long,
        taskId: Long,
        id: Long,
    )
}
