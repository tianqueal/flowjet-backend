package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.services.AvatarService
import com.tianqueal.flowjet.backend.utils.constants.AvatarConstants
import org.springframework.stereotype.Component

@Component
class UserProfileMapper(
    private val avatarService: AvatarService,
) {
    fun toDto(entity: UserEntity): UserProfileResponse =
        UserProfileResponse(
            id = entity.safeId,
            username = entity.username,
            name = entity.name,
            avatarUrl =
                entity.avatarUrl?.takeIf { it.isNotBlank() }
                    ?: avatarService.getAvatarUrl(entity.email, AvatarConstants.DEFAULT_SIZE),
        )
}
