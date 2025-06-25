package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse
import com.tianqueal.flowjet.backend.mappers.v1.ProjectStatusMapper
import com.tianqueal.flowjet.backend.repositories.ProjectStatusRepository
import com.tianqueal.flowjet.backend.services.ProjectStatusService

class ProjectStatusImpl(
    private val projectStatusRepository: ProjectStatusRepository,
    private val projectStatusMapper: ProjectStatusMapper,
) : ProjectStatusService {
    override fun finAll(): List<ProjectStatusResponse> =
        projectStatusRepository
            .findAll()
            .map(projectStatusMapper::toDto)
}
