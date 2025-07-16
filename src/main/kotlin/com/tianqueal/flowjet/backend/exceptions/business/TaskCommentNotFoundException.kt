package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class TaskCommentNotFoundException : AppException {
    constructor(commentId: Long) : super(
        message = "Task comment not found with Comment ID: '$commentId'",
        errorCode = MessageKeys.ERROR_TASK_COMMENT_NOT_FOUND,
    )
}
