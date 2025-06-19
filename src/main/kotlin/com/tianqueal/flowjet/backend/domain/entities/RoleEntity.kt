package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.utils.enums.RoleName
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Schema(description = "Role entity representing a user role in the system")
@Entity
@Table(name = "roles")
class RoleEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Int? = null,

  @Column(name = "code", nullable = false, unique = true, length = 32)
  @Enumerated(EnumType.STRING)
  var code: RoleName,

  @Column(name = "name", nullable = false, length = 64)
  var name: String,
)
