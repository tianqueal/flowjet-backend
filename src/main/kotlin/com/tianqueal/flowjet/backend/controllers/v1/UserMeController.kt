package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserProfileRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.USERS_ME}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
    name = "Current User Management",
    description = "Endpoints for managing the current user's profile",
)
class UserMeController(
    private val userService: UserService,
    private val authenticatedUserService: AuthenticatedUserService,
) {
    @Operation(
        summary = "Get current authenticated user's details",
        description = "Retrieves the profile information of the currently logged-in user.",
    )
    @GetMapping
    fun getCurrentUser(): ResponseEntity<UserResponse> {
        val currentUser = userService.findById(authenticatedUserService.getAuthenticatedUserId())
        return ResponseEntity.ok(currentUser)
    }

    @Operation(
        summary = "Update current user's profile",
        description = "Updates the profile information of the currently logged-in user.",
    )
    @PutMapping
    fun updateCurrentUser(
        @SwaggerRequestBody(description = "Updated user data")
        @Valid
        @RequestBody updateUserProfileRequest: UpdateUserProfileRequest,
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateProfile(updateUserProfileRequest)
        return ResponseEntity.ok(updatedUser)
    }
}
