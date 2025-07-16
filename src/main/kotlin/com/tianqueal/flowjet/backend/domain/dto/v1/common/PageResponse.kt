package com.tianqueal.flowjet.backend.domain.dto.v1.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Paginated response containing a list of items and pagination metadata")
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
)
