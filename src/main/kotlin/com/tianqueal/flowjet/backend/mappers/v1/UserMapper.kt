package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserProfileRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.services.AvatarService
import org.springframework.stereotype.Component

@Component
class UserMapper(
  private val avatarService: AvatarService,
  private val roleMapper: RoleMapper
) {
  fun toDto(entity: UserEntity): UserResponse = UserResponse(
    id = entity.id ?: -1,
    username = entity.username,
    email = entity.email,
    name = entity.name,
    avatarUrl = entity.avatarUrl?.takeIf { it.isNotBlank() }
      ?: avatarService.getAvatarUrl(entity.email, 420),
    roles = entity.roles.map { roleMapper.toDto(it.code, it.name) }.toSet(),
    verifiedAt = entity.verifiedAt,
    accountExpiredAt = entity.accountExpiredAt,
    lockedAt = entity.lockedAt,
    credentialsExpiredAt = entity.credentialsExpiredAt,
    disabledAt = entity.disabledAt,
    createdAt = entity.createdAt,
    updatedAt = entity.updatedAt,
    deletedAt = entity.deletedAt
  )

  fun toEntity(dto: CreateAdminUserRequest): UserEntity =
    toEntityCommon(dto.username, dto.email, dto.name, dto.avatarUrl)

  fun toEntity(dto: CreateUserRequest): UserEntity =
    toEntityCommon(dto.username, dto.email, dto.name, dto.avatarUrl)

  fun toEntity(dto: UserRegisterRequest): UserEntity =
    toEntityCommon(dto.username, dto.email, dto.name, null)

  fun updateEntityFromDto(dto: UpdateUserRequest, entity: UserEntity) {
    entity.username = dto.username
    entity.email = dto.email
    entity.name = dto.name
    entity.avatarUrl = dto.avatarUrl
    entity.accountExpiredAt = dto.accountExpiredAt
    entity.credentialsExpiredAt = dto.credentialsExpiredAt
  }

  fun updateEntityFromDto(dto: UpdateUserProfileRequest, entity: UserEntity) {
    entity.name = dto.name
    entity.avatarUrl = dto.avatarUrl
  }

  private fun toEntityCommon(username: String, email: String, name: String, avatarUrl: String?): UserEntity =
    UserEntity(
      username = username,
      email = email,
      name = name,
      passwordHash = "",
      roles = mutableSetOf(),
      avatarUrl = avatarUrl
    )
}
