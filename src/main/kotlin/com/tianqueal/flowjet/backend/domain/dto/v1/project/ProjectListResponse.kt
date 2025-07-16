package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserProfileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Response containing a list of projects")
data class ProjectListResponse(
    @field:Schema(description = "Unique identifier of the project", example = "1")
    val id: Long,
    @field:Schema(description = "Name of the project", example = "Project Alpha")
    val name: String,
    @field:Schema(description = "Status of the project")
    val status: ProjectStatusResponse,
    @field:Schema(description = "User who created the project")
    val owner: UserProfileResponse,
    val memberCount: Int,
    @field:Schema(description = "Creation timestamp of the project", example = "2020-01-01T00:00:00Z")
    val createdAt: Instant,
)
