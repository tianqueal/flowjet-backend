package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserByAdminRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserSelfRequest
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
  fun createUserByAdmin(createUserRequest: CreateUserRequest): UserResponse
  fun registerUser(userRegisterRequest: UserRegisterRequest): UserResponse

  fun updateUserByAdmin(id: Long, updateUserByAdminRequest: UpdateUserByAdminRequest): UserResponse
  fun updateCurrentUser(username: String, updateUserSelfRequest: UpdateUserSelfRequest): UserResponse

  fun markUserAsVerifiedByUsername(username: String)
  fun markUserAsNotVerifiedByUsername(username: String)

  fun resetPasswordByEmail(email: String, newPassword: String)
}
