package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository :
    JpaRepository<TaskEntity, Long>,
    JpaSpecificationExecutor<TaskEntity> {
    @Query("SELECT t FROM TaskEntity t JOIN FETCH t.project WHERE t.id = :id AND t.project.id = :projectId")
    fun findByIdAndProjectIdWithProject(id: Long, projectId: Long): TaskEntity?
}
