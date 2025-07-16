package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class CannotAddOwnerAsProjectMemberException : AppException {
    constructor(projectId: Long, userId: Long) : super(
        message = "Cannot add the owner as a project member for project ID: '$projectId' and user ID: '$userId'",
        errorCode = MessageKeys.ERROR_CANNOT_ADD_OWNER_AS_PROJECT_MEMBER,
    )
}
