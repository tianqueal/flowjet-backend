package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity_
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity
import com.tianqueal.flowjet.backend.domain.entities.TaskEntity_
import com.tianqueal.flowjet.backend.domain.entities.TaskStatusEntity_
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object TaskSpecification {
    fun belongsToProject(projectId: Long): Specification<TaskEntity> =
        Specification { root, _, cb ->
            cb.equal(root.get(TaskEntity_.project).get(ProjectEntity_.id), projectId)
        }

    fun filterBy(
        name: String?,
        statusId: Int?,
    ): Specification<TaskEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (!name.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get(TaskEntity_.name)), "%${name.lowercase()}%")
            }

            if (statusId != null) {
                predicates += cb.equal(root.get(TaskEntity_.status).get(TaskStatusEntity_.id), statusId)
            }

            cb.and(*predicates.toTypedArray())
        }
}
