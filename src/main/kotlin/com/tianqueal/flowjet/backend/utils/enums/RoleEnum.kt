package com.tianqueal.flowjet.backend.utils.enums

enum class RoleEnum {
    ROLE_ADMIN,
    ROLE_USER,
    ;

    fun shortName(): String = name.removePrefix("ROLE_")
}
