package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.TaskCommentEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskCommentEntity_
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity_
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object TaskCommentSpecification {
    fun filterBy(
        taskId: Long,
        onlyRoots: Boolean = true,
    ): Specification<TaskCommentEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            predicates += (cb.equal(root.get(TaskCommentEntity_.task).get(TaskEntity_.id), taskId))

            if (onlyRoots) {
                predicates += (cb.isNull(root.get(TaskCommentEntity_.parent)))
            }

            cb.and(*predicates.toTypedArray())
        }
}
