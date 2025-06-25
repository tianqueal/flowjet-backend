package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProjectMemberRepository :
    JpaRepository<ProjectMemberEntity, ProjectMemberId>,
    JpaSpecificationExecutor<ProjectMemberEntity> {
    @Query(
        """
    SELECT CASE WHEN COUNT(pm) > 0 THEN TRUE ELSE FALSE END
    FROM ProjectMemberEntity pm
    WHERE pm.id.projectId = :projectId AND pm.id.userId = :userId
  """,
    )
    fun isMember(
        projectId: Long,
        userId: Long,
    ): Boolean
}
