package com.tianqueal.flowjet.backend.domain.dto.v1.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response DTO for field validation errors")
data class FieldErrorsResponse(
    @field:Schema(description = "Map of field names to their validation error messages")
    val fieldErrors: Map<String, List<String>>,
)
