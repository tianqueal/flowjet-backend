package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.MemberRoleResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.entities.MemberRoleEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectEntity
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.domain.entities.keys.ProjectMemberId
import org.springframework.stereotype.Component

@Component
class ProjectMemberMapper(
    private val userProfileMapper: UserProfileMapper,
) {
    fun toDto(entity: ProjectMemberEntity): ProjectMemberResponse =
        ProjectMemberResponse(
            member = userProfileMapper.toDto(entity.user),
            memberRole =
                MemberRoleResponse(
                    id = entity.memberRole.safeId,
                    code = entity.memberRole.code,
                    name = entity.memberRole.name,
                    displayOrder = entity.memberRole.displayOrder,
                ),
            memberSince = entity.createdAt,
        )

    fun toEntity(
        projectEntity: ProjectEntity,
        userEntity: UserEntity,
        memberRoleEntity: MemberRoleEntity,
    ): ProjectMemberEntity =
        ProjectMemberEntity(
            id = ProjectMemberId(projectId = projectEntity.safeId, userId = userEntity.safeId),
            project = projectEntity,
            user = userEntity,
            memberRole = memberRoleEntity,
        )

    fun updateEntityFromDto(
        entity: ProjectMemberEntity,
        memberRoleEntity: MemberRoleEntity,
    ) {
        entity.memberRole = memberRoleEntity
    }
}
