package com.tianqueal.flowjet.backend.exceptions.business

open class AppException(
  message: String,
  val errorCode: String,
  vararg val args: Any?,
) : RuntimeException(message) {
  constructor(
    message: String,
    cause: Throwable,
    errorCode: String,
    vararg args: Any?,
  ) : this(message, errorCode, *args) {
    initCause(cause)
  }
}
