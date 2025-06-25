package com.tianqueal.flowjet.backend.domain.entities

import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Schema(description = "Entity representing a member's role in a project")
@Entity
@Table(name = "member_roles")
class MemberRoleEntity(
    @field:Schema(description = "Unique identifier of the member role")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Schema(description = "Unique code of the member role", example = "ADMIN")
    @Column(name = "code", nullable = false, unique = true, length = 32)
    @Enumerated(EnumType.STRING)
    var code: MemberRoleEnum,
    @field:Schema(description = "Name of the member role", example = "Admin")
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
)
