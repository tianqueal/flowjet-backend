package com.tianqueal.flowjet.backend.services

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse

interface ProjectStatusService {
    fun finAll(): List<ProjectStatusResponse>
}
