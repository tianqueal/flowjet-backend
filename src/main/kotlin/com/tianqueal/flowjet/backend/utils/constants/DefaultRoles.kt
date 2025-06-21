package com.tianqueal.flowjet.backend.utils.constants

import com.tianqueal.flowjet.backend.utils.enums.RoleName

object DefaultRoles {
  val USER: Set<RoleName> = setOf(RoleName.ROLE_USER)
  val ADMIN: Set<RoleName> = setOf(RoleName.ROLE_ADMIN)
}
