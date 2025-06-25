package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.MemberRoleResponse
import com.tianqueal.flowjet.backend.mappers.v1.MemberRoleMapper
import com.tianqueal.flowjet.backend.repositories.MemberRoleRepository
import com.tianqueal.flowjet.backend.services.MemberRoleService

class MemberRoleServiceImpl(
    private val memberRoleRepository: MemberRoleRepository,
    private val memberRoleMapper: MemberRoleMapper,
) : MemberRoleService {
    override fun findAll(): List<MemberRoleResponse> =
        memberRoleRepository
            .findAll()
            .map(memberRoleMapper::toDto)
}
