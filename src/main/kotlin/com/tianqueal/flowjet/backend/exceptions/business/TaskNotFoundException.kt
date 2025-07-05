package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class TaskNotFoundException : AppException {
    constructor(id: Long) : super(
        message = "Task not found with ID: '$id'",
        errorCode = MessageKeys.ERROR_TASK_NOT_FOUND,
        args = arrayOf(id),
    )
}
