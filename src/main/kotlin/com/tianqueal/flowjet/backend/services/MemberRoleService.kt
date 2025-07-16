package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.project.MemberRoleResponse

interface MemberRoleService {
    fun findAll(): List<MemberRoleResponse>
}
