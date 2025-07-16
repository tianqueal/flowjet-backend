package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.domain.entities.keys.TaskAssigneeId
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Schema(description = "Entity representing the assignment of a user to a task")
@Entity
@Table(name = "task_assignees")
class TaskAssigneeEntity(
    @field:Schema(description = "Composite key for the task assignee entity")
    @EmbeddedId
    var id: TaskAssigneeId,
    @field:Schema(description = "Task to which the assignee belongs")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    var task: TaskEntity,
    @field:Schema(description = "User who is a member of the project")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @field:Schema(description = "Timestamp when the task assignee was added to the task")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
