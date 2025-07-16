package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository :
    JpaRepository<TaskEntity, Long>,
    JpaSpecificationExecutor<TaskEntity> {
    @EntityGraph(attributePaths = ["status", "owner"])
    override fun findAll(
        spec: Specification<TaskEntity>?,
        pageable: Pageable,
    ): Page<TaskEntity>

    @EntityGraph(attributePaths = ["status", "project", "owner"])
    fun findWithStatusProjectAndOwnerByIdAndProjectId(
        id: Long,
        projectId: Long,
    ): TaskEntity?

    @EntityGraph(attributePaths = ["project", "owner"])
    fun findWithProjectAndOwnerByIdAndProjectId(
        id: Long,
        projectId: Long,
    ): TaskEntity?
}
