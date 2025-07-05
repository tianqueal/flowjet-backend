package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.utils.enums.TaskStatusEnum
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Schema(description = "Task Status entity representing the status of a task")
@Entity
@Table(name = "task_statuses")
class TaskStatusEntity(
    @field:Schema(description = "Unique identifier for the task status")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Schema(description = "Code representing the task status")
    @Column(name = "code", unique = true, nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    var code: TaskStatusEnum,
    @field:Schema(description = "Name of the task status")
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
)
