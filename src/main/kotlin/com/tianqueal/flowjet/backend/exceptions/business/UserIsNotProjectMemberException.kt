package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class UserIsNotProjectMemberException : AppException {
    constructor(projectId: Long, userId: Long) : super(
        message = "User with ID: '$userId' is not a member of project with ID: '$projectId'",
        errorCode = MessageKeys.ERROR_USER_IS_NOT_PROJECT_MEMBER,
    )
}
