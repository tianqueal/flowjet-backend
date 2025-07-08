package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.TaskAssigneeEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskAssigneeEntity_
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity_
import com.tianqueal.flowjet.backend.domain.entities.UserEntity_
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object TaskAssigneeSpecification {
    fun filterBy(
        taskId: Long,
        username: String?,
        name: String?,
    ): Specification<TaskAssigneeEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            predicates += cb.equal(root.get(TaskAssigneeEntity_.task).get(TaskEntity_.id), taskId)

            if (!username.isNullOrBlank() || !name.isNullOrBlank()) {
                val userJoin = root.join(TaskAssigneeEntity_.user, JoinType.INNER)

                if (!username.isNullOrBlank()) {
                    predicates += cb.like(cb.lower(userJoin.get(UserEntity_.username)), "%${username.lowercase()}%")
                }

                if (!name.isNullOrBlank()) {
                    predicates +=
                        cb.like(
                            cb.lower(userJoin.get(UserEntity_.name)),
                            "%${name.lowercase()}%",
                        )
                }
            }

            cb.and(*predicates.toTypedArray())
        }
}
