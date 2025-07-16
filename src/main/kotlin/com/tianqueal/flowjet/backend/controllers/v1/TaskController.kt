package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskRequest
import com.tianqueal.flowjet.backend.services.TaskService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECTS}/{projectId}${ApiPaths.TASKS}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
    name = "Task Management",
    description = "Endpoints for managing tasks within a project",
)
class TaskController(
    private val taskService: TaskService,
) {
    @Operation(summary = "Get all tasks for a project")
    @GetMapping
    fun getAllTasks(
        @Parameter(description = "ID of the project to retrieve tasks for")
        @PathVariable projectId: Long,
        @Parameter(description = "Filter by task name (case-insensitive, partial match)")
        @RequestParam(required = false) name: String? = null,
        @Parameter(description = "Filter by task status ID")
        @RequestParam(required = false) statusId: Int? = null,
        @ParameterObject
        @PageableDefault(
            page = PaginationConstants.DEFAULT_PAGE_NUMBER,
            size = PaginationConstants.DEFAULT_PAGE_SIZE,
        )
        pageable: Pageable,
    ): ResponseEntity<Page<TaskResponse>> = ResponseEntity.ok(taskService.findAll(projectId, name, statusId, pageable))

    @Operation(summary = "Get a specific task by ID")
    @GetMapping("/{id}")
    fun getTaskById(
        @Parameter(description = "ID of the project to which the task belongs")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task to retrieve")
        @PathVariable id: Long,
    ): ResponseEntity<TaskResponse> =
        ResponseEntity.ok(
            taskService.findById(projectId, id),
        )

    @Operation(summary = "Create a new task. Allowed for project members except viewers.")
    @PostMapping
    fun createTask(
        @Parameter(description = "ID of the project to create the task in")
        @PathVariable projectId: Long,
        @SwaggerRequestBody(description = "Details of the task to create")
        @Valid
        @RequestBody createTaskRequest: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        val createdTask = taskService.create(projectId = projectId, createTaskRequest = createTaskRequest)
        val location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.id)
                .toUri()
        return ResponseEntity.created(location).body(createdTask)
    }

    @Operation(summary = "Update an existing task by ID. Allowed for project owner and/or task owner.")
    @PutMapping("/{id}")
    fun updateTask(
        @Parameter(description = "ID of the project to which the task belongs")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task to update")
        @PathVariable id: Long,
        @SwaggerRequestBody(description = "Updated task data")
        @Valid
        @RequestBody updateTaskRequest: UpdateTaskRequest,
    ): ResponseEntity<TaskResponse> = ResponseEntity.ok(taskService.update(projectId, id, updateTaskRequest))

    @Operation(summary = "Delete a task by ID. Allowed for project owner and/or task owner.")
    @DeleteMapping("/{id}")
    fun deleteTask(
        @Parameter(description = "ID of the project to which the task belongs")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task to delete")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        taskService.delete(projectId, id)
        return ResponseEntity.noContent().build()
    }
}
