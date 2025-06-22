package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.services.GravatarService
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class GravatarServiceImpl : GravatarService {
  override fun getAvatarUrl(email: String?, size: Int): String {
    val cleanedEmail = email?.trim()?.lowercase()
    val hash = cleanedEmail
      ?.takeIf { it.isNotEmpty() }
      ?.let { md5Hex(it) }
      ?: DEFAULT_HASH

    return UriComponentsBuilder.fromUriString(GRAVATAR_URL_PREFIX)
      .pathSegment(hash)
      .queryParam("s", size)
      .queryParam("d", DEFAULT_STYLE)
      .toUriString()
  }

  private fun md5Hex(input: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray(StandardCharsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  companion object {
    private const val GRAVATAR_URL_PREFIX = "https://www.gravatar.com/avatar"
    private const val DEFAULT_HASH = "00000000000000000000000000000000"
    private const val DEFAULT_STYLE = "identicon"
  }
}
