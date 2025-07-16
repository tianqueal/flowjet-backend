package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class CannotAssignOwnerRoleException : AppException {
    constructor(projectId: Long, userId: Long) : super(
        message = "Cannot assign the owner role to user ID: '$userId' in project ID: '$projectId'",
        errorCode = MessageKeys.ERROR_CANNOT_ASSIGN_OWNER_ROLE,
    )
}
