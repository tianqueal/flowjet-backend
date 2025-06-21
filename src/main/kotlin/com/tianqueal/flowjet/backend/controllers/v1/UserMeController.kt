package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.annotations.ProtectedOperationErrorResponses
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserSelfRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import com.tianqueal.flowjet.backend.utils.functions.AuthFunctions
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.USERS_ME}")
@Tag(
  name = "Current User Management",
  description = "Endpoints for managing the current user's profile"
)
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@PreAuthorize("isAuthenticated()")
class UserMeController(
  private val userService: UserService
) {
  @Operation(
    summary = "Get current authenticated user's details",
    description = "Retrieves the profile information of the currently logged-in user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User profile retrieved successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = UserResponse::class)
        )]
      )
    ]
  )
  @ProtectedOperationErrorResponses
  @GetMapping
  fun getCurrentUser(): ResponseEntity<UserResponse> {
    val usernameOrEmail = AuthFunctions.getAuthenticatedUsernameOrEmail()
    val currentUser = userService.findByUsernameOrEmail(usernameOrEmail)
    return ResponseEntity.ok(currentUser)
  }

  @Operation(
    summary = "Update current user's profile",
    description = "Updates the profile information of the currently logged-in user."
  )
  @SwaggerRequestBody(
    description = "Updated user data",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = UpdateUserSelfRequest::class)
    )]
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User profile updated successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = UserResponse::class)
        )]
      )
    ]
  )
  @ProtectedOperationErrorResponses
  @PutMapping
  fun updateCurrentUser(
    @Valid @RequestBody updateSelfRequestDto: UpdateUserSelfRequest
  ): ResponseEntity<UserResponse> {
    val username = AuthFunctions.getAuthenticatedUsernameOrEmail()
    val updatedUser = userService.updateCurrentUser(username, updateSelfRequestDto)
    return ResponseEntity.ok(updatedUser)
  }
}
