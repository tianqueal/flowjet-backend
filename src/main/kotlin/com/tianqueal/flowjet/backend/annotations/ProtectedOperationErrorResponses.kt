package com.tianqueal.flowjet.backend.annotations

import com.tianqueal.flowjet.backend.domain.dto.v1.error.ErrorResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@ApiResponses(
  value = [
    ApiResponse(
      responseCode = "400",
      description = "Invalid input data",
      content = [
        Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )
      ]
    ),
    ApiResponse(
      responseCode = "401",
      description = "Unauthorized - Authentication required or token is invalid",
      content = [
        Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )
      ]
    ),
    // @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient
    // permissions to access this resource", content = @Content(mediaType =
    // MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
    // ErrorResponseDto.class))),
    ApiResponse(
      responseCode = "404",
      description = "Resource not found",
      content = [
        Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )
      ]
    ),
    ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = [
        Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )
      ]
    )
  ]
)
annotation class ProtectedOperationErrorResponses
