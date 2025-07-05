package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.TaskStatusEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskStatusRepository : JpaRepository<TaskStatusEntity, Int>
