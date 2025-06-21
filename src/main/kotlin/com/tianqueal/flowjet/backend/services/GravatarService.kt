package com.tianqueal.flowjet.backend.services

interface GravatarService {
  fun getAvatarUrl(email: String?, size: Int): String
}
