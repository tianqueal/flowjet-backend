package com.tianqueal.flowjet.backend.utils.constants

object ValidationMessageKeys {
    // Validation - user
    const val VALIDATION_USER_USERNAME_NOT_BLANK = "{validation.user.username.notBlank}"
    const val VALIDATION_USER_USERNAME_SIZE = "{validation.user.username.size}"
    const val VALIDATION_USER_USERNAME_PATTERN = "{validation.user.username.pattern}"
    const val VALIDATION_USER_EMAIL_NOT_BLANK = "{validation.user.email.notBlank}"
    const val VALIDATION_USER_EMAIL_INVALID = "{validation.user.email.invalid}"
    const val VALIDATION_USER_EMAIL_SIZE = "{validation.user.email.size}"
    const val VALIDATION_USER_PASSWORD_NOT_BLANK = "{validation.user.password.notBlank}"
    const val VALIDATION_USER_PASSWORD_SIZE = "{validation.user.password.size}"
    const val VALIDATION_USER_PASSWORD_PATTERN = "{validation.user.password.pattern}"
    const val VALIDATION_USER_NAME_NOT_BLANK = "{validation.user.name.notBlank}"
    const val VALIDATION_USER_NAME_SIZE = "{validation.user.name.size}"
    const val VALIDATION_USER_NAME_PATTERN = "{validation.user.name.pattern}"
    const val VALIDATION_USER_AVATAR_URL_SIZE = "{validation.user.avatarUrl.size}"
    const val VALIDATION_USER_AVATAR_URL_INVALID = "{validation.user.avatarUrl.invalid}"

    // Validation - auth
    const val VALIDATION_AUTH_USERNAME_OR_EMAIL_NOT_BLANK = "{validation.auth.usernameOrEmail.notBlank}"
    const val VALIDATION_AUTH_PASSWORD_NOT_NULL = "{validation.auth.password.notNull}"
    const val VALIDATION_AUTH_TOKEN_NOT_BLANK = "{validation.auth.token.notBlank}"

    const val VALIDATION_IDENTIFIER_NUMBER_NOT_NULL = "{validation.identifier.number.notNull}"
    const val VALIDATION_IDENTIFIER_NUMBER_POSITIVE = "{validation.identifier.number.positive}"

    const val VALIDATION_PROJECT_NAME_NOT_BLANK = "{validation.project.name.notBlank}"
    const val VALIDATION_PROJECT_NAME_SIZE = "{validation.project.name.size}"
    const val VALIDATION_PROJECT_DESCRIPTION_SIZE = "{validation.project.description.size}"

    const val VALIDATION_TASK_NAME_NOT_BLANK = "{validation.task.name.notBlank}"
    const val VALIDATION_TASK_NAME_SIZE = "{validation.task.name.size}"
    const val VALIDATION_TASK_DESCRIPTION_SIZE = "{validation.task.description.size}"
}
