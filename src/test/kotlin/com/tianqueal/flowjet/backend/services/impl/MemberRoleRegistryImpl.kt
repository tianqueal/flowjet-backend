package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.services.MemberRoleRegistry
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("test")
@Transactional(readOnly = true)
class MemberRoleRegistryImpl(
    private val memberRoleRepository: MemberRoleRepository,
) : MemberRoleRegistry {
    private val roleCache: MutableMap<MemberRoleEnum, MemberRoleEntity> = mutableMapOf()

    @PostConstruct
    fun initialize() {
        val memberRoles = memberRoleRepository.findAll()
        memberRoles.forEach { memberRoleEntity ->
            try {
                val roleEnum = memberRoleEntity.code
                roleCache[roleEnum] = memberRoleEntity
            } catch (_: IllegalArgumentException) {
                println("WARN: Member role with code '${memberRoleEntity.code}' found in DB but not in MemberRoleEnum.")
            }
        }
    }

    override fun getRole(code: MemberRoleEnum): MemberRoleEntity =
        roleCache[code]
            ?: throw IllegalArgumentException("Member role with code '$code' not found in registry.")

    override fun getRoleId(code: MemberRoleEnum): Int = getRole(code).safeId
}
