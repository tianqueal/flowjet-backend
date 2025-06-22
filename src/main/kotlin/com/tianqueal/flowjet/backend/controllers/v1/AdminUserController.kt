package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.annotations.ProtectedOperationErrorResponses
import com.tianqueal.flowjet.backend.domain.dto.v1.error.ErrorResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserByAdminRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}")
@Tag(
  name = "Admin User Management",
  description = "Endpoints for administrators to manage users"
)
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
  private val userService: UserService
) {
  @Operation(
    summary = "Get all users",
    description = "Retrieves a paginated list of all users in the system. Requires Admin privileges."
  )
  @ApiResponse(
    responseCode = "200",
    description = "List of users retrieved successfully",
    useReturnTypeSchema = true
  )
  @ProtectedOperationErrorResponses
  @Parameters(
    value = [
      Parameter(
        description = "Filter by username (case-insensitive, partial match)",
        name = "username",
        example = "john_doe",
        schema = Schema(type = "string")
      ),
      Parameter(
        description = "Filter by email (case-insensitive, partial match)",
        name = "email",
        example = "user@example.com",
        schema = Schema(type = "string")
      ),
      Parameter(
        description = "Filter by user name (case-insensitive, partial match)",
        name = "name",
        example = "John",
        schema = Schema(type = "string")
      ),
      Parameter(
        description = "Zero-based page index (0..N)",
        name = "page",
        schema = Schema(type = "integer", defaultValue = "0")
      ),
      Parameter(
        description = "The size of the page to be returned",
        name = "size",
        schema = Schema(type = "integer", defaultValue = "20"),
      ),
      Parameter(
        description = "Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.",
        name = "sort",
        content = [Content(array = ArraySchema(schema = Schema(type = "string")))]
      ),
    ]
  )
  @GetMapping
  fun getAllUsers(
    @RequestParam(required = false) username: String?,
    @RequestParam(required = false) email: String?,
    @RequestParam(required = false) name: String?,

    @PageableDefault(
      page = PaginationConstants.DEFAULT_PAGE_NUMBER,
      size = PaginationConstants.DEFAULT_PAGE_SIZE,
    )
    @Parameter(hidden = true)
    pageable: Pageable
  ): ResponseEntity<PagedModel<UserResponse>> =
    ResponseEntity.ok(PagedModel(userService.findAll(username, email, name, pageable)))

  @Operation(
    summary = "Get user by ID",
    description = "Retrieves a user by their unique ID. Requires Admin privileges."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User found",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = UserResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      ),
    ]
  )
  @ProtectedOperationErrorResponses
  @GetMapping("/{id}")
  fun getUserById(
    @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
    @PathVariable id: Long
  ): ResponseEntity<UserResponse> = ResponseEntity.ok(userService.findById(id))

  @Operation(
    summary = "Create a new user",
    description = "Registers a new user in the system. Requires Admin privileges."
  )
  @SwaggerRequestBody(
    description = "User data to create",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = CreateUserRequest::class)
    )]
  )
  @ApiResponses(
    value = [ApiResponse(
      responseCode = "201",
      description = "User created successfully",
      content = [Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = Schema(implementation = UserResponse::class)
      )]
    ), ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = [Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = Schema(implementation = ErrorResponse::class)
      )]
    ), ApiResponse(
      responseCode = "409",
      description = "Conflict - user already exists with the provided username or email",
      content = [Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = Schema(implementation = ErrorResponse::class)
      )]
    )]
  )
  @ProtectedOperationErrorResponses
  @PostMapping
  fun createUser(
    @Valid @RequestBody createUserRequest: CreateUserRequest
  ): ResponseEntity<UserResponse> {
    val createdUser: UserResponse = userService.createUserByAdmin(createUserRequest)
    val location = ServletUriComponentsBuilder
      .fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(createdUser.id)
      .toUri()
    return ResponseEntity.created(location).body(createdUser)
  }

  @Operation(summary = "Update user by ID", description = "Updates an existing user's details. Requires ADMIN role.")
  @SwaggerRequestBody(
    description = "Updated user data",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = UpdateUserByAdminRequest::class)
    )]
  )
  @ApiResponses(
    value = [ApiResponse(
      responseCode = "200",
      description = "User updated successfully",
      content = [Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = Schema(implementation = UserResponse::class)
      )]
    ), ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = [Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = Schema(implementation = ErrorResponse::class)
      )]
    )]
  )
  @ProtectedOperationErrorResponses
  @PutMapping("/{id}")
  fun updateUser(
    @Parameter(description = "ID of the user to update", required = true, example = "1")
    @PathVariable id: Long,
    @Valid @RequestBody updateUserRequest: UpdateUserByAdminRequest
  ): ResponseEntity<UserResponse> =
    ResponseEntity.ok(userService.updateUserByAdmin(id, updateUserRequest))
}
