package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum

interface MemberRoleRegistry {
    /**
     * Gets the full MemberRoleEntity by its enum code.
     */
    fun getRole(code: MemberRoleEnum): MemberRoleEntity

    /**
     * Gets only the ID of a MemberRoleEntity by its enum code.
     */
    fun getRoleId(code: MemberRoleEnum): Int
}
