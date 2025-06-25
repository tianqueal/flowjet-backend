package com.tianqueal.flowjet.backend.mappers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse
import com.tianqueal.flowjet.backend.domain.entities.ProjectStatusEntity
import org.springframework.stereotype.Component

@Component
class ProjectStatusMapper {
  fun toDto(entity: ProjectStatusEntity): ProjectStatusResponse = ProjectStatusResponse(
    id = entity.id ?: -1,
    code = entity.code,
    name = entity.name,
  )
}
