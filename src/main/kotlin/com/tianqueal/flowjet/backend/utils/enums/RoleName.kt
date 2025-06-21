package com.tianqueal.flowjet.backend.utils.enums

enum class RoleName(val displayName: String) {
  ROLE_ADMIN("Administrator"),
  ROLE_USER("User");

  fun getCode(): String = name
  fun shortName(): String = name.removePrefix("ROLE_")
}
