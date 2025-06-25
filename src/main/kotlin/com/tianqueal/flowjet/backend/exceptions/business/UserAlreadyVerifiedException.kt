package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class UserAlreadyVerifiedException(
  fieldName: String,
  fieldValue: Any,
) : AppException(
  message = "User already verified with $fieldName: '$fieldValue'",
  errorCode = MessageKeys.ERROR_USER_ALREADY_VERIFIED
)
