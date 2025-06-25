package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserProfileRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserService {
  fun findAll(username: String?, email: String?, name: String?, pageable: Pageable): Page<UserResponse>
  fun findById(id: Long): UserResponse
  fun findByUsername(username: String): UserResponse
  fun findByEmail(email: String): UserResponse
  fun findByUsernameOrEmail(usernameOrEmail: String): UserResponse

  fun createAdminUser(createAdminUserRequest: CreateAdminUserRequest): UserResponse
  fun create(createUserRequest: CreateUserRequest): UserResponse
  fun registerUser(userRegisterRequest: UserRegisterRequest): UserResponse

  fun update(id: Long, updateUserRequest: UpdateUserRequest): UserResponse
  fun updateProfile(updateUserProfileRequest: UpdateUserProfileRequest): UserResponse

  fun verify(username: String)
  fun unverify(username: String)

  fun resetPassword(email: String, newPassword: String)
}
