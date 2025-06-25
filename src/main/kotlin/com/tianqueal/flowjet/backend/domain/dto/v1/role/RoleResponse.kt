package com.tianqueal.flowjet.backend.domain.dto.v1.role

import com.tianqueal.flowjet.backend.utils.enums.RoleEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response DTO for user roles")
data class RoleResponse(
  @field:Schema(description = "Code identifier for the role", example = "ROLE_USER", implementation = RoleEnum::class)
  val code: RoleEnum,

  @field:Schema(description = "Name of the role", example = "User")
  val name: String,
)
