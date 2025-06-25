package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectMemberResponse
import com.tianqueal.flowjet.backend.mappers.v1.ProjectMemberMapper
import com.tianqueal.flowjet.backend.repositories.ProjectMemberRepository
import com.tianqueal.flowjet.backend.services.ProjectMemberService
import com.tianqueal.flowjet.backend.specifications.ProjectMemberSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectMemberServiceImpl(
  private val projectMemberRepository: ProjectMemberRepository,
  private val projectMemberMapper: ProjectMemberMapper,
) : ProjectMemberService {
  @Transactional(readOnly = true)
  override fun findAll(
    projectId: Long,
    memberRoleId: Int?,
    username: String?,
    pageable: Pageable,
  ): Page<ProjectMemberResponse> = projectMemberRepository.findAll(
    ProjectMemberSpecification.filterBy(projectId, memberRoleId, username),
    pageable
  ).map(projectMemberMapper::toDto)

  @Transactional(readOnly = true)
  override fun isMember(projectId: Long, userId: Long): Boolean =
    projectMemberRepository.isMember(projectId, userId)
}
