package com.tianqueal.flowjet.backend.annotations.constrains.v1.project

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
  description = "Project name",
  example = "My Project",
  maxLength = 100,
  requiredMode = RequiredMode.REQUIRED
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_PROJECT_NAME_NOT_BLANK)
@Size(max = 100, message = ValidationMessageKeys.VALIDATION_PROJECT_NAME_SIZE)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class ProjectNameConstraint(
  val message: String = "Invalid project name",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
