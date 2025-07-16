package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class TaskAssigneeAlreadyExistsException : AppException {
    constructor(taskId: Long, userId: Long) : super(
        message = "Task assignee for task ID $taskId and user ID $userId already exists.",
        errorCode = MessageKeys.ERROR_TASK_ASSIGNEE_ALREADY_EXISTS,
    )
}
