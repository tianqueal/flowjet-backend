package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import com.tianqueal.flowjet.backend.utils.functions.AuthFunctions
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service(BeanNames.AUTHENTICATED_USER_SERVICE)
@Transactional(readOnly = true)
class AuthenticatedUserService(
    private val userRepository: UserRepository,
) {
    fun getAuthenticatedUserEntity(): UserEntity {
        val username = AuthFunctions.getAuthenticatedUsername()

        return userRepository.findByUsername(username)
            ?: throw UserNotFoundException(username)
    }

    fun getAuthenticatedUserEntityWithRoles(): UserEntity {
        val username = AuthFunctions.getAuthenticatedUsername()

        return userRepository.findWithRolesByUsername(username)
            ?: throw UserNotFoundException(username)
    }

    fun getAuthenticatedUserId(): Long = getAuthenticatedUserEntity().safeId
}
