package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.tianqueal.flowjet.backend.utils.enums.ProjectStatusEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "DTO for Project Status")
data class ProjectStatusResponse(
  @field:Schema(description = "Unique identifier of the project status")
  val id: Int,

  @field:Schema(description = "Code of the project status")
  val code: ProjectStatusEnum,

  @field:Schema(description = "Name of the project status")
  val name: String,
)
