package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectRequest
import com.tianqueal.flowjet.backend.services.ProjectService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import com.tianqueal.flowjet.backend.utils.enums.ProjectAccessType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECTS}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
    name = "Project Management",
    description = "Endpoints for managing projects",
)
class ProjectController(
    private val projectService: ProjectService,
) {
    @Operation(
        summary = "Get all projects",
        description = "Retrieves a paginated list of all projects for the authenticated user",
    )
    @GetMapping
    fun getAllProjects(
        @Parameter(description = "Filter projects by access type", example = "ALL_ACCESSIBLE")
        @RequestParam(required = false) accessType: ProjectAccessType = ProjectAccessType.ALL_ACCESSIBLE,
        @Parameter(description = "Filter by project name (case-insensitive, partial match)")
        @RequestParam(required = false) name: String?,
        @Parameter(description = "Filter by project description (case-insensitive, partial match)")
        @RequestParam(required = false) description: String?,
        @Parameter(description = "Filter by project status ID")
        @RequestParam(required = false) projectStatusId: Int?,
        @ParameterObject
        @PageableDefault(
            page = PaginationConstants.DEFAULT_PAGE_NUMBER,
            size = PaginationConstants.DEFAULT_PAGE_SIZE,
        )
        pageable: Pageable,
    ): ResponseEntity<PagedModel<ProjectResponse>> =
        ResponseEntity.ok(
            PagedModel(
                projectService.findAll(accessType, name, description, projectStatusId, pageable),
            ),
        )

    @Operation(
        summary = "Get project by ID",
        description = "Retrieves a project by its unique ID. Requires user to be a member of the project.",
    )
    @GetMapping("/{id}")
    fun getProjectById(
        @Parameter(description = "Unique ID of the project")
        @PathVariable id: Long,
    ): ResponseEntity<ProjectResponse> = ResponseEntity.ok(projectService.findById(id))

    @Operation(
        summary = "Create a new project",
        description = "Creates a new project with the provided details. Requires user to be an authenticated member.",
    )
    @PostMapping
    fun createProject(
        @SwaggerRequestBody(description = "Details of the project to create")
        @Valid
        @RequestBody createProjectRequest: CreateProjectRequest,
    ): ResponseEntity<ProjectResponse> {
        val createdProject = projectService.create(createProjectRequest)
        val location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProject.id)
                .toUri()

        return ResponseEntity.created(location).body(createdProject)
    }

    @Operation(
        summary = "Update project by ID",
        description = "Updates an existing project's details. Requires user to be a owner of the project.",
    )
    @PutMapping("/{id}")
    fun updateProject(
        @Parameter(description = "ID of the project to update")
        @PathVariable id: Long,
        @SwaggerRequestBody(description = "Updated project data")
        @Valid
        @RequestBody updateProjectRequest: UpdateProjectRequest,
    ): ResponseEntity<ProjectResponse> = ResponseEntity.ok(projectService.update(id, updateProjectRequest))

    @Operation(
        summary = "Delete project by ID",
        description = "Deletes a project by its unique ID. Requires user to be an owner of the project.",
    )
    @DeleteMapping("/{id}")
    fun deleteProject(
        @Parameter(description = "Unique ID of the project to delete")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        projectService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
