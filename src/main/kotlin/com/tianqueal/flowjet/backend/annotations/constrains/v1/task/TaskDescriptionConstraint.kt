package com.tianqueal.flowjet.backend.annotations.constrains.v1.task

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
    description = "Task description",
    example = "This is a sample task description.",
    maxLength = 1000,
    nullable = true,
)
@Size(max = 1000, message = ValidationMessageKeys.VALIDATION_TASK_DESCRIPTION_SIZE)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class TaskDescriptionConstraint(
    val message: String = "Invalid task description",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
