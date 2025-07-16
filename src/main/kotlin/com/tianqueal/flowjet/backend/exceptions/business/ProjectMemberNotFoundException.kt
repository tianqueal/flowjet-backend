package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class ProjectMemberNotFoundException : AppException {
    constructor(projectId: Long, userId: Long) : super(
        message = "Project member not found with Project ID: '$projectId' and User ID: '$userId'",
        errorCode = MessageKeys.ERROR_PROJECT_MEMBER_NOT_FOUND,
    )
}
