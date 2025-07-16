package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class ProjectNotFoundException : AppException {
    constructor(id: Long) : super(
        message = "Project not found with ID: '$id'",
        errorCode = MessageKeys.ERROR_PROJECT_NOT_FOUND,
        args = arrayOf(id),
    )
}
