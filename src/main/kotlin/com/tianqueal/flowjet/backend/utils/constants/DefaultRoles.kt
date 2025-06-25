package com.tianqueal.flowjet.backend.utils.constants

import com.tianqueal.flowjet.backend.utils.enums.RoleEnum

object DefaultRoles {
    val USER: Set<RoleEnum> = setOf(RoleEnum.ROLE_USER)
    val ADMIN: Set<RoleEnum> = setOf(RoleEnum.ROLE_ADMIN)
}
