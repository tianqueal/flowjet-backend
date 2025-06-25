package com.tianqueal.flowjet.backend.annotations.constrains.v1.user

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import com.tianqueal.flowjet.backend.utils.constants.ValidationPatterns
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
    description = "Full name of the user",
    example = "John Doe",
    minLength = 3,
    maxLength = 100,
    pattern = ValidationPatterns.NAME,
    requiredMode = RequiredMode.REQUIRED,
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_USER_NAME_NOT_BLANK)
@Size(min = 3, max = 100, message = ValidationMessageKeys.VALIDATION_USER_NAME_SIZE)
@Pattern(
    regexp = ValidationPatterns.NAME,
    message = ValidationMessageKeys.VALIDATION_USER_NAME_PATTERN,
)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UserNameConstraint(
    val message: String = "Invalid user name",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
