package com.tianqueal.flowjet.backend.specifications

import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object UserSpecification {
  fun filterBy(
    username: String?,
    email: String?,
    name: String?
  ): Specification<UserEntity> {
    return Specification { root, _, cb ->
      val predicates = mutableListOf<Predicate>()

      if (!username.isNullOrBlank()) {
        predicates += cb.like(
          cb.lower(root.get("username")), "%${username.lowercase()}%"
        )
      }

      if (!email.isNullOrBlank()) {
        predicates += cb.like(cb.lower(root.get("email")), "%${email.lowercase()}%")
      }

      if (!name.isNullOrBlank()) {
        predicates += cb.like(cb.lower(root.get("name")), "%${name.lowercase()}%")
      }

      cb.and(*predicates.toTypedArray())
    }
  }
}
