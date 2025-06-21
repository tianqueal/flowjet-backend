package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.RoleEntity
import com.tianqueal.flowjet.backend.utils.enums.RoleName
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : CrudRepository<RoleEntity, Int> {
  fun findAllByCodeIn(codes: Set<RoleName>): Set<RoleEntity>
}
