package com.tianqueal.flowjet.backend.exceptions

import com.tianqueal.flowjet.backend.domain.dto.v1.error.ErrorResponse
import com.tianqueal.flowjet.backend.exceptions.business.AppException
import com.tianqueal.flowjet.backend.exceptions.business.CannotAddOwnerAsProjectMemberException
import com.tianqueal.flowjet.backend.exceptions.business.CannotAssignOwnerRoleException
import com.tianqueal.flowjet.backend.exceptions.business.CannotSelfManageProjectMembershipException
import com.tianqueal.flowjet.backend.exceptions.business.MemberRoleNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectMemberAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectMemberNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.ProjectNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskAssigneeAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.TaskAssigneeNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.TaskNotFoundException
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyExistsException
import com.tianqueal.flowjet.backend.exceptions.business.UserAlreadyVerifiedException
import com.tianqueal.flowjet.backend.exceptions.business.UserIsNotProjectMemberException
import com.tianqueal.flowjet.backend.exceptions.business.UserNotFoundException
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
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
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.resource.NoResourceFoundException

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
    private val messageSource: MessageSource,
) {
    /**
     * Handles validation errors for request bodies (e.g., @Valid failures).
     * Returns a 400 Bad Request with details about which fields failed validation.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(description = "Invalid input data or request format")
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors =
            ex.bindingResult.fieldErrors
                .groupBy { it.field }
                .mapValues { entry ->
                    entry.value.mapNotNull { it.defaultMessage }
                }
        val code = MessageKeys.ERROR_AUTH_VALIDATION_REGISTER
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(code, null, locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message,
                path = request.requestURI,
                details = fieldErrors,
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handles JSON parsing errors (e.g., invalid JSON format).
     * Returns a 400 Bad Request with a message indicating the JSON format is invalid.
     */
    @ApiResponse(description = "Invalid JSON format in request body")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_INVALID_JSON_FORMAT
        val message = messageSource.getMessage(code, null, "Invalid JSON format", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {} - Cause: {}",
            ex::class.simpleName,
            message,
            ex.cause?.message ?: "No cause",
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handles business logic exceptions that result in bad requests.
     * Returns 400 Bad Request with a detailed error response.
     */
    @ApiResponse(description = "Bad request due to business logic violations")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        CannotAddOwnerAsProjectMemberException::class,
        CannotSelfManageProjectMembershipException::class,
        CannotAssignOwnerRoleException::class,
    )
    fun handleBadRequestExceptions(
        ex: AppException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status: HttpStatus = HttpStatus.BAD_REQUEST
        val errorResponse = buildErrorResponse(ex, request)
        logWarning(ex, status, errorResponse)
        return ResponseEntity(errorResponse, status)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        IllegalArgumentException::class,
    )
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_INVALID_ARGUMENT
        val message = messageSource.getMessage(code, null, "Invalid argument", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {} - Cause: {}",
            ex::class.simpleName,
            message,
            ex.cause?.message ?: "No cause",
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handles JWT-related exceptions (invalid, expired, etc.).
     * Returns 401 Unauthorized with a JWT error message.
     */
    @ApiResponse(description = "JWT processing error")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtException::class)
    fun handleJwtException(
        ex: JwtException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_AUTH_JWT
        val message = messageSource.getMessage(code, null, "JWT processing error", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {}",
            ex::class.simpleName,
            message,
        )

        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Handles authentication-related exceptions (login, credentials, account status).
     * Returns 401 Unauthorized with an appropriate message.
     */
    @ApiResponse(description = "Unauthorized access or authentication failure")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val (code, messageArgs) =
            when (ex) {
                is BadCredentialsException -> MessageKeys.ERROR_AUTH_INVALID_CREDENTIALS to null
                is InternalAuthenticationServiceException ->
                    if (ex.cause is AppException) {
                        val appCause = ex.cause as AppException
                        appCause.errorCode to appCause.args
                    } else {
                        MessageKeys.ERROR_AUTH_GENERIC to null
                    }

                is AccountExpiredException -> MessageKeys.ERROR_AUTH_ACCOUNT_EXPIRED to null
                is LockedException -> MessageKeys.ERROR_AUTH_USER_LOCKED to null
                is CredentialsExpiredException -> MessageKeys.ERROR_AUTH_CREDENTIALS_EXPIRED to null
                is DisabledException -> MessageKeys.ERROR_AUTH_USER_DISABLED to null
                is InsufficientAuthenticationException -> MessageKeys.ERROR_AUTH_INSUFFICIENT_AUTHENTICATION to null
                else -> MessageKeys.ERROR_AUTH_GENERIC to null
            }
        val message = messageSource.getMessage(code, messageArgs, "Authentication failed", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
                details =
                    ex.cause?.let { cause ->
                        mapOf(
                            "cause" to cause::class.simpleName,
                            "causeMessage" to cause.message,
                        )
                    },
            )

        if (ex.cause != null) {
            log.warn(
                "{}: {} - Type: {} - Cause: {} - Cause Type: {}",
                ex::class.simpleName,
                message,
                ex::class.simpleName,
                ex.cause?.message,
                ex.cause?.let { it::class.simpleName },
                ex,
            )
        } else {
            log.warn(
                "{}: {} - Type: {}",
                ex::class.simpleName,
                message,
                ex::class.simpleName,
                ex,
            )
        }

        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Handles authorisation failures (e.g., insufficient permissions).
     * Returns 403 Forbidden with a suitable message.
     */
    @ApiResponse(description = "Access denied or insufficient permissions")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(
        ex: AuthorizationDeniedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_AUTH_FORBIDDEN
        val message = messageSource.getMessage(code, null, "Access denied", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {}",
            ex::class.simpleName,
            message,
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_RESOURCE_NOT_FOUND
        val message = messageSource.getMessage(code, null, "Resource not found", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {}",
            ex::class.simpleName,
            message,
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    /**
     * Handles entity not found exceptions (e.g., JPA entity not found).
     * Returns 404 Not Found with a detailed error response.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        ex: EntityNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_RESOURCE_NOT_FOUND
        val message = messageSource.getMessage(code, null, "Resource not found", locale)
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.warn(
            "{}: {}",
            ex::class.simpleName,
            message,
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    /**
     * Handles not found exceptions (e.g., project status, project, user).
     * Returns 404 Not Found with a detailed error response.
     */
    @ApiResponse(description = "Resource not found")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        ProjectNotFoundException::class,
        UserNotFoundException::class,
        MemberRoleNotFoundException::class,
        ProjectMemberNotFoundException::class,
        TaskNotFoundException::class,
        TaskAssigneeNotFoundException::class,
    )
    fun handleNotFoundExceptions(
        ex: AppException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        val errorResponse = buildErrorResponse(ex, request)
        logWarning(ex, status, errorResponse)
        return ResponseEntity(errorResponse, status)
    }

    /**
     * Handles HTTP method not allowed errors (e.g., POST on a GET-only endpoint).
     * Returns 405 Method Not Allowed with details about supported methods.
     */
    @ApiResponse(description = "HTTP method not allowed")
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_METHOD_NOT_ALLOWED
        val message =
            messageSource.getMessage(
                code,
                arrayOf(ex.method),
                "HTTP method not allowed",
                locale,
            )
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
                details =
                    mapOf(
                        "method" to ex.method,
                        "supported" to ex.supportedMethods,
                    ),
            )

        log.warn(
            "{}: {} - Supported: {}",
            ex::class.simpleName,
            ex.method,
            ex.supportedMethods,
        )

        return ResponseEntity(errorResponse, HttpStatus.METHOD_NOT_ALLOWED)
    }

    /**
     * Handles conflict exceptions (e.g., user already exists).
     * Returns 409 Conflict with a detailed error response.
     */
    @ApiResponse(description = "Conflict with existing resource")
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(
        UserAlreadyExistsException::class,
        UserAlreadyVerifiedException::class,
        ProjectMemberAlreadyExistsException::class,
        TaskAssigneeAlreadyExistsException::class,
    )
    fun handleConflictExceptions(
        ex: AppException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status: HttpStatus = HttpStatus.CONFLICT
        val errorResponse = buildErrorResponse(ex, request)
        logWarning(ex, status, errorResponse)
        return ResponseEntity(errorResponse, status)
    }

    /**
     * Handles unprocessable content exceptions
     * Returns 422 Unprocessable Entity with a detailed error response.
     */
    @ApiResponse(description = "Unprocessable content")
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(
        UserIsNotProjectMemberException::class,
    )
    fun handleUnprocessableContentException(
        ex: AppException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY
        val errorResponse = buildErrorResponse(ex, request)
        logWarning(ex, status, errorResponse)
        return ResponseEntity(errorResponse, status)
    }

    /**
     * Handles all other uncaught exceptions.
     * Returns 500 Internal Server Error with a generic error message.
     */
    @ApiResponse(description = "Internal server error")
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val locale = LocaleContextHolder.getLocale()
        val code = MessageKeys.ERROR_APP_GENERIC
        val message =
            messageSource.getMessage(
                code,
                null,
                "An unexpected error occurred. Please try again later.",
                locale,
            )
        val errorResponse =
            ErrorResponse(
                code = code,
                error = ex::class.simpleName,
                message = message ?: "Unexpected error",
                path = request.requestURI,
            )

        log.error(
            "Unhandled generic exception: {}",
            message,
            ex,
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Logs a warning for AppException with detailed information.
     * This method is used internally to log exceptions consistently.
     */
    private fun logWarning(
        ex: AppException,
        status: HttpStatus,
        response: ErrorResponse,
    ) {
        log.warn(
            "{} ({}): {} - Details: {}",
            response.error,
            status.value(),
            response.message,
            response.details,
            ex,
        )
    }

    /**
     * Builds a detailed error response for AppException.
     * This method is used internally to create consistent error responses.
     */
    private fun buildErrorResponse(
        ex: AppException,
        request: HttpServletRequest,
    ): ErrorResponse {
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(ex.errorCode, ex.args, ex.message, locale)

        return ErrorResponse(
            code = ex.errorCode,
            error = ex::class.simpleName,
            message = message ?: "Unexpected error",
            path = request.requestURI,
            details = ex.args.takeIf { it.isNotEmpty() },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
