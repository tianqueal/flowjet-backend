package com.tianqueal.flowjet.backend.annotations.constrains.v1.user

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@Schema(
  description = "User email address",
  example = "john.doe@example.com",
  maxLength = 255,
  requiredMode = RequiredMode.REQUIRED
)
@NotBlank(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_NOT_BLANK)
@Email(message = ValidationMessageKeys.VALIDATION_USER_EMAIL_INVALID)
@Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_EMAIL_SIZE)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UserEmailConstraint(
  val message: String = "Invalid email address",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
