package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserProfileRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.domain.entities.RoleEntity
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyVerifiedException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.v1.UserMapper
import com.tianqueal.flowjet.backend.repositories.RoleRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.specifications.UserSpecification
import com.tianqueal.flowjet.backend.utils.constants.DefaultRoles
import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
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
  private val roleRepository: RoleRepository,
  private val authenticatedUserService: AuthenticatedUserService,
) : UserService, UserDetailsService {
  private lateinit var defaultRoles: Set<RoleEntity>

  @PostConstruct
  @Transactional(readOnly = true)
  fun initDefaultRole() {
    defaultRoles = roleRepository.findAllByCodeIn(DefaultRoles.USER)
  }

  @Transactional(readOnly = true)
  override fun loadUserByUsername(usernameOrEmail: String): UserDetails {
    val userEntity = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
      ?: throw UserNotFoundException(usernameOrEmail)

    return User.builder()
      .username(userEntity.username)
      .password(userEntity.passwordHash)
      .accountExpired(!userEntity.isAccountNonExpired())
      .accountLocked(!userEntity.isAccountNonLocked())
      .credentialsExpired(!userEntity.isCredentialsNonExpired())
      .disabled(!userEntity.isEnabled())
      .roles(*userEntity.roles.map { it.code.shortName() }.toTypedArray())
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
    userRepository.findByIdOrNull(id)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(id)

  @Transactional(readOnly = true)
  override fun findByUsername(username: String): UserResponse =
    userRepository.findByUsername(username)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(username)

  @Transactional(readOnly = true)
  override fun findByEmail(email: String): UserResponse =
    userRepository.findByEmail(email)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(email)

  @Transactional(readOnly = true)
  override fun findByUsernameOrEmail(usernameOrEmail: String): UserResponse =
    userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
      ?.let(userMapper::toDto)
      ?: throw UserNotFoundException(usernameOrEmail)

  override fun createAdminUser(createAdminUserRequest: CreateAdminUserRequest): UserResponse {
    validateUserUniqueness(createAdminUserRequest.username, createAdminUserRequest.email)

    val userEntity = userMapper.toEntity(createAdminUserRequest).apply {
      passwordHash = passwordEncoder.encode(createAdminUserRequest.password)
      roles = roleRepository.findAllByCodeIn(DefaultRoles.ADMIN).toMutableSet()
    }
    return userRepository.save(userEntity).let(userMapper::toDto)
  }

  override fun create(createUserRequest: CreateUserRequest): UserResponse {
    validateUserUniqueness(createUserRequest.username, createUserRequest.email)

    val userEntity = userMapper.toEntity(createUserRequest).apply {
      passwordHash = passwordEncoder.encode(createUserRequest.password)
      roles = defaultRoles.toMutableSet()
    }
    return userRepository.save(userEntity).let(userMapper::toDto)
  }

  override fun registerUser(userRegisterRequest: UserRegisterRequest): UserResponse {
    validateUserUniqueness(userRegisterRequest.username, userRegisterRequest.email)

    val userEntity = userMapper.toEntity(userRegisterRequest).apply {
      passwordHash = passwordEncoder.encode(userRegisterRequest.password)
      roles = defaultRoles.toMutableSet()
    }
    return userRepository.save(userEntity).let(userMapper::toDto)
  }

  override fun update(id: Long, updateUserRequest: UpdateUserRequest): UserResponse {
    val userEntity = userRepository.findByIdOrNull(id)
      ?: throw UserNotFoundException(id)
    userMapper.updateEntityFromDto(updateUserRequest, userEntity)
    return userRepository.save(userEntity).let(userMapper::toDto)
  }

  override fun updateProfile(updateUserProfileRequest: UpdateUserProfileRequest): UserResponse {
    val userEntity = authenticatedUserService.getAuthenticatedUserEntity()
    userMapper.updateEntityFromDto(updateUserProfileRequest, userEntity)
    return userRepository.save(userEntity).let(userMapper::toDto)
  }

  override fun verify(username: String) {
    val userEntity = userRepository.findByUsername(username)
      ?: throw UserNotFoundException(username)
    if (userEntity.verifiedAt != null) {
      throw UserAlreadyVerifiedException("username", username)
    }
    userEntity.verifiedAt = Instant.now()
    userRepository.save(userEntity)
  }

  override fun unverify(username: String) {
    val userEntity = userRepository.findByUsername(username)
      ?: throw UserNotFoundException(username)
    if (userEntity.verifiedAt == null) {
      throw UserAlreadyVerifiedException("username", username)
    }
    userEntity.verifiedAt = null
    userRepository.save(userEntity)
  }

  override fun resetPassword(email: String, newPassword: String) {
    val userEntity = userRepository.findByEmail(email)
      ?: throw UserNotFoundException(email)

    userEntity.passwordHash = passwordEncoder.encode(newPassword)
    userRepository.save(userEntity)
  }

  private fun validateUserUniqueness(username: String, email: String) {
    if (userRepository.existsByUsername(username))
      throw UserAlreadyExistsException("username", username)

    if (userRepository.existsByEmail(email))
      throw UserAlreadyExistsException("email", email)
  }
}
