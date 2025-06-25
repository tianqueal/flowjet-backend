package com.tianqueal.flowjet.backend.annotations.constrains.v1.auth

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
  description = "Username or email address",
  example = "john.doe / john.doe@example.com",
  nullable = false,
  requiredMode = Schema.RequiredMode.REQUIRED
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_AUTH_USERNAME_OR_EMAIL_NOT_BLANK)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UsernameOrEmailConstraint(
  val message: String = "Invalid username or email address",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
