package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.utils.enums.RoleEnum
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
    @field:Schema(description = "Unique identifier of the role")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Schema(description = "Unique code of the role")
    @Column(name = "code", nullable = false, unique = true, length = 32)
    @Enumerated(EnumType.STRING)
    var code: RoleEnum,
    @field:Schema(description = "Name of the role")
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
)
