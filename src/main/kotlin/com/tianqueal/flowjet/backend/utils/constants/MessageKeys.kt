package com.tianqueal.flowjet.backend.utils.constants

object MessageKeys {
    // General errors
    const val ERROR_METHOD_NOT_ALLOWED = "error.methodNotAllowed"
    const val ERROR_APP_GENERIC = "error.app.generic"

    // Authentication errors
    const val ERROR_AUTH_GENERIC = "error.auth.generic"
    const val ERROR_AUTH_INVALID_CREDENTIALS = "error.auth.invalidCredentials"
    const val ERROR_AUTH_USER_DISABLED = "error.auth.userDisabled"
    const val ERROR_AUTH_USER_LOCKED = "error.auth.userLocked"
    const val ERROR_AUTH_ACCOUNT_EXPIRED = "error.auth.accountExpired"
    const val ERROR_AUTH_CREDENTIALS_EXPIRED = "error.auth.credentialsExpired"
    const val ERROR_AUTH_INSUFFICIENT_AUTHENTICATION = "error.auth.insufficientAuthentication"
    const val ERROR_AUTH_JWT = "error.auth.jwt"
    const val ERROR_AUTH_VALIDATION_REGISTER = "error.auth.validation.register"
    const val ERROR_AUTH_FORBIDDEN = "error.auth.forbidden"

    const val ERROR_RESOURCE_NOT_FOUND = "error.resource.notFound"
    const val ERROR_PROJECT_NOT_FOUND = "error.project.notFound"
    const val ERROR_INVALID_JSON_FORMAT = "error.invalidJsonFormat"
    const val ERROR_MEMBER_ROLE_NOT_FOUND = "error.memberRole.notFound"
    const val ERROR_PROJECT_MEMBER_NOT_FOUND = "error.projectMember.notFound"
    const val ERROR_PROJECT_MEMBER_ALREADY_EXISTS = "error.projectMember.alreadyExists"
    const val ERROR_CANNOT_ASSIGN_OWNER_ROLE = "error.projectMember.cannotAssignOwnerRole"
    const val ERROR_CANNOT_ADD_OWNER_AS_PROJECT_MEMBER = "error.projectMember.cannotAddOwnerAsProjectMember"
    const val ERROR_CANNOT_SELF_MANAGE_PROJECT_MEMBERSHIP = "error.projectMember.cannotSelfManageProjectMembership"
    const val ERROR_INVALID_ARGUMENT = "error.invalidArgument"
    const val ERROR_TASK_NOT_FOUND = "error.task.notFound"
    const val ERROR_TASK_ASSIGNEE_NOT_FOUND = "error.task.assignee.notFound"
    const val ERROR_TASK_ASSIGNEE_ALREADY_EXISTS = "error.task.assignee.alreadyExists"
    const val ERROR_TASK_COMMENT_NOT_FOUND = "error.task.comment.notFound"
    const val ERROR_COMMENT_NESTING_LIMIT_EXCEEDED = "error.comment.nestingLimitExceeded"

    // Authentication success messages
    const val AUTH_LOGIN_SUCCESS = "auth.login.success"
    const val AUTH_REGISTER_SUCCESS = "auth.register.success"

    // User errors
    const val ERROR_USER_NOT_FOUND = "error.user.notFound"
    const val ERROR_USER_ALREADY_EXISTS = "error.user.alreadyExists"
    const val ERROR_USER_ALREADY_VERIFIED = "error.user.alreadyVerified"
    const val ERROR_USER_IS_NOT_PROJECT_MEMBER = "error.user.isNotProjectMember"

    // Email
    const val EMAIL_VERIFICATION_SUBJECT = "email.verification.subject"
    const val EMAIL_VERIFICATION_SUCCESS = "email.verification.success"
    const val EMAIL_PASSWORD_RESET_SUBJECT = "email.passwordReset.subject"
    const val EMAIL_PROJECT_MEMBER_INVITATION_SUBJECT = "email.projectMemberInvitation.subject"

    const val PASSWORD_RESET_REQUEST_SUCCESS = "passwordReset.request.success"
    const val PASSWORD_RESET_CONFIRM_SUCCESS = "passwordReset.confirm.success"

    const val PROJECT_MEMBER_INVITATION_SENT = "projectMember.invitation.sent"
    const val PROJECT_MEMBER_INVITATION_SUCCESS = "projectMember.invitation.success"
}
