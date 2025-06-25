package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity_
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object UserSpecification {
    fun filterBy(
        username: String?,
        email: String?,
        name: String?,
    ): Specification<UserEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (!username.isNullOrBlank()) {
                predicates +=
                    cb.like(
                        cb.lower(root.get(UserEntity_.username)),
                        "%${username.lowercase()}%",
                    )
            }

            if (!email.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get(UserEntity_.email)), "%${email.lowercase()}%")
            }

            if (!name.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get(UserEntity_.name)), "%${name.lowercase()}%")
            }

            cb.and(*predicates.toTypedArray())
        }
}
