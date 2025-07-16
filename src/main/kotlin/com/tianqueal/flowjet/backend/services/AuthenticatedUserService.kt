package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service(BeanNames.AUTHENTICATED_USER_SERVICE)
@Transactional(readOnly = true)
class AuthenticatedUserService(
    private val userRepository: UserRepository,
) {
    fun getAuthenticatedUserId(): Long {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth.principal == null) {
            throw InsufficientAuthenticationException(
                "User is not authenticated or principal is null",
            )
        }
        return auth.name.toLongOrNull()
            ?: throw IllegalStateException("Authenticated user principal is not a valid user ID.")
    }

//    fun getAuthenticatedUserEntity(): UserEntity {
//        val userId = getAuthenticatedUserId()
//        return userRepository.findByIdOrNull(userId)
//            ?: throw UserNotFoundException(userId)
//    }

    fun getAuthenticatedUserEntityWithRoles(): UserEntity {
        val id = getAuthenticatedUserId()

        return userRepository.findWithRolesById(id)
            ?: throw UserNotFoundException(id)
    }

//    fun getAuthenticatedUserIdFromAccessor(accessor: StompHeaderAccessor): Long? =
//        (accessor.user as? UsernamePasswordAuthenticationToken)?.name?.toLongOrNull()
}
