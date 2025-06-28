package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.MemberRoleResponse
import com.tianqueal.flowjet.backend.mappers.v1.MemberRoleMapper
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.services.MemberRoleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberRoleServiceImpl(
    private val memberRoleRepository: MemberRoleRepository,
    private val memberRoleMapper: MemberRoleMapper,
) : MemberRoleService {
    override fun findAll(): List<MemberRoleResponse> =
        memberRoleRepository
            .findAll()
            .map(memberRoleMapper::toDto)
}
