package com.tianqueal.flowjet.backend.exceptions

import com.tianqueal.flowjet.backend.domain.dto.v1.error.ErrorResponse
import com.tianqueal.flowjet.backend.exceptions.business.AppException
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.resource.NoResourceFoundException
import kotlin.reflect.full.findAnnotation

/**
 * Global exception handler for the application.
 *
 * This class centralizes the handling of common exceptions thrown by controllers and services,
 * providing consistent error responses in JSON format, with support for i18n and detailed logging.
 *
 * @author Christian A.
 */
@ControllerAdvice
class GlobalExceptionHandler(
  private val messageSource: MessageSource
) {
  /**
   * Handles validation errors for request bodies (e.g., @Valid failures).
   * Returns a 400 Bad Request with details about which fields failed validation.
   */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationExceptions(
    ex: MethodArgumentNotValidException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val fieldErrors = ex.bindingResult.fieldErrors
      .groupBy { it.field }
      .mapValues { entry ->
        entry.value.mapNotNull { it.defaultMessage }
      }
    val code = MessageKeys.ERROR_AUTH_VALIDATION_REGISTER
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(code, null, locale)
    val errorResponse = ErrorResponse(
      code = code,
      error = MethodArgumentNotValidException::class.simpleName,
      message = message,
      path = request.requestURI,
      details = fieldErrors
    )
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  /**
   * Handles custom application exceptions (AppException).
   * Returns the status defined by @ResponseStatus or defaults to 500 Internal Server Error.
   */
  @ExceptionHandler(AppException::class)
  fun handleAppException(
    ex: AppException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = ex.errorCode
    val message = messageSource.getMessage(code, ex.args, ex.message, locale)
    val responseStatus = ex::class.findAnnotation<ResponseStatus>()
    val status = responseStatus?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI,
      details = ex.args.takeIf { it.isNotEmpty() }
    )

    log.warn(
      "{} ({}): {} - Args: {}",
      ex::class.simpleName,
      status,
      message,
      ex.args,
      ex
    )

    return ResponseEntity(errorResponse, status)
  }

  /**
   * Handles authentication-related exceptions (login, credentials, account status).
   * Returns 401 Unauthorized with an appropriate message.
   */
  @ExceptionHandler(AuthenticationException::class)
  fun handleAuthenticationException(
    ex: AuthenticationException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val (code, messageArgs) = when (ex) {
      is BadCredentialsException -> MessageKeys.ERROR_AUTH_INVALID_CREDENTIALS to null
      is InternalAuthenticationServiceException ->
        if (ex.cause is AppException) {
          val appCause = ex.cause as AppException
          appCause.errorCode to appCause.args
        } else MessageKeys.ERROR_AUTH_GENERIC to null

      is AccountExpiredException -> MessageKeys.ERROR_AUTH_ACCOUNT_EXPIRED to null
      is LockedException -> MessageKeys.ERROR_AUTH_USER_LOCKED to null
      is CredentialsExpiredException -> MessageKeys.ERROR_AUTH_CREDENTIALS_EXPIRED to null
      is DisabledException -> MessageKeys.ERROR_AUTH_USER_DISABLED to null
      is InsufficientAuthenticationException -> MessageKeys.ERROR_AUTH_INSUFFICIENT_AUTHENTICATION to null
      else -> MessageKeys.ERROR_AUTH_GENERIC to null
    }
    val message = messageSource.getMessage(code, messageArgs, "Authentication failed", locale)
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI,
      details = ex.cause?.let { cause ->
        mapOf(
          "cause" to cause::class.simpleName,
          "causeMessage" to cause.message
        )
      }
    )

    if (ex.cause != null) {
      log.warn(
        "{}: {} - Type: {} - Cause: {} - Cause Type: {}",
        AuthenticationException::class.simpleName,
        message,
        ex::class.simpleName,
        ex.cause?.message,
        ex.cause?.let { it::class.simpleName },
        ex
      )
    } else {
      log.warn(
        "{}: {} - Type: {}",
        AuthenticationException::class.simpleName,
        message,
        ex::class.simpleName,
        ex
      )
    }

    return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
  }

  /**
   * Handles JWT-related exceptions (invalid, expired, etc.).
   * Returns 401 Unauthorized with a JWT error message.
   */
  @ExceptionHandler(JwtException::class)
  fun handleJwtException(
    ex: JwtException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = MessageKeys.ERROR_AUTH_JWT
    val message = messageSource.getMessage(code, null, "JWT processing error", locale)
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI
    )

    log.warn(
      "{}: {}",
      JwtException::class.simpleName,
      message,
    )

    return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
  }

  /**
   * Handles authorisation failures (e.g., insufficient permissions).
   * Returns 403 Forbidden with a suitable message.
   */
  @ExceptionHandler(AuthorizationDeniedException::class)
  fun handleAuthorizationDeniedException(
    ex: AuthorizationDeniedException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = MessageKeys.ERROR_AUTH_FORBIDDEN
    val message = messageSource.getMessage(code, null, "Access denied", locale)
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI
    )

    log.warn(
      "{}: {}",
      AuthorizationDeniedException::class.simpleName,
      message,
      ex
    )

    return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
  }

  /**
   * Handles requests for static or dynamic resources that do not exist.
   * Returns 404 Not Found with resource details.
   */
  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(
    ex: NoResourceFoundException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = MessageKeys.ERROR_RESOURCE_NOT_FOUND
    val message = messageSource.getMessage(code, null, "Resource not found", locale)
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI
    )

    log.warn(
      "{}: {} - Resource: {}",
      NoResourceFoundException::class.simpleName,
      message,
      ex.resourcePath,
      ex
    )

    return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
  }

  /**
   * Handles HTTP method not allowed errors (e.g., POST on a GET-only endpoint).
   * Returns 405 Method Not Allowed with details about supported methods.
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleMethodNotSupported(
    ex: HttpRequestMethodNotSupportedException,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = MessageKeys.ERROR_METHOD_NOT_ALLOWED
    val message = messageSource.getMessage(
      code,
      arrayOf(ex.method),
      "HTTP method not allowed",
      locale
    )
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI,
      details = mapOf(
        "method" to ex.method,
        "supported" to ex.supportedMethods
      )
    )

    log.warn(
      "{}: {} - Supported: {}",
      HttpRequestMethodNotSupportedException::class.simpleName,
      ex.method,
      ex.supportedMethods
    )

    return ResponseEntity(
      errorResponse,
      HttpStatus.METHOD_NOT_ALLOWED
    )
  }

  /**
   * Handles all other uncaught exceptions.
   * Returns 500 Internal Server Error with a generic error message.
   */
  @ExceptionHandler(Exception::class)
  fun handleGenericException(
    ex: Exception,
    request: HttpServletRequest
  ): ResponseEntity<ErrorResponse> {
    val locale = LocaleContextHolder.getLocale()
    val code = MessageKeys.ERROR_APP_GENERIC
    val message = messageSource.getMessage(
      code,
      null,
      "An unexpected error occurred. Please try again later.",
      locale
    )
    val errorResponse = ErrorResponse(
      code = code,
      error = ex::class.simpleName,
      message = message ?: "Unexpected error",
      path = request.requestURI
    )

    log.error(
      "Unhandled generic exception: {}",
      message,
      ex
    )

    return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }

  companion object {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
  }
}
