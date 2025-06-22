package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.annotations.PublicOperationErrorResponses
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.*
import com.tianqueal.flowjet.backend.domain.dto.v1.error.ErrorResponse
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.PasswordResetService
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("${ApiPaths.V1}${ApiPaths.AUTH}")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
class AuthController(
  private val authenticationManager: AuthenticationManager,
  private val jwtTokenProvider: JwtTokenProvider,
  private val messageSource: MessageSource,
  private val userService: UserService,
  private val emailVerificationService: EmailVerificationService,
  private val passwordResetService: PasswordResetService,
) {
  @Operation(
    summary = "Authenticate user and return JWT",
    description = "Takes username/email and password and returns a JWT if credentials are valid."
  )
  @SwaggerRequestBody(
    description = "Login request containing username/email and password",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = LoginRequest::class)
    )]
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Authentication successful",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = LoginResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Invalid credentials",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      )
    ]
  )
  @PublicOperationErrorResponses
  @PostMapping(ApiPaths.LOGIN)
  fun login(
    @Valid @RequestBody loginRequest: LoginRequest
  ): ResponseEntity<LoginResponse> {
    val authentication = authenticationManager.authenticate(
      UsernamePasswordAuthenticationToken(
        loginRequest.usernameOrEmail, loginRequest.password
      )
    )
    SecurityContextHolder.getContext().authentication = authentication
    val tokenDetails = jwtTokenProvider.generateAccessTokenDetails(
      authentication,
      loginRequest.rememberMe
    )
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(MessageKeys.AUTH_LOGIN_SUCCESS, null, locale)
    return ResponseEntity.ok(
      LoginResponse(
        message = message,
        jwtResponse = tokenDetails
      )
    )
  }

  @Operation(
    summary = "Register a new user",
    description = "Registers a new user with the provided details."
  )
  @SwaggerRequestBody(
    description = "User data to create",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = UserRegisterRequest::class)
    )]
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = UserRegisterResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "409",
        description = "User already exists",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      )
    ]
  )
  @PublicOperationErrorResponses
  @PostMapping(ApiPaths.REGISTER)
  fun register(
    @Valid @RequestBody userRegisterRequest: UserRegisterRequest
  ): ResponseEntity<UserRegisterResponse> {
    val createdUser = userService.registerUser(userRegisterRequest)
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(MessageKeys.AUTH_REGISTER_SUCCESS, null, locale)
    emailVerificationService.sendEmailVerification(
      user = createdUser,
      apiVersionPath = ApiPaths.V1
    )
    val location = ServletUriComponentsBuilder
      .fromCurrentContextPath()
      .path("${ApiPaths.V1}${ApiPaths.USER_BY_ID}")
      .buildAndExpand(createdUser.id)
      .toUri()
    return ResponseEntity.created(location).body(
      UserRegisterResponse(
        message = message,
        userResponse = createdUser
      )
    )
  }

  @Operation(
    summary = "Verify email address",
    description = "Verifies the user's email address using a verification token."
  )
  @Parameter(
    name = "token",
    description = "Verification token",
    required = true,
    schema = Schema(type = "string")
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Email verified successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = VerifyEmailResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Invalid or expired verification token",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      )
    ]
  )
  @PublicOperationErrorResponses
  @GetMapping(ApiPaths.VERIFY_EMAIL)
  fun verifyEmail(
    @RequestParam token: String
  ): ResponseEntity<VerifyEmailResponse> {
    emailVerificationService.verifyTokenAndMarkAsVerified(token)
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(MessageKeys.EMAIL_VERIFICATION_SUCCESS, null, locale)
    return ResponseEntity.ok(VerifyEmailResponse(success = true, message = message))
  }

  @Operation(
    summary = "Reset password",
    description = "Sends a password reset email to the user."
  )
  @SwaggerRequestBody(
    description = "Email address to send the reset link",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = PasswordResetRequest::class)
    )]
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Password reset email sent successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = PasswordResetResponse::class)
        )]
      ),
    ]
  )
  @PublicOperationErrorResponses
  @PostMapping("${ApiPaths.PASSWORD_RESET}/request")
  fun requestPasswordReset(
    @Valid @RequestBody passwordResetRequest: PasswordResetRequest
  ): ResponseEntity<PasswordResetResponse> {
    passwordResetService.sendPasswordResetEmail(
      email = passwordResetRequest.email,
      apiVersionPath = ApiPaths.V1
    )
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(MessageKeys.PASSWORD_RESET_REQUEST_SUCCESS, null, locale)
    return ResponseEntity.ok(PasswordResetResponse(message))
  }

  @Operation(
    summary = "Confirm password reset",
    description = "Confirms the password reset using a token and sets the new password."
  )
  @SwaggerRequestBody(
    description = "Password reset confirmation request containing token and new password",
    required = true,
    content = [Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = Schema(implementation = PasswordResetConfirmRequest::class)
    )]
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Password reset successfully",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = PasswordResetResponse::class)
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid or expired password reset token",
        content = [Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ErrorResponse::class)
        )]
      )
    ]
  )
  @PublicOperationErrorResponses
  @PostMapping("${ApiPaths.PASSWORD_RESET}/confirm")
  fun confirmPasswordReset(
    @Valid @RequestBody passwordResetConfirmRequest: PasswordResetConfirmRequest
  ): ResponseEntity<PasswordResetResponse> {
    passwordResetService.verifyTokenAndResetPassword(
      token = passwordResetConfirmRequest.token,
      newPassword = passwordResetConfirmRequest.newPassword
    )
    val locale = LocaleContextHolder.getLocale()
    val message = messageSource.getMessage(MessageKeys.PASSWORD_RESET_CONFIRM_SUCCESS, null, locale)
    return ResponseEntity.ok(PasswordResetResponse(message))
  }
}
