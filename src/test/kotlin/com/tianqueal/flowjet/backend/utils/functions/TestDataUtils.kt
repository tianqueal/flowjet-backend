package com.tianqueal.flowjet.backend.utils.functions

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest

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
  ) = CreateUserRequest(username, email, name, password, avatarUrl)

  fun createTestUserRegisterRequest(
    username: String = DEFAULT_USERNAME,
    email: String = DEFAULT_EMAIL,
    name: String = DEFAULT_NAME,
    password: String = DEFAULT_PASSWORD,
  ) = UserRegisterRequest(username, email, name, password)

  fun createTestLoginRequest(
    usernameOrEmail: String = DEFAULT_USERNAME,
    password: String = DEFAULT_PASSWORD,
    rememberMe: Boolean = false,
  ) = LoginRequest(
    usernameOrEmail, password, rememberMe
  )

  fun createTestProjectRequest(
    name: String = "Test Project",
    description: String = "Test project description",
    projectStatusId: Int = 1,
  ): CreateProjectRequest = CreateProjectRequest(
    name = name,
    description = description,
    projectStatusId = projectStatusId
  )

  fun createTestUserRequest(
    username: String = DEFAULT_USERNAME,
    email: String = DEFAULT_EMAIL,
    name: String = DEFAULT_NAME,
    password: String = DEFAULT_PASSWORD,
  ): CreateUserRequest = CreateUserRequest(
    username = username,
    email = email,
    name = name,
    password = password
  )
}
