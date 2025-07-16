package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskCommentRequest
import com.tianqueal.flowjet.backend.services.TaskCommentService
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECTS}/{projectId}${ApiPaths.TASKS}/{taskId}${ApiPaths.COMMENTS}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(name = "Task Comment Management", description = "Endpoints for managing comments on a task")
class TaskCommentController(
    private val taskCommentService: TaskCommentService,
) {
    @Operation(summary = "Get all comments for a specific task")
    @GetMapping
    fun getCommentsForTask(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @ParameterObject
        @PageableDefault(
            page = PaginationConstants.DEFAULT_PAGE_NUMBER,
            size = PaginationConstants.DEFAULT_PAGE_SIZE,
        )
        pageable: Pageable,
    ): ResponseEntity<Page<TaskCommentResponse>> {
        val comments = taskCommentService.findAll(projectId, taskId, pageable)
        return ResponseEntity.ok(comments)
    }

    @Operation(summary = "Create a new comment on a task")
    @PostMapping
    fun createComment(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @SwaggerRequestBody
        @Valid
        @RequestBody
        request: CreateTaskCommentRequest,
    ): ResponseEntity<TaskCommentResponse> {
        val comment = taskCommentService.create(projectId, taskId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    @Operation(summary = "Update an existing comment")
    @PutMapping("/{commentId}")
    fun updateComment(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @Parameter(description = "ID of the comment to update")
        @PathVariable commentId: Long,
        @SwaggerRequestBody
        @Valid
        @RequestBody
        request: UpdateTaskCommentRequest,
    ): ResponseEntity<TaskCommentResponse> {
        val updatedComment = taskCommentService.update(projectId, taskId, commentId, request)
        return ResponseEntity.ok(updatedComment)
    }

    @Operation(summary = "Delete a comment")
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @Parameter(description = "ID of the project")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the task")
        @PathVariable taskId: Long,
        @Parameter(description = "ID of the comment to delete")
        @PathVariable commentId: Long,
    ): ResponseEntity<Void> {
        taskCommentService.delete(projectId, taskId, commentId)
        return ResponseEntity.noContent().build()
    }
}
