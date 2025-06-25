package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class UserAlreadyExistsException(
  fieldName: String,
  fieldValue: Any,
) : AppException(
  message = "User already exists with $fieldName: '$fieldValue'",
  errorCode = MessageKeys.ERROR_USER_ALREADY_EXISTS
)
