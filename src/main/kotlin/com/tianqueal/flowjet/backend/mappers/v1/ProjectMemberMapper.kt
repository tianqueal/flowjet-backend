package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.domain.entities.ProjectMemberEntity
import org.springframework.stereotype.Component

@Component
class ProjectMemberMapper(
  private val userProfileMapper: UserProfileMapper,
) {
  fun toDto(entity: ProjectMemberEntity): ProjectMemberResponse = ProjectMemberResponse(
    member = userProfileMapper.toDto(entity.user),
    memberRole = entity.memberRole.code,
    memberSince = entity.createdAt
  )
}
