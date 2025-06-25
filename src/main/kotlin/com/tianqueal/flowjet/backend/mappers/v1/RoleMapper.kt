package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.role.RoleResponse
import com.tianqueal.flowjet.backend.utils.enums.RoleEnum
import org.springframework.stereotype.Component

@Component
class RoleMapper {
  fun toDto(code: RoleEnum, name: String): RoleResponse = RoleResponse(
    code = code,
    name = name
  )
}
