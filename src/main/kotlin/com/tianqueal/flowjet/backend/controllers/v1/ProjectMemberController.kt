package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberInvitationResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectMemberRequest
import com.tianqueal.flowjet.backend.services.ProjectMemberService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import com.tianqueal.flowjet.backend.utils.constants.PaginationConstants
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
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
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.PROJECTS}/{projectId}${ApiPaths.MEMBERS}")
@Tag(
    name = "Project Member Management",
    description = "Endpoints for managing project members",
)
class ProjectMemberController(
    private val projectMemberService: ProjectMemberService,
    private val messageSource: MessageSource,
) {
    @Operation(summary = "Get all members of a project")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
    fun getAllProjectMembers(
        @Parameter(description = "ID of the project to retrieve members for")
        @PathVariable projectId: Long,
        @Parameter(description = "Filter by member role ID")
        @RequestParam(required = false) memberRoleId: Int? = null,
        @Parameter(description = "Filter by username (case-insensitive, partial match)")
        @RequestParam(required = false) username: String? = null,
        @ParameterObject
        @PageableDefault(
            page = PaginationConstants.DEFAULT_PAGE_NUMBER,
            size = PaginationConstants.DEFAULT_PAGE_SIZE,
        )
        pageable: Pageable,
    ): ResponseEntity<Page<ProjectMemberResponse>> =
        ResponseEntity.ok(projectMemberService.findAll(projectId, memberRoleId, username, pageable))

    @Operation(summary = "Invite a new member to a project")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
    fun inviteProjectMember(
        @Parameter(description = "ID of the project to add the member to")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the user to add as a member")
        @SwaggerRequestBody
        @Valid
        @RequestBody projectMemberInvitationRequest: ProjectMemberInvitationRequest,
    ): ResponseEntity<ProjectMemberInvitationResponse> {
        projectMemberService.inviteProjectMember(
            projectId = projectId,
            projectMemberInvitationRequest = projectMemberInvitationRequest,
            apiVersionPath = ApiPaths.V1,
        )
        val locale = LocaleContextHolder.getLocale()
        val message =
            messageSource.getMessage(
                MessageKeys.PROJECT_MEMBER_INVITATION_SENT,
                null,
                locale,
            )
        return ResponseEntity
            .accepted()
            .body(ProjectMemberInvitationResponse(message = message))
    }

    @Operation(summary = "Accept a project member invitation")
    @GetMapping("/accept-invitation")
    fun acceptProjectMemberInvitation(
        @Parameter(description = "ID of the project to add the member to")
        @PathVariable projectId: Long,
        @Parameter(description = "Invitation token for accepting the project member invitation")
        @RequestParam token: String,
    ): ResponseEntity<ProjectMemberInvitationResponse> {
        projectMemberService.verifyTokenAndAcceptInvitation(projectId = projectId, token = token)
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.PROJECT_MEMBER_INVITATION_SUCCESS, null, locale)
        return ResponseEntity.ok(ProjectMemberInvitationResponse(message))
    }

    @Operation(summary = "Update a project member's role")
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
    fun updateProjectMemberRole(
        @Parameter(description = "ID of the project to update the member in")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the user whose role is being updated")
        @PathVariable userId: Long,
        @SwaggerRequestBody @Valid @RequestBody updateProjectMemberRequest: UpdateProjectMemberRequest,
    ): ResponseEntity<ProjectMemberResponse> =
        ResponseEntity.ok(projectMemberService.updateMemberRole(projectId, userId, updateProjectMemberRequest))

    @Operation(summary = "Remove a member from a project")
    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_BEARER)
    fun deleteProjectMember(
        @Parameter(description = "ID of the project to remove the member from")
        @PathVariable projectId: Long,
        @Parameter(description = "ID of the user to remove from the project")
        @PathVariable userId: Long,
    ): ResponseEntity<Void> {
        projectMemberService.removeProjectMember(projectId, userId)
        return ResponseEntity.noContent().build()
    }
}
