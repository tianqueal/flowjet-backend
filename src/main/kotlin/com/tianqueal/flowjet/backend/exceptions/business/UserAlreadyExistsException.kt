package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.domain.dto.v1.error.FieldErrorsResponse
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class UserAlreadyExistsException private constructor(
    errors: Map<String, String>,
) : AppException(
        message = "User already exists with conflicting fields: ${errors.entries.joinToString()}",
        errorCode = MessageKeys.ERROR_USER_ALREADY_EXISTS,
        args =
            arrayOf(
                FieldErrorsResponse(
                    fieldErrors = errors.mapValues { listOf(it.value) },
                ),
            ),
    ) {
    companion object {
        fun of(
            fieldName: String,
            fieldValue: String,
        ): UserAlreadyExistsException = UserAlreadyExistsException(mapOf(fieldName to fieldValue))

        fun of(errors: Map<String, String>): UserAlreadyExistsException = UserAlreadyExistsException(errors)
    }
}
