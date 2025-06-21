package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class UserAlreadyExistsException(
  fieldName: String,
  fieldValue: Any,
) : AppException(
  "User already exists with $fieldName: '$fieldValue'",
  MessageKeys.ERROR_USER_ALREADY_EXISTS
)
