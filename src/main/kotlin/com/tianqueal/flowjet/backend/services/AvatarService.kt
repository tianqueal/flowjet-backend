package com.tianqueal.flowjet.backend.services

interface AvatarService {
    fun getAvatarUrl(
        email: String?,
        size: Int,
    ): String
}
