package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserEmailConstraint
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to reset user password")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PasswordResetRequest(
    @field:UserEmailConstraint
    val email: String,
)
