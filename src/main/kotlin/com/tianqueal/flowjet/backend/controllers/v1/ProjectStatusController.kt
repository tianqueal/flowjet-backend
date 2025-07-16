package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse
import com.tianqueal.flowjet.backend.services.ProjectStatusService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECT_STATUSES}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
    name = "Project Status Management",
    description =
        "Endpoints for retrieving available project status options. " +
            "These statuses are used for categorizing and tracking the lifecycle of projects.",
)
class ProjectStatusController(
    private val projectStatusService: ProjectStatusService,
) {
    @Operation(summary = "Get all project statuses")
    @GetMapping
    fun getAllTaskStatuses(): ResponseEntity<List<ProjectStatusResponse>> = ResponseEntity.ok(projectStatusService.findAll())
}
