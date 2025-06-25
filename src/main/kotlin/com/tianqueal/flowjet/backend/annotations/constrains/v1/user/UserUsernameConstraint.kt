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
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@Schema(
  description = "Username for the user",
  example = "john.doe",
  minLength = 3,
  maxLength = 50,
  pattern = ValidationPatterns.USERNAME,
  requiredMode = RequiredMode.REQUIRED,
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_USER_USERNAME_NOT_BLANK)
@Size(min = 3, max = 50, message = ValidationMessageKeys.VALIDATION_USER_USERNAME_SIZE)
@Pattern(
  regexp = ValidationPatterns.USERNAME,
  message = ValidationMessageKeys.VALIDATION_USER_USERNAME_PATTERN
)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UserUsernameConstraint(
  val message: String = "Invalid username",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
