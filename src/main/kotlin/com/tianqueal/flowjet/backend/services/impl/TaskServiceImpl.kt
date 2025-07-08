package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskRequest
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.mappers.v1.TaskMapper
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.repositories.TaskRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.services.TaskService
import com.tianqueal.flowjet.backend.specifications.TaskSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
    private val authenticatedUserService: AuthenticatedUserService,
    private val projectPermissionService: ProjectPermissionService,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
) : TaskService {
    @Transactional(readOnly = true)
    override fun findAll(
        projectId: Long,
        name: String?,
        description: String?,
        statusId: Int?,
        pageable: Pageable,
    ): Page<TaskResponse> {
        val projectEntity = projectRepository.findByIdOrNull(projectId) ?: throw ProjectNotFoundException(projectId)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRead(projectEntity, userId)) {
            throw AuthorizationDeniedException("You do not have permission to access tasks in this project.")
        }
        val projectSpec = TaskSpecification.belongsToProject(projectId)
        val filterSpec = TaskSpecification.filterBy(name, description, statusId)
        return taskRepository.findAll(projectSpec.and(filterSpec), pageable).map(taskMapper::toDto)
    }

    @Transactional(readOnly = true)
    override fun findById(
        projectId: Long,
        id: Long,
    ): TaskResponse {
        val taskEntity =
            taskRepository.findByIdAndProjectIdWithProject(id, projectId)
                ?: throw TaskNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canRead(taskEntity.project, userId)) {
            throw AuthorizationDeniedException("You do not have permission to access this task.")
        }
        return taskEntity.let(taskMapper::toDto)
    }

    override fun create(
        projectId: Long,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse {
        val projectEntity =
            projectRepository.findByIdOrNull(projectId)
                ?: throw ProjectNotFoundException(projectId)
        return create(
            projectEntity = projectEntity,
            userEntity = authenticatedUserService.getAuthenticatedUserEntity(),
            createTaskRequest = createTaskRequest,
        )
    }

    override fun create(
        projectEntity: ProjectEntity,
        userEntity: UserEntity,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse {
        if (!projectPermissionService.canCreateTask(projectEntity, userEntity.safeId)) {
            throw AuthorizationDeniedException("You do not have permission to create tasks in this project.")
        }

        return taskRepository
            .save(
                taskMapper.toEntity(
                    dto = createTaskRequest,
                    project = projectEntity,
                    user = userEntity,
                ),
            ).let(taskMapper::toDto)
    }

    override fun create(
        projectId: Long,
        userId: Long,
        createTaskRequest: CreateTaskRequest,
    ): TaskResponse {
        val projectEntity =
            projectRepository.findByIdOrNull(projectId)
                ?: throw ProjectNotFoundException(projectId)
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(userId)
        return create(
            projectEntity = projectEntity,
            userEntity = userEntity,
            createTaskRequest = createTaskRequest,
        )
    }

    override fun update(
        projectId: Long,
        id: Long,
        updateTaskRequest: UpdateTaskRequest,
    ): TaskResponse {
        val taskEntity =
            taskRepository.findByIdAndProjectIdWithProject(id, projectId)
                ?: throw TaskNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canUpdateTask(
                projectEntity = taskEntity.project,
                taskEntity = taskEntity,
                userId = userId,
            )
        ) {
            throw AuthorizationDeniedException("You do not have permission to update this task.")
        }
        taskMapper.updateEntityFromDto(updateTaskRequest, taskEntity)
        return taskRepository.save(taskEntity).let(taskMapper::toDto)
    }

    override fun delete(
        projectId: Long,
        id: Long,
    ) {
        val taskEntity =
            taskRepository.findByIdAndProjectIdWithProject(id, projectId)
                ?: throw TaskNotFoundException(id)
        val userId = authenticatedUserService.getAuthenticatedUserId()
        if (!projectPermissionService.canDeleteTask(
                projectEntity = taskEntity.project,
                taskEntity = taskEntity,
                userId = userId,
            )
        ) {
            throw AuthorizationDeniedException("You do not have permission to delete this task.")
        }
        taskRepository.delete(taskEntity)
    }
}
