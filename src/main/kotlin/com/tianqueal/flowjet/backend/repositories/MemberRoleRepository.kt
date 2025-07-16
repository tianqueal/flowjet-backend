package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRoleRepository : JpaRepository<MemberRoleEntity, Int> {
    fun findByCode(code: MemberRoleEnum): MemberRoleEntity?
}
