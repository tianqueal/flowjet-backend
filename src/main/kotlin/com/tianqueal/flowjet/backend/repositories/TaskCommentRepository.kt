package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.TaskCommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TaskCommentRepository :
    JpaRepository<TaskCommentEntity, Long>,
    JpaSpecificationExecutor<TaskCommentEntity> {
    @EntityGraph(attributePaths = ["author"])
    override fun findAll(
        spec: Specification<TaskCommentEntity>?,
        pageable: Pageable,
    ): Page<TaskCommentEntity>

    @EntityGraph(attributePaths = ["author"])
    fun findByParentIdIn(parentIds: List<Long>): List<TaskCommentEntity>

    @EntityGraph(attributePaths = ["task", "author"])
    fun findWithTaskAndAuthorByIdAndTaskId(
        id: Long,
        taskId: Long,
    ): TaskCommentEntity?
}
