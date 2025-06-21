package com.tianqueal.flowjet.backend.domain.dto.v1.role

import com.tianqueal.flowjet.backend.utils.enums.RoleName
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response DTO for user roles")
data class RoleResponse(
  @field:Schema(description = "Code identifier for the role", example = "ROLE_USER", implementation = RoleName::class)
  val code: RoleName,

  @field:Schema(description = "Name of the role", example = "User")
  val name: String,
)
