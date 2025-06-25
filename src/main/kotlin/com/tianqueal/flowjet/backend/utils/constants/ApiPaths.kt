package com.tianqueal.flowjet.backend.utils.constants

object ApiPaths {
    // Prefixes
    const val BASE = "/api"
    const val V1 = "$BASE/v1"
    const val ADMIN = "/admin"

    // Resources
    const val USERS = "/users"
    const val USERS_ME = "/users/me"
    const val PROFILES = "/profiles"
    const val AUTH = "/auth"
    const val PROJECTS = "/projects"
    const val TASKS = "/tasks"
    const val NOTIFICATIONS = "/notifications"
    const val TAGS = "/tags"
    const val COMMENTS = "/comments"
    const val ROLES = "/roles"
    const val MEMBER_ROLES = "/member-roles"
    const val MEMBERS = "/members"
    const val TASK_ASSIGNEES = "/task-assignees"
    const val TASK_TAGS = "/task-tags"
    const val TASK_COMMENTS = "/task-comments"
    const val USER_ROLES = "/user-roles"
    const val TASK_STATUSES = "/task-statuses"
    const val PROJECT_STATUSES = "/project-statuses"
    const val NOTIFICATION_TYPES = "/notification-types"

    // Routes with identifiers
    const val USER_BY_ID = "$USERS/{id}"
    const val PROJECT_BY_ID = "$PROJECTS/{id}"
    const val TASK_BY_ID = "$TASKS/{id}"
    const val NOTIFICATION_BY_ID = "$NOTIFICATIONS/{id}"
    const val TAG_BY_ID = "$TAGS/{id}"
    const val COMMENT_BY_ID = "$COMMENTS/{id}"
    const val ROLE_BY_ID = "$ROLES/{id}"
    const val MEMBER_ROLE_BY_ID = "$MEMBER_ROLES/{id}"

    // const val PROJECT_MEMBER_BY_ID = "$PROJECT_MEMBERS/{projectId}/{userId}"
    const val TASK_ASSIGNEE_BY_ID = "$TASK_ASSIGNEES/{taskId}/{userId}"
    const val TASK_TAG_BY_ID = "$TASK_TAGS/{taskId}/{tagId}"
    const val TASK_COMMENT_BY_ID = "$TASK_COMMENTS/{taskId}/{commentId}"
    const val USER_ROLE_BY_ID = "$USER_ROLES/{userId}/{roleId}"
    const val TASK_STATUS_BY_ID = "$TASK_STATUSES/{id}"
    const val PROJECT_STATUS_BY_ID = "$PROJECT_STATUSES/{id}"
    const val NOTIFICATION_TYPE_BY_ID = "$NOTIFICATION_TYPES/{id}"

    // Auth paths
    const val LOGIN = "/login"
    const val REGISTER = "/register"
    const val VERIFY_EMAIL = "/verify-email"
    const val PASSWORD_RESET = "/password-reset"
    const val REFRESH_TOKEN = "/refresh-token"

    // Nested routes
    const val PROJECT_USERS = "$PROJECTS/{projectId}/users"
    const val PROJECT_TASKS = "$PROJECTS/{projectId}/tasks"
    const val TASK_COMMENTS_BY_TASK = "$TASKS/{taskId}/comments"
    const val USER_NOTIFICATIONS = "$USERS/{userId}/notifications"
    const val USER_PROJECTS = "$USERS/{userId}/projects"
    const val USER_ROLES_BY_USER = "$USERS/{userId}/roles"
}
