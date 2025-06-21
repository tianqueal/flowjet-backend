package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException : AppException {
  constructor(usernameOrEmail: String) : super(
    "User not found with username: '$usernameOrEmail'",
    MessageKeys.ERROR_USER_NOT_FOUND,
    usernameOrEmail
  )

  constructor(id: Long) : super(
    "User not found with ID: '$id'",
    MessageKeys.ERROR_USER_NOT_FOUND,
    id
  )
}
