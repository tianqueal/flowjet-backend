package com.tianqueal.flowjet.backend.services.impl

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse
import com.tianqueal.flowjet.backend.mappers.v1.ProjectStatusMapper
import com.tianqueal.flowjet.backend.repositories.ProjectStatusRepository
import com.tianqueal.flowjet.backend.services.ProjectStatusService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProjectStatusImpl(
    private val projectStatusRepository: ProjectStatusRepository,
    private val projectStatusMapper: ProjectStatusMapper,
) : ProjectStatusService {
    override fun findAll(): List<ProjectStatusResponse> =
        projectStatusRepository
            .findAll()
            .map(projectStatusMapper::toDto)
}
