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

@Schema(description = "Represents a single comment in the system")
@Entity
@Table(name = "task_comments")
class TaskCommentEntity(
    @field:Schema(description = "Unique identifier of the task comment")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Schema(description = "Task to which this comment belongs")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    var task: TaskEntity,
    @field:Schema(description = "User who authored the task comment")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var author: UserEntity,
    @field:Schema(description = "Content of the comment", example = "This is a comment on the task.")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,
    @field:Schema(description = "Parent comment if this is a reply to another comment", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_comment_id")
    var parent: TaskCommentEntity? = null,
//    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
//    var replies: MutableSet<TaskCommentEntity> = mutableSetOf(),
    @field:Schema(description = "Timestamp when the comment was created")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @field:Schema(description = "Timestamp when the comment was last updated")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    val safeId: Long
        get() =
            id
                ?: throw IllegalStateException("ID not initialized for $this")
}
