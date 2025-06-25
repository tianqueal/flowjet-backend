package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.project.ProjectDescriptionConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.project.ProjectNameConstraint
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Request DTO for creating a new project")
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateProjectRequest(
  @field:ProjectNameConstraint
  var name: String,

  @field:ProjectDescriptionConstraint
  var description: String? = null,

  @field:NotNull
  var projectStatusId: Int,
)
