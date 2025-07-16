package com.tianqueal.flowjet.backend.domain.dto.v1.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserEmailConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserNameConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserPasswordConstraint
import com.tianqueal.flowjet.backend.annotations.constrains.v1.user.UserUsernameConstraint
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User registration request")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRegisterRequest(
    @field:UserUsernameConstraint
    val username: String,
    @field:UserEmailConstraint
    val email: String,
    @field:UserNameConstraint
    val name: String,
    @field:UserPasswordConstraint
    val password: String,
)
