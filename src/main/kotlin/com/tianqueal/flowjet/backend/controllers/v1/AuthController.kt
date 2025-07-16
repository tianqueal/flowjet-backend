package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetConfirmRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.UserRegisterResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.VerifyEmailResponse
import com.tianqueal.flowjet.backend.security.jwt.JwtTokenProvider
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.PasswordResetService
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

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
        description = "Takes username/email and password and returns a JWT if credentials are valid.",
    )
    @PostMapping(ApiPaths.LOGIN)
    fun login(
        @SwaggerRequestBody(description = "Login request containing username/email and password")
        @Valid
        @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.usernameOrEmail,
                    loginRequest.password,
                ),
            )
        SecurityContextHolder.getContext().authentication = authentication
        val tokenDetails =
            jwtTokenProvider.generateAccessTokenDetails(
                authentication,
                loginRequest.rememberMe,
            )
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.AUTH_LOGIN_SUCCESS, null, locale)
        return ResponseEntity.ok(
            LoginResponse(
                message = message,
                jwtResponse = tokenDetails,
            ),
        )
    }

    @Operation(
        summary = "Register a new user",
        description = "Registers a new user with the provided details.",
    )
    @PostMapping(ApiPaths.REGISTER)
    fun register(
        @SwaggerRequestBody(description = "User data to create")
        @Valid
        @RequestBody userRegisterRequest: UserRegisterRequest,
    ): ResponseEntity<UserRegisterResponse> {
        val createdUser = userService.registerUser(userRegisterRequest)
        emailVerificationService.sendEmail(
            user = createdUser,
            apiVersionPath = ApiPaths.V1,
        )
        val location =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("${ApiPaths.V1}${ApiPaths.USER_BY_ID}")
                .buildAndExpand(createdUser.id)
                .toUri()
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.AUTH_REGISTER_SUCCESS, null, locale)
        return ResponseEntity.created(location).body(
            UserRegisterResponse(
                message = message,
                userResponse = createdUser,
            ),
        )
    }

    @Operation(
        summary = "Verify email address",
        description = "Verifies the user's email address using a verification token.",
    )
    @GetMapping(ApiPaths.VERIFY_EMAIL)
    fun verifyEmail(
        @Parameter(description = "Verification token")
        @RequestParam token: String,
    ): ResponseEntity<VerifyEmailResponse> {
        emailVerificationService.verifyTokenAndMarkAsVerified(token)
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.EMAIL_VERIFICATION_SUCCESS, null, locale)
        return ResponseEntity.ok(VerifyEmailResponse(success = true, message = message))
    }

    @Operation(summary = "Reset password", description = "Sends a password reset email to the user.")
    @PostMapping("${ApiPaths.PASSWORD_RESET}/request")
    fun requestPasswordReset(
        @SwaggerRequestBody(description = "Email address to send the reset link")
        @Valid
        @RequestBody passwordResetRequest: PasswordResetRequest,
    ): ResponseEntity<PasswordResetResponse> {
        passwordResetService.sendPasswordResetEmail(
            email = passwordResetRequest.email,
            apiVersionPath = ApiPaths.V1,
        )
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.PASSWORD_RESET_REQUEST_SUCCESS, null, locale)
        return ResponseEntity.ok(PasswordResetResponse(message))
    }

    @Operation(
        summary = "Confirm password reset",
        description = "Confirms the password reset using a token and sets the new password.",
    )
    @PostMapping("${ApiPaths.PASSWORD_RESET}/confirm")
    fun confirmPasswordReset(
        @SwaggerRequestBody(description = "Password reset confirmation request containing token and new password")
        @Valid
        @RequestBody passwordResetConfirmRequest: PasswordResetConfirmRequest,
    ): ResponseEntity<PasswordResetResponse> {
        passwordResetService.verifyTokenAndResetPassword(
            token = passwordResetConfirmRequest.token,
            newPassword = passwordResetConfirmRequest.newPassword,
        )
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(MessageKeys.PASSWORD_RESET_CONFIRM_SUCCESS, null, locale)
        return ResponseEntity.ok(PasswordResetResponse(message))
    }
}
