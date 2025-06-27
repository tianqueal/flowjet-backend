package com.tianqueal.flowjet.backend.domain.dto.v1.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request to add a member to a project")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectMemberInvitationRequest(
    @field:Schema(description = "Id of the user to add as a member")
    @field:NotNull(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_NOT_NULL)
    @field:Positive(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_POSITIVE)
    val userId: Long,
    @field:Schema(description = "ID of the role to assign to the member")
    @field:NotNull(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_NOT_NULL)
    @field:Positive(message = ValidationMessageKeys.VALIDATION_IDENTIFIER_NUMBER_POSITIVE)
    val memberRoleId: Int,
)
