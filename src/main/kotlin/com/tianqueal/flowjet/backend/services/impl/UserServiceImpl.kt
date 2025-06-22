package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserByAdminRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserSelfRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.domain.entities.RoleEntity
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyVerifiedException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.UserMapper
import com.tianqueal.flowjet.backend.repositories.RoleRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.specifications.UserSpecification
import com.tianqueal.flowjet.backend.utils.constants.DefaultRoles
import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val userMapper: UserMapper,
  private val passwordEncoder: PasswordEncoder,
  private val roleRepository: RoleRepository
) : UserService, UserDetailsService {
  private lateinit var defaultRoles: Set<RoleEntity>

  @PostConstruct
  @Transactional(readOnly = true)
  fun initDefaultRole() {
    defaultRoles = roleRepository.findAllByCodeIn(DefaultRoles.USER)
  }

  @Transactional(readOnly = true)
  override fun loadUserByUsername(usernameOrEmail: String): UserDetails {
    val user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
      ?: throw UserNotFoundException(usernameOrEmail)

    return User.builder()
      .username(user.username)
      .password(user.passwordHash)
      .accountExpired(!user.isAccountNonExpired())
      .accountLocked(!user.isAccountNonLocked())
      .credentialsExpired(!user.isCredentialsNonExpired())
      .disabled(!user.isEnabled())
      .roles(*user.roles.map { it.code.shortName() }.toTypedArray())
      .build()
  }

  @Transactional(readOnly = true)
  override fun findAll(username: String?, email: String?, name: String?, pageable: Pageable): Page<UserResponse> =
    userRepository.findAll(
      UserSpecification.filterBy(username, email, name),
      pageable
    ).map(userMapper::toDto)

  @Transactional(readOnly = true)
  override fun findById(id: Long): UserResponse =
    userRepository.findById(id)
      .orElseThrow { UserNotFoundException(id) }
      .let(userMapper::toDto)

  @Transactional(readOnly = true)
  override fun findByUsername(username: String): UserResponse =
    userRepository.findByUsername(username)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(username)

  @Transactional(readOnly = true)
  override fun findByEmail(email: String): UserResponse {
    return userRepository.findByEmail(email)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(email)
  }

  @Transactional(readOnly = true)
  override fun findByUsernameOrEmail(usernameOrEmail: String): UserResponse {
    return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(usernameOrEmail)
  }

  override fun createAdminUser(createAdminUserRequest: CreateAdminUserRequest): UserResponse {
    if (userRepository.existsByUsername(createAdminUserRequest.username)) {
      throw UserAlreadyExistsException("username", createAdminUserRequest.username)
    }

    if (userRepository.existsByEmail(createAdminUserRequest.email)) {
      throw UserAlreadyExistsException("email", createAdminUserRequest.email)
    }

    val userEntity = userMapper.toEntity(createAdminUserRequest).apply {
      passwordHash = passwordEncoder.encode(createAdminUserRequest.password)
      roles = roleRepository.findAllByCodeIn(DefaultRoles.ADMIN).toMutableSet()
    }
    return userMapper.toDto(userRepository.save(userEntity))
  }

  override fun createUserByAdmin(createUserRequest: CreateUserRequest): UserResponse {
    if (userRepository.existsByUsername(createUserRequest.username)) {
      throw UserAlreadyExistsException("username", createUserRequest.username)
    }

    if (userRepository.existsByEmail(createUserRequest.email)) {
      throw UserAlreadyExistsException("email", createUserRequest.email)
    }

    val userEntity = userMapper.toEntity(createUserRequest).apply {
      passwordHash = passwordEncoder.encode(createUserRequest.password)
      roles = defaultRoles.toMutableSet()
    }
    return userMapper.toDto(userRepository.save(userEntity))
  }

  override fun registerUser(userRegisterRequest: UserRegisterRequest): UserResponse {
    if (userRepository.existsByUsername(userRegisterRequest.username)) {
      throw UserAlreadyExistsException("username", userRegisterRequest.username)
    }

    if (userRepository.existsByEmail(userRegisterRequest.email)) {
      throw UserAlreadyExistsException("email", userRegisterRequest.email)
    }

    val userEntity = userMapper.toEntity(userRegisterRequest).apply {
      passwordHash = passwordEncoder.encode(userRegisterRequest.password)
      roles = defaultRoles.toMutableSet()
    }
    return userMapper.toDto(userRepository.save(userEntity))
  }

  override fun updateUserByAdmin(id: Long, updateUserByAdminRequest: UpdateUserByAdminRequest): UserResponse {
    val userEntity = userRepository.findById(id).orElseThrow {
      UserNotFoundException(id)
    }
    userMapper.updateEntityByAdminFromDto(updateUserByAdminRequest, userEntity)
    return userMapper.toDto(userRepository.save(userEntity))
  }

  override fun updateCurrentUser(username: String, updateUserSelfRequest: UpdateUserSelfRequest): UserResponse {
    val userEntity = userRepository.findByUsername(username)
      ?: throw UserNotFoundException(username)
    userMapper.updateEntityUserSelfFromDto(updateUserSelfRequest, userEntity)
    return userMapper.toDto(userRepository.save(userEntity))
  }

  override fun markUserAsVerifiedByUsername(username: String) {
    val userEntity = userRepository.findByUsername(username)
      ?: throw UserNotFoundException(username)
    if (userEntity.verifiedAt != null) {
      throw UserAlreadyVerifiedException("username", username)
    }
    userEntity.verifiedAt = Instant.now()
    userRepository.save(userEntity)
  }

  override fun markUserAsNotVerifiedByUsername(username: String) {
    val userEntity = userRepository.findByUsername(username)
      ?: throw UserNotFoundException(username)
    if (userEntity.verifiedAt == null) {
      throw UserAlreadyVerifiedException("username", username)
    }
    userEntity.verifiedAt = null
    userRepository.save(userEntity)
  }

  override fun resetPasswordByEmail(email: String, newPassword: String) {
    val userEntity = userRepository.findByEmail(email)
      ?: throw UserNotFoundException(email)

    userEntity.passwordHash = passwordEncoder.encode(newPassword)
    userRepository.save(userEntity)
  }
}
