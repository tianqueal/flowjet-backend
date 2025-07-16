package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class ProjectMemberAlreadyExistsException : AppException {
    constructor(projectId: Long, userId: Long) : super(
        message = "Project member already exists with user ID: '$userId' in project ID: '$projectId'",
        errorCode = MessageKeys.ERROR_PROJECT_MEMBER_ALREADY_EXISTS,
    )
}
