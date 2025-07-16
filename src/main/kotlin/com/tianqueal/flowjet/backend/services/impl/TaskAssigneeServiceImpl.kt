package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskAssigneeResponse
import com.tianqueal.flowjet.backend.domain.entities.keys.TaskAssigneeId
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskAssigneeAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.TaskAssigneeNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserIsNotProjectMemberException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.v1.TaskAssigneeMapper
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.TaskAssigneeRepository
import com.tianqueal.flowjet.backend.repositories.TaskRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.services.TaskAssigneeService
import com.tianqueal.flowjet.backend.specifications.TaskAssigneeSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TaskAssigneeServiceImpl(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
    private val authenticatedUserService: AuthenticatedUserService,
    private val projectPermissionService: ProjectPermissionService,
    private val taskAssigneeMapper: TaskAssigneeMapper,
) : TaskAssigneeService {
    @Transactional(readOnly = true)
    override fun findAssigneesForTask(
        projectId: Long,
        taskId: Long,
        username: String?,
        name: String?,
        pageable: Pageable,
    ): Page<TaskAssigneeResponse> {
        if (!projectRepository.existsById(projectId)) {
            throw ProjectNotFoundException(projectId)
        }
        if (!taskRepository.existsById(taskId)) {
            throw TaskNotFoundException(taskId)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRead(projectId, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to view task assignees of this project.",
            )
        }

        return taskAssigneeRepository
            .findAll(
                TaskAssigneeSpecification.filterBy(taskId, username, name),
                pageable,
            ).map(taskAssigneeMapper::toDto)
    }

    override fun assignUser(
        projectId: Long,
        taskId: Long,
        userId: Long,
    ): TaskAssigneeResponse {
        val taskEntity =
            taskRepository.findWithProjectAndOwnerByIdAndProjectId(taskId, projectId)
                ?: throw TaskNotFoundException(taskId)
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(userId)
        if (!projectMemberRepository.isMember(projectId, userId)) {
            throw UserIsNotProjectMemberException(projectId, userId)
        }
        if (taskAssigneeRepository.existsById(TaskAssigneeId(taskId, userId))) {
            throw TaskAssigneeAlreadyExistsException(taskId, userId)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canAddTaskAssignee(taskEntity.project, taskEntity, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to assign users to tasks in this project.",
            )
        }

        val taskAssigneeEntity =
            taskAssigneeMapper.toEntity(
                taskEntity = taskEntity,
                userEntity = userEntity,
            )

        return taskAssigneeRepository.save(taskAssigneeEntity).let(taskAssigneeMapper::toDto)
    }

    override fun unassignUser(
        projectId: Long,
        taskId: Long,
        userId: Long,
    ) {
        val taskEntity =
            taskRepository.findWithProjectAndOwnerByIdAndProjectId(taskId, projectId)
                ?: throw TaskNotFoundException(taskId)
        val taskAssigneeId = TaskAssigneeId(taskEntity.safeId, userId)
        if (!taskAssigneeRepository.existsById(taskAssigneeId)) {
            throw TaskAssigneeNotFoundException(taskId, userId)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRemoveTaskAssignee(taskEntity.project, taskEntity, authenticatedUserId)) {
            throw AuthorizationDeniedException(
                "Access denied: You do not have permission to unassign users from tasks in this project.",
            )
        }

        taskAssigneeRepository.deleteById(taskAssigneeId)
    }
}
