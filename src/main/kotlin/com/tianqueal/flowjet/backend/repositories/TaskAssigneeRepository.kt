package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.TaskAssigneeEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.TaskAssigneeId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TaskAssigneeRepository :
    JpaRepository<TaskAssigneeEntity, TaskAssigneeId>,
    JpaSpecificationExecutor<TaskAssigneeEntity> {
    @EntityGraph(attributePaths = ["user"])
    override fun findAll(
        spec: Specification<TaskAssigneeEntity>?,
        page: Pageable,
    ): Page<TaskAssigneeEntity>
}
