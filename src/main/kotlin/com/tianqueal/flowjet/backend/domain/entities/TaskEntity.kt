package com.tianqueal.flowjet.backend.domain.entities

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Schema(description = "Task entity representing a task in the project")
@Entity
@Table(name = "tasks")
class TaskEntity(
    @field:Schema(description = "Unique identifier of the task")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Schema(description = "Name of the task")
    @Column(name = "name", nullable = false, length = 100)
    var name: String,
    @field:Schema(description = "Description of the task")
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,
    @field:Schema(description = "Status of the task")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_status_id", nullable = false)
    var status: TaskStatusEntity,
    @field:Schema(description = "Project to which the task belongs")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: ProjectEntity,
    @field:Schema(description = "User who created the task")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var owner: UserEntity,
    @field:Schema(description = "")
    @Column(name = "due_date")
    var dueDate: Instant? = null,
    @field:Schema(description = "Creation timestamp of the project")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @field:Schema(description = "Last updated timestamp of the project")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
//    @field:Schema(description = "Deletion timestamp of the project, if deleted", nullable = true)
//    @Column(name = "deleted_at", nullable = true)
//    var deletedAt: Instant? = null,
) {
    val safeId: Long
        get() =
            id
                ?: throw IllegalStateException("ID not initialized for $this")
}
