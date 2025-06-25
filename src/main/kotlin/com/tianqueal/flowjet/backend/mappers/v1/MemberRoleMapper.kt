package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.MemberRoleResponse
import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity

class MemberRoleMapper {
    fun toDto(entity: MemberRoleEntity): MemberRoleResponse =
        MemberRoleResponse(
            id = entity.id ?: -1,
            code = entity.code,
            name = entity.name,
        )
}
