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
    description = "Password for the user",
    example = "SecureP@ssw0rd",
    minLength = 8,
    maxLength = 64,
    pattern = ValidationPatterns.PASSWORD,
    requiredMode = RequiredMode.REQUIRED,
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_NOT_BLANK)
@Size(min = 8, max = 64, message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_SIZE)
@Pattern(
    regexp = ValidationPatterns.PASSWORD,
    message = ValidationMessageKeys.VALIDATION_USER_PASSWORD_PATTERN,
)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UserPasswordConstraint(
    val message: String = "Invalid password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
