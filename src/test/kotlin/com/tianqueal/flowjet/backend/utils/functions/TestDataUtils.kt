package com.tianqueal.flowjet.backend.utils.functions

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest

object TestDataUtils {
    const val DEFAULT_USERNAME = "test.user"
    const val DEFAULT_EMAIL = "$DEFAULT_USERNAME@example.com"
    const val DEFAULT_NAME = "Test User"
    const val DEFAULT_PASSWORD = "SecureP@ssw0rd"

    fun createTestUserRequest(
        username: String = DEFAULT_USERNAME,
        email: String = DEFAULT_EMAIL,
        name: String = DEFAULT_NAME,
        password: String = DEFAULT_PASSWORD,
        avatarUrl: String? = null,
    ) = CreateUserRequest(
        username = username,
        email = email,
        name = name,
        password = password,
        avatarUrl = avatarUrl,
    )

    fun createTestUserRegisterRequest(
        username: String = DEFAULT_USERNAME,
        email: String = DEFAULT_EMAIL,
        name: String = DEFAULT_NAME,
        password: String = DEFAULT_PASSWORD,
    ) = UserRegisterRequest(
        username = username,
        email = email,
        name = name,
        password = password,
    )

    fun createTestLoginRequest(
        usernameOrEmail: String = DEFAULT_USERNAME,
        password: String = DEFAULT_PASSWORD,
        rememberMe: Boolean = false,
    ) = LoginRequest(
        usernameOrEmail = usernameOrEmail,
        password = password,
        rememberMe = rememberMe,
    )

    fun createTestProjectRequest(
        name: String = "Test Project",
        description: String = "Test project description",
        statusId: Int = 1,
    ): CreateProjectRequest =
        CreateProjectRequest(
            name = name,
            description = description,
            statusId = statusId,
        )
}
