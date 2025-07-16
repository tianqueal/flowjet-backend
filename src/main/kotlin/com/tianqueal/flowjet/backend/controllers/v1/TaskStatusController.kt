package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskStatusResponse
import com.tianqueal.flowjet.backend.services.TaskStatusService
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
@RequestMapping("${ApiPaths.V1}${ApiPaths.TASK_STATUSES}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
@Tag(
    name = "Task Status Management",
    description =
        "Endpoints for retrieving available task status options. " +
            "These statuses are used for categorizing and tracking task progress within projects.",
)
class TaskStatusController(
    private val taskStatusService: TaskStatusService,
) {
    @Operation(summary = "Get all task statuses")
    @GetMapping
    fun getAllTaskStatuses(): ResponseEntity<List<TaskStatusResponse>> = ResponseEntity.ok(taskStatusService.findAll())
}
