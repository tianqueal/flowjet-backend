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

@Schema(description = "Project entity representing a project in the system")
@Entity
@Table(name = "projects")
class ProjectEntity(
    @field:Schema(description = "Unique identifier of the project", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Schema(description = "Name of the project", example = "Project Alpha")
    @Column(name = "name", nullable = false, length = 100)
    var name: String,
    @field:Schema(description = "Description of the project", example = "This is a sample project description.")
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,
    @field:Schema(description = "Status of the project")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_status_id", nullable = false)
    var status: ProjectStatusEntity,
    @field:Schema(description = "User who created the project")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var owner: UserEntity,
//    @field:Schema(description = "List of members in the project")
//    @OneToMany(
//        mappedBy = "project",
//        fetch = FetchType.LAZY,
//        cascade = [CascadeType.ALL],
//        orphanRemoval = true,
//    )
//    var members: MutableSet<ProjectMemberEntity> = mutableSetOf(),
    @field:Schema(description = "Creation timestamp of the project")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @field:Schema(description = "Last updated timestamp of the project")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
//    @field:Schema(description = "Deletion timestamp of the project, if deleted")
//    @Column(name = "deleted_at")
//    var deletedAt: Instant? = null,
) {
    val safeId: Long
        get() =
            id
                ?: throw IllegalStateException("ID not initialized for $this")
}
