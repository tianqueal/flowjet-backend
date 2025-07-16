package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskAssigneeResponse
import com.tianqueal.flowjet.backend.services.TaskAssigneeService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECTS}/{projectId}${ApiPaths.TASKS}/{taskId}/assignees")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(name = "Task Assignee Management", description = "Endpoints for managing users assigned to tasks within a project")
class TaskAssigneeController(
    private val taskAssigneeService: TaskAssigneeService,
) {
    @Operation(summary = "Get all users assigned to a specific task")
    @GetMapping
    fun getAssigneesForTask(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task to retrieve assignees from")
        @PathVariable taskId: Long,
        @Parameter(description = "Filter by username (case-insensitive, partial match)")
        @RequestParam(required = false) username: String? = null,
        @Parameter(description = "Filter by user name (case-insensitive, partial match)")
        @RequestParam(required = false) name: String? = null,
        @ParameterObject
        @PageableDefault(
            page = PaginationConstants.DEFAULT_PAGE_NUMBER,
            size = PaginationConstants.DEFAULT_PAGE_SIZE,
        )
        pageable: Pageable,
    ): ResponseEntity<Page<TaskAssigneeResponse>> {
        val assignees = taskAssigneeService.findAssigneesForTask(projectId, taskId, username, name, pageable)
        return ResponseEntity.ok(assignees)
    }

    @Operation(summary = "Assign a user to a specific task")
    @PostMapping("/{userId}")
    fun assignUserToTask(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @Parameter(description = "ID of the user to assign")
        @PathVariable userId: Long,
    ): ResponseEntity<TaskAssigneeResponse> {
        val newAssignment = taskAssigneeService.assignUser(projectId, taskId, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(newAssignment)
    }

    @Operation(summary = "Remove a user assignment from a task")
    @DeleteMapping("/{userId}")
    fun removeUserFromTask(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @Parameter(description = "ID of the user to unassign")
        @PathVariable userId: Long,
    ): ResponseEntity<Void> {
        taskAssigneeService.unassignUser(projectId, taskId, userId)
        return ResponseEntity.noContent().build()
    }
}
