package com.tianqueal.flowjet.backend.domain.dto.v1.project

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response for project member invitation request")
data class ProjectMemberInvitationResponse(
    @field:Schema(description = "Message indicating the result of the project member invitation request")
    val message: String,
)
