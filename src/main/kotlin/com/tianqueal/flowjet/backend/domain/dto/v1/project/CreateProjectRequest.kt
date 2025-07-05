package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.project.ProjectDescriptionConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.project.ProjectNameConstraint
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request DTO for creating a new project")
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateProjectRequest(
    @field:ProjectNameConstraint
    val name: String,
    @field:ProjectDescriptionConstraint
    val description: String? = null,
    @field:NotNull(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_NOT_NULL)
    @field:Positive(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_POSITIVE)
    val statusId: Int,
)
