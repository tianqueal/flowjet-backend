package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository :
    JpaRepository<ProjectEntity, Long>,
    JpaSpecificationExecutor<ProjectEntity> {
    @EntityGraph(attributePaths = ["status", "owner"])
    override fun findAll(
        spec: Specification<ProjectEntity>?,
        pageable: Pageable,
    ): Page<ProjectEntity>

//    @Query(
//        """
//       SELECT COUNT(p) > 0
//        FROM ProjectEntity p
//        WHERE p.id = :id AND p.owner.id = :userId
//    """,
//    )
//    fun isOwner(
//        id: Long,
//        userId: Long,
//    ): Boolean

    @Query(
        """
        SELECT COUNT(p) > 0
        FROM ProjectEntity p
        WHERE p.id = :projectId AND (
            p.owner.id = :userId OR
            EXISTS (SELECT 1 FROM ProjectMemberEntity pm WHERE pm.project.id = p.id AND pm.user.id = :userId)
        )
    """,
    )
    fun hasReadAccess(
        projectId: Long,
        userId: Long,
    ): Boolean

    @EntityGraph(attributePaths = ["owner"])
    fun findWithOwnerById(id: Long): ProjectEntity?

    @EntityGraph(attributePaths = ["status", "owner"])
    fun findWithStatusAndOwnerById(id: Long): ProjectEntity?
}
