package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import com.tianqueal.flowjet.backend.repositories.projections.ProjectMemberCountProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProjectMemberRepository :
    JpaRepository<ProjectMemberEntity, ProjectMemberId>,
    JpaSpecificationExecutor<ProjectMemberEntity> {
    @EntityGraph(attributePaths = ["user", "memberRole"])
    override fun findAll(
        spec: Specification<ProjectMemberEntity>?,
        page: Pageable,
    ): Page<ProjectMemberEntity>

    @Query(
        """
    SELECT COUNT(pm) > 0
    FROM ProjectMemberEntity pm
    WHERE pm.id.projectId = :projectId AND pm.id.userId = :userId
  """,
    )
    fun isMember(
        projectId: Long,
        userId: Long,
    ): Boolean

    @EntityGraph(attributePaths = ["user", "memberRole"])
    fun findWithUserAndMemberRoleByIdProjectId(
        projectId: Long,
        sort: Sort,
    ): List<ProjectMemberEntity>

    @EntityGraph(attributePaths = ["memberRole"])
    fun findWithMemberRoleById(id: ProjectMemberId): ProjectMemberEntity?

    @EntityGraph(attributePaths = ["project"])
    fun findWithProjectById(id: ProjectMemberId): ProjectMemberEntity?

//    @EntityGraph(attributePaths = ["user", "memberRole"])
//    fun findWithUserAndMemberRoleById(id: ProjectMemberId): ProjectMemberEntity?

    @EntityGraph(attributePaths = ["project", "user", "memberRole"])
    fun findWithProjectUserAndMemberRoleById(id: ProjectMemberId): ProjectMemberEntity?

    @Query(
        """
        SELECT pm.project.id as projectId, COUNT(pm.id) as memberCount
        FROM ProjectMemberEntity pm
        WHERE pm.project.id IN :projectIds
        GROUP BY pm.project.id
    """,
    )
    fun countMembersByProjectIdIn(projectIds: List<Long>): List<ProjectMemberCountProjection>
}
