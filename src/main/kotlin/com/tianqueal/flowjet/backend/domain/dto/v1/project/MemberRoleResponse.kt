package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response DTO for a member's role in a project")
data class MemberRoleResponse(
    @field:Schema(description = "Unique identifier of the member role")
    val id: Int,
    @field:Schema(description = "Unique code of the member role")
    val code: MemberRoleEnum,
    @field:Schema(description = "Name of the member role")
    val name: String,
)
