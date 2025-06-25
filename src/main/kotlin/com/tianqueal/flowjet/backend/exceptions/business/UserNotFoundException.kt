package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class UserNotFoundException : AppException {
  constructor(usernameOrEmail: String) : super(
    message = "User not found with username or email: '$usernameOrEmail'",
    errorCode = MessageKeys.ERROR_USER_NOT_FOUND,
    args = arrayOf(usernameOrEmail),
  )

  constructor(id: Long) : super(
    message = "User not found with ID: '$id'",
    errorCode = MessageKeys.ERROR_USER_NOT_FOUND,
    args = arrayOf(id)
  )
}
