package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity_
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity_
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity_
import com.tianqueal.flowjet.backend.domain.entities.UserEntity_
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object ProjectMemberSpecification {
    fun filterBy(
        projectId: Long,
        memberRoleId: Int?,
        username: String?,
    ): Specification<ProjectMemberEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            predicates += cb.equal(root.get(ProjectMemberEntity_.project).get(ProjectEntity_.id), projectId)

            if (memberRoleId != null) {
                predicates += cb.equal(root.get(ProjectMemberEntity_.memberRole).get(MemberRoleEntity_.id), memberRoleId)
            }

            if (!username.isNullOrBlank()) {
                val userJoin = root.join(ProjectMemberEntity_.user, JoinType.INNER)
                predicates += cb.like(cb.lower(userJoin.get(UserEntity_.username)), "%${username.lowercase()}%")
            }

            cb.and(*predicates.toTypedArray())
        }
}
