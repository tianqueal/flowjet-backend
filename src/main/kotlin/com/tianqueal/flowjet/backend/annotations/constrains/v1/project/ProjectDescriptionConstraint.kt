package com.tianqueal.flowjet.backend.annotations.constrains.v1.project

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
    description = "Project description",
    example = "This is a sample project description.",
    maxLength = 1000,
    nullable = true,
)
@Size(max = 1000, message = ValidationMessageKeys.VALIDATION_PROJECT_DESCRIPTION_SIZE)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class ProjectDescriptionConstraint(
    val message: String = "Invalid project description",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
