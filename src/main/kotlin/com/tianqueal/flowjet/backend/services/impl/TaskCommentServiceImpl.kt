package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.task.CreateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskCommentResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.task.UpdateTaskCommentRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.websocket.WebSocketEvent
import com.tianqueal.flowjet.backend.domain.entities.TaskCommentEntity
import com.tianqueal.flowjet.backend.exceptions.business.CommentNestingLimitExceededException
import com.tianqueal.flowjet.backend.exceptions.business.TaskCommentNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserIsNotProjectMemberException
import com.tianqueal.flowjet.backend.mappers.v1.TaskCommentMapper
import com.tianqueal.flowjet.backend.repositories.TaskCommentRepository
import com.tianqueal.flowjet.backend.repositories.TaskRepository
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.AuthenticatedUserService
import com.tianqueal.flowjet.backend.services.ProjectPermissionService
import com.tianqueal.flowjet.backend.services.TaskCommentService
import com.tianqueal.flowjet.backend.specifications.TaskCommentSpecification
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.CommentConstants
import com.tianqueal.flowjet.backend.utils.enums.WebSocketEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TaskCommentServiceImpl(
    private val taskCommentRepository: TaskCommentRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val taskCommentMapper: TaskCommentMapper,
    private val projectPermissionService: ProjectPermissionService,
    private val authenticatedUserService: AuthenticatedUserService,
    private val simpMessagingTemplate: SimpMessagingTemplate,
) : TaskCommentService {
    @Transactional(readOnly = true)
    override fun findAll(
        projectId: Long,
        taskId: Long,
        pageable: Pageable,
    ): Page<TaskCommentResponse> {
        val taskEntity =
            taskRepository.findWithProjectAndOwnerByIdAndProjectId(taskId, projectId)
                ?: throw TaskNotFoundException(taskId)
        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()

        if (!projectPermissionService.canRead(taskEntity.project.safeId, authenticatedUserId)) {
            throw AuthorizationDeniedException("Access denied: You do not have permission to view task comments for this resource.")
        }

        val rootTaskCommentsPage =
            taskCommentRepository.findAll(
                TaskCommentSpecification.filterBy(taskId = taskId, onlyRoots = true),
                pageable,
            )
        val rootTaskCommentIds = rootTaskCommentsPage.content.mapNotNull { it.id }

        if (rootTaskCommentIds.isEmpty()) {
            return rootTaskCommentsPage.map { taskCommentMapper.toDto(it) }
        }

        val replies =
            taskCommentRepository
                .findByParentIdIn(rootTaskCommentIds)
                .groupBy { it.parent?.id }
                .mapValues { entry -> entry.value.map { taskCommentMapper.toDto(it) } }

        val contentWithReplies =
            rootTaskCommentsPage.content.map { root ->
                taskCommentMapper.toDto(root, replies[root.id].orEmpty())
            }

        return PageImpl(contentWithReplies, pageable, rootTaskCommentsPage.totalElements)
    }

    override fun create(
        projectId: Long,
        taskId: Long,
        request: CreateTaskCommentRequest,
    ): TaskCommentResponse {
        val taskEntity =
            taskRepository.findWithProjectAndOwnerByIdAndProjectId(taskId, projectId)
                ?: throw TaskNotFoundException(taskId)
        val authorId = authenticatedUserService.getAuthenticatedUserId()

        if (!projectPermissionService.canCreateTaskComment(taskEntity.project, authorId)) {
            throw UserIsNotProjectMemberException(projectId, authorId)
        }

        val parentTaskComment =
            request.parentId?.let {
                val p =
                    taskCommentRepository.findByIdOrNull(it)
                        ?: throw TaskCommentNotFoundException(it)
                validateNestingDepth(p)
                p
            }

        val newComment =
            TaskCommentEntity(
                content = request.content,
                task = taskEntity,
                author = userRepository.getReferenceById(authorId),
                parent = parentTaskComment,
            )

        val savedTaskComment = taskCommentRepository.save(newComment)
        val taskCommentResponse = taskCommentMapper.toDto(savedTaskComment)

        val event = WebSocketEvent(eventType = WebSocketEventType.TASK_COMMENT_CREATED, payload = taskCommentResponse)
        val topicUri = buildTaskCommentTopicUri(projectId, taskId)
        simpMessagingTemplate.convertAndSend(topicUri, event)

        return taskCommentResponse
    }

    override fun update(
        projectId: Long,
        taskId: Long,
        id: Long,
        request: UpdateTaskCommentRequest,
    ): TaskCommentResponse {
        val taskCommentEntity =
            taskCommentRepository.findWithTaskAndAuthorByIdAndTaskId(id, taskId)
                ?: throw TaskCommentNotFoundException(id)

        if (taskCommentEntity.task.project.id != projectId) {
            throw TaskCommentNotFoundException(id)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()

        if (taskCommentEntity.author.id != authenticatedUserId) {
            throw AuthorizationDeniedException("Access denied: You can only update your own comments.")
        }

        taskCommentEntity.content = request.content
        val updatedTaskComment = taskCommentRepository.save(taskCommentEntity)
        val taskCommentResponse = updatedTaskComment.let(taskCommentMapper::toDto)

        val event = WebSocketEvent(WebSocketEventType.TASK_COMMENT_UPDATED, taskCommentResponse)
        val topicUri = buildTaskCommentTopicUri(projectId, taskId)
        simpMessagingTemplate.convertAndSend(topicUri, event)

        return taskCommentResponse
    }

    override fun delete(
        projectId: Long,
        taskId: Long,
        id: Long,
    ) {
        val taskCommentEntity =
            taskCommentRepository.findWithTaskAndAuthorByIdAndTaskId(id, taskId)
                ?: throw TaskCommentNotFoundException(id)

        if (taskCommentEntity.task.project.id != projectId) {
            throw TaskCommentNotFoundException(id)
        }

        val authenticatedUserId = authenticatedUserService.getAuthenticatedUserId()

        if (taskCommentEntity.author.id != authenticatedUserId &&
            !projectPermissionService.isProjectOwner(
                taskCommentEntity.task.project,
                authenticatedUserId,
            )
        ) {
            throw AuthorizationDeniedException("Access denied: You can only delete your own comments.")
        }

        taskCommentRepository.delete(taskCommentEntity)

        val event = WebSocketEvent(WebSocketEventType.TASK_COMMENT_DELETED, mapOf("id" to id))
        val topicUri = buildTaskCommentTopicUri(projectId, taskId)
        simpMessagingTemplate.convertAndSend(topicUri, event)
    }

    private fun validateNestingDepth(
        comment: TaskCommentEntity?,
        currentDepth: Int = 1,
    ) {
        if (currentDepth >= CommentConstants.MAX_COMMENT_DEPTH) {
            throw CommentNestingLimitExceededException(comment?.safeId)
        }
        comment?.parent?.let {
            validateNestingDepth(it, currentDepth + 1)
        }
    }

    private val buildTaskCommentTopicUri = { projectId: Long, taskId: Long ->
        "${ApiPaths.WS_TOPIC_PREFIX}${ApiPaths.PROJECTS}/$projectId${ApiPaths.TASKS}/$taskId${ApiPaths.COMMENTS}"
    }
}
