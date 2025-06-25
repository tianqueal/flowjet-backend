package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "DTO for Project Member Response")
data class ProjectMemberResponse(
    @field:Schema(description = "User profile of the project member", implementation = UserProfileResponse::class)
    val member: UserProfileResponse,
    @field:Schema(description = "Role of the member in the project", implementation = MemberRoleEnum::class)
    val memberRole: MemberRoleEnum,
    @field:Schema(description = "Timestamp when the member joined the project", example = "2020-01-01T00:00:00Z")
    val memberSince: Instant,
)
