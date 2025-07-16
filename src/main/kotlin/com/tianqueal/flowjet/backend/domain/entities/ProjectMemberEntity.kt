package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
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

@Schema(description = "Entity representing the membership of a user in a project, including their role.")
@Entity
@Table(name = "project_members")
class ProjectMemberEntity(
    @field:Schema(description = "Composite key for the project member entity")
    @EmbeddedId
    var id: ProjectMemberId,
    @field:Schema(description = "Project to which the member belongs")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    var project: ProjectEntity,
    @field:Schema(description = "User who is a member of the project")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @field:Schema(description = "Role of the member in the project")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_role_id", nullable = false)
    var memberRole: MemberRoleEntity,
    @field:Schema(description = "Timestamp when the project member was added to the project")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
