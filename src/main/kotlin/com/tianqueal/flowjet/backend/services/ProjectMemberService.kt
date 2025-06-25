package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectMemberService {
  fun findAll(
    projectId: Long,
    memberRoleId: Int?,
    username: String?,
    pageable: Pageable,
  ): Page<ProjectMemberResponse>

  fun isMember(projectId: Long, userId: Long): Boolean

  // fun addMemberToProject(projectId: Long, userId: Long, roleId: Long): ProjectMemberResponse
  // fun removeMemberFromProject(projectId: Long, userId: Long)
  // fun changeMemberRole(projectId: Long, userId: Long, newRoleId: Long): ProjectMemberResponse
}
