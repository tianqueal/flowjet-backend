package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class CannotSelfManageProjectMembershipException : AppException {
    constructor(projectId: Long) : super(
        message = "Cannot manage your own project membership for project ID: '$projectId'",
        errorCode = MessageKeys.ERROR_CANNOT_SELF_MANAGE_PROJECT_MEMBERSHIP,
    )
}
