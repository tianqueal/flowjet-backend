package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.services.AvatarService
import com.tianqueal.flowjet.backend.utils.constants.AvatarConstants
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class AvatarServiceImpl : AvatarService {
  override fun getAvatarUrl(email: String?, size: Int): String {
    val cleanedEmail = email?.trim()?.lowercase()
    val hash = cleanedEmail
      ?.takeIf { it.isNotEmpty() }
      ?.let { md5Hex(it) }
      ?: AvatarConstants.DEFAULT_AVATAR_HASH

    return UriComponentsBuilder.fromUriString(AvatarConstants.URL_PREFIX)
      .pathSegment(hash)
      .queryParam("s", size)
      .queryParam("d", AvatarConstants.DEFAULT_STYLE)
      .toUriString()
  }

  private fun md5Hex(input: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray(StandardCharsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }
}
