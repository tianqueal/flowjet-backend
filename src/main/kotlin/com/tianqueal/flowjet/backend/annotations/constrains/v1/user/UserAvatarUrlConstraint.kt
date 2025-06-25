package com.tianqueal.flowjet.backend.annotations.constrains.v1.user

import com.tianqueal.flowjet.backend.utils.constants.ValidationMessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Schema(
  description = "URL of the user's avatar image",
  example = "https://example.com/avatar.jpg",
  maxLength = 255,
  nullable = true
)
@Size(max = 255, message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_SIZE)
@URL(message = ValidationMessageKeys.VALIDATION_USER_AVATAR_URL_INVALID)
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
annotation class UserAvatarUrlConstraint(
  val message: String = "Invalid avatar URL",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
