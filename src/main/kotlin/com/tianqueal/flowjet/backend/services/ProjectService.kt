package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectRequest
import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import com.tianqueal.flowjet.backend.utils.enums.ProjectAccessType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectService {
  fun findAll(
    accessType: ProjectAccessType,
    name: String?,
    description: String?,
    projectStatusId: Int?,
    pageable: Pageable,
  ): Page<ProjectResponse>

  fun findById(id: Long): ProjectResponse

  fun create(createProjectRequest: CreateProjectRequest): ProjectResponse
  fun create(userEntity: UserEntity, createProjectRequest: CreateProjectRequest): ProjectResponse
  fun create(userId: Long, createProjectRequest: CreateProjectRequest): ProjectResponse
  fun update(id: Long, updateProjectRequest: UpdateProjectRequest): ProjectResponse
  fun delete(id: Long)
}
