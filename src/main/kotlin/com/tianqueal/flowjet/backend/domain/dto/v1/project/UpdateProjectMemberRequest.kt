package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request to update a member's role in a project")
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateProjectMemberRequest(
    @field:Schema(description = "Id of the member role to update")
    @field:NotNull(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_NOT_NULL)
    @field:Positive(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_POSITIVE)
    val memberRoleId: Int,
)
