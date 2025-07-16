package com.tianqueal.flowjet.backend.annotations.constrains.v1.task

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
    description = "Task comment content",
    example = "This is a comment on the task.",
    maxLength = 1000,
    requiredMode = RequiredMode.REQUIRED,
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_TASK_COMMENT_NOT_BLANK)
@Size(max = 1000, message = ValidationMessageKeys.VALIDATION_TASK_COMMENT_CONTENT_SIZE)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class TaskCommentContentConstraint(
    val message: String = "Invalid task comment content",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
