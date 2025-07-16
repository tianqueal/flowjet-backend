package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.repositories.TaskStatusRepository
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

abstract class AbstractTaskControllerTest : AbstractProjectControllerTest() {
    @Autowired
    protected lateinit var taskStatusRepository: TaskStatusRepository

    protected fun createTestTask(
        creatorToken: String,
        projectId: Long,
        name: String = "Test Task",
        description: String? = "This is a test task description.",
    ): TaskResponse {
        val createRequest =
            CreateTaskRequest(
                name = name,
                description = description,
                statusId = getDefaultTaskStatusId(),
            )

        val response =
            mockMvc
                .post(buildTasksUri(projectId)) {
                    header(HttpHeaders.AUTHORIZATION, creatorToken)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(createRequest)
                }.andExpect { status { isCreated() } }
                .andReturn()

        return objectMapper.readValue(
            response.response.contentAsByteArray,
            TaskResponse::class.java,
        )
    }

    protected fun buildTaskUri(
        projectId: Long,
        taskId: Long,
    ): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}/$taskId"

    protected fun getDefaultTaskStatusId(): Int = taskStatusRepository.findAll().first().safeId

    protected fun buildTasksUri(projectId: Long): String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}"
}
