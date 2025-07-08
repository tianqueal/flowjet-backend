package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity_
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity_
import com.tianqueal.flowjet.backend.domain.entities.ProjectStatusEntity_
import com.tianqueal.flowjet.backend.domain.entities.UserEntity_
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object ProjectSpecification {
    fun isOwner(userId: Long): Specification<ProjectEntity> =
        Specification { root, _, cb ->
            cb.equal(root.get(ProjectEntity_.owner).get(UserEntity_.id), userId)
        }

    fun isMember(userId: Long): Specification<ProjectEntity> =
        Specification { root, query, cb ->
            val subquery = query?.subquery(ProjectMemberEntity::class.java)
            val subqueryRoot = subquery?.from(ProjectMemberEntity::class.java)
            subquery?.select(subqueryRoot)
            val subqueryPredicates = mutableListOf<Predicate>()
            subqueryPredicates.add(cb.equal(subqueryRoot?.get(ProjectMemberEntity_.project), root))
            subqueryPredicates.add(cb.equal(subqueryRoot?.get(ProjectMemberEntity_.user)?.get(UserEntity_.id), userId))
            subquery?.where(*subqueryPredicates.toTypedArray())
            cb.exists(subquery)
        }

    fun isOwnerOrMember(userId: Long): Specification<ProjectEntity> = isOwner(userId).or(isMember(userId))

    fun filterBy(
        name: String?,
        description: String?,
        projectStatusId: Int?,
    ): Specification<ProjectEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (!name.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get(ProjectEntity_.name)), "%${name.lowercase()}%")
            }

            if (!description.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get(ProjectEntity_.description)), "%${description.lowercase()}%")
            }

            if (projectStatusId != null) {
                predicates += cb.equal(root.get(ProjectEntity_.status).get(ProjectStatusEntity_.id), projectStatusId)
            }

            cb.and(*predicates.toTypedArray())
        }
}
