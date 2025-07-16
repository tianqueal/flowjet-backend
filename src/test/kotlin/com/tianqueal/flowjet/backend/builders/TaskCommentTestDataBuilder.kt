package com.tianqueal.flowjet.backend.builders

import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.TestUris

data class TaskCommentTestData(
    val projectId: Long,
    val taskId: Long,
    val topic: String,
    val taskCommentContent: String = "Test comment",
) {
    val createUri: String = "${TestUris.PROJECTS_URI}/$projectId${ApiPaths.TASKS}/$taskId${ApiPaths.COMMENTS}"

    fun buildTaskCommentUri(commentId: Long): String = "$createUri/$commentId"
}

class TaskCommentTestDataBuilder {
    private var projectId: Long = 0
    private var taskId: Long = 0
    private var taskCommentContent: String = "Test comment"

    fun withProject(projectId: Long): TaskCommentTestDataBuilder {
        this.projectId = projectId
        return this
    }

    fun withTask(taskId: Long): TaskCommentTestDataBuilder {
        this.taskId = taskId
        return this
    }

    fun withContent(content: String): TaskCommentTestDataBuilder {
        this.taskCommentContent = content
        return this
    }

    fun build(): TaskCommentTestData {
        require(projectId > 0) { "Project ID must be set" }
        require(taskId > 0) { "Task ID must be set" }

        val topic = "${ApiPaths.WS_TOPIC_PREFIX}${ApiPaths.PROJECTS}/$projectId${ApiPaths.TASKS}/$taskId${ApiPaths.COMMENTS}"

        return TaskCommentTestData(
            projectId = projectId,
            taskId = taskId,
            topic = topic,
            taskCommentContent = taskCommentContent,
        )
    }
}
