package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class TaskAssigneeNotFoundException : AppException {
    constructor(taskId: Long, userId: Long) : super(
        message = "Task assignee not found with Task ID: '$taskId' and User ID: '$userId'",
        errorCode = MessageKeys.ERROR_TASK_ASSIGNEE_NOT_FOUND,
    )
}
