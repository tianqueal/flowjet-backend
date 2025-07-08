package com.tianqueal.flowjet.backend.repositories.projections

interface ProjectMemberCountProjection {
    fun getProjectId(): Long

    fun getMemberCount(): Int
}
