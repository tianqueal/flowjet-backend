package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
  name = "Admin User Management",
  description = "Endpoints for administrators to manage users"
)
class AdminUserController(
  private val userService: UserService,
) {
  @Operation(
    summary = "Get all users",
    description = "Retrieves a paginated list of all users in the system. Requires Admin privileges."
  )
  @GetMapping
  fun getAllUsers(
    @Parameter(description = "Filter by username (case-insensitive, partial match)")
    @RequestParam(required = false) username: String?,

    @Parameter(description = "Filter by email (case-insensitive, partial match)")
    @RequestParam(required = false) email: String?,

    @Parameter(description = "Filter by user name (case-insensitive, partial match)")
    @RequestParam(required = false) name: String?,

    @PageableDefault(
      page = PaginationConstants.DEFAULT_PAGE_NUMBER,
      size = PaginationConstants.DEFAULT_PAGE_SIZE,
    )
    pageable: Pageable,
  ): ResponseEntity<Page<UserResponse>> =
    ResponseEntity.ok(userService.findAll(username, email, name, pageable))

  @Operation(
    summary = "Get user by ID",
    description = "Retrieves a user by their unique ID. Requires Admin privileges."
  )
  @GetMapping("/{id}")
  fun getUserById(
    @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
    @PathVariable id: Long,
  ): ResponseEntity<UserResponse> = ResponseEntity.ok(userService.findById(id))

  @Operation(
    summary = "Create a new user",
    description = "Registers a new user in the system. Requires Admin privileges."
  )
  @PostMapping
  fun createUser(
    @SwaggerRequestBody(description = "Details of the user to create")
    @Valid @RequestBody createUserRequest: CreateUserRequest,
  ): ResponseEntity<UserResponse> {
    val createdUser: UserResponse = userService.create(createUserRequest)
    val location = ServletUriComponentsBuilder
      .fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(createdUser.id)
      .toUri()
    return ResponseEntity.created(location).body(createdUser)
  }

  @Operation(
    summary = "Update user by ID",
    description = "Updates an existing user's details. Requires Admin privileges."
  )
  @PutMapping("/{id}")
  fun updateUser(
    @Parameter(description = "ID of the user to update")
    @PathVariable id: Long,

    @SwaggerRequestBody(description = "Updated user data")
    @Valid @RequestBody updateUserRequest: UpdateUserRequest,
  ): ResponseEntity<UserResponse> =
    ResponseEntity.ok(userService.update(id, updateUserRequest))
}
