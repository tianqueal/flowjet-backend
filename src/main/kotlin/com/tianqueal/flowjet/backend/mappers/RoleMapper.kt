package com.tianqueal.flowjet.backend.mappers

import com.tianqueal.flowjet.backend.domain.dto.v1.role.RoleResponse
import com.tianqueal.flowjet.backend.utils.enums.RoleName
import org.springframework.stereotype.Component

@Component
class RoleMapper {
  fun toDto(code: RoleName, name: String): RoleResponse = RoleResponse(
    code = code,
    name = name
  )
}
