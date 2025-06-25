package com.tianqueal.flowjet.backend.domain.entities.keys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.Objects

@Embeddable
class ProjectMemberId(
    @Column(name = "project_id")
    var projectId: Long,
    @Column(name = "user_id")
    var userId: Long,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectMemberId

        return projectId == other.projectId && userId == other.userId
    }

    override fun hashCode(): Int = Objects.hash(projectId, userId)
}
