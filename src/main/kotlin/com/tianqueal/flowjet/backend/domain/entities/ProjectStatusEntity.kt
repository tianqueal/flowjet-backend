package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.utils.enums.ProjectStatusEnum
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Schema(description = "Project status entity representing the status of a project")
@Entity
@Table(name = "project_statuses")
class ProjectStatusEntity(
    @field:Schema(description = "Unique identifier of the project status", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Schema(description = "Code of the project status", example = "IN_PROGRESS")
    @Column(name = "code", unique = true, nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    var code: ProjectStatusEnum,
    @field:Schema(description = "Name of the project status", example = "In Progress")
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
) {
    val safeId: Int
        get() =
            id
                ?: throw IllegalStateException("ID not initialized for $this")
}
