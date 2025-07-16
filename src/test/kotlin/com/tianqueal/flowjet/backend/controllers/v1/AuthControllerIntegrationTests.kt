package com.tianqueal.flowjet.backend.controllers.v1

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetConfirmRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.VerifyEmailResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.PasswordResetService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTests
    @Autowired
    constructor(
        private val emailVerificationService: EmailVerificationService,
        private val passwordResetService: PasswordResetService,
    ) : AbstractAuthenticatableControllerTest() {
        @Test
        fun `login with valid username should return OK and JWT`() {
            createTestUser()

            val loginRequest = TestDataUtils.createTestLoginRequest()

            val result =
                mockMvc
                    .post(TestUris.LOGIN_URI) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(loginRequest)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            val loginResponse =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    LoginResponse::class.java,
                )
            val jwtResponse = loginResponse.jwtResponse

            assertThat(jwtResponse.token).isNotBlank
            assertThat(jwtResponse.tokenPurpose).isEqualTo(SecurityConstants.TOKEN_PURPOSE_ACCESS)
        }

        @Test
        fun `login with valid email should return OK and JWT`() {
            createTestUser()

            val loginRequest =
                TestDataUtils.createTestLoginRequest(
                    usernameOrEmail = TestDataUtils.DEFAULT_EMAIL,
                )

            val result =
                mockMvc
                    .post(TestUris.LOGIN_URI) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(loginRequest)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            val loginResponse =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    LoginResponse::class.java,
                )
            val jwtResponse = loginResponse.jwtResponse

            assertThat(jwtResponse.token).isNotBlank
            assertThat(jwtResponse.tokenPurpose).isEqualTo(SecurityConstants.TOKEN_PURPOSE_ACCESS)
        }

        @Test
        fun `login with unregistered user should return Unauthorized`() {
            val loginRequest = TestDataUtils.createTestLoginRequest()

            mockMvc
                .post(TestUris.LOGIN_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(loginRequest)
                }.andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `register with valid data should create user and return Created`() {
            val createUserRequest = TestDataUtils.createTestUserRegisterRequest()

            mockMvc
                .post(REGISTER_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(createUserRequest)
                }.andExpect { status { isCreated() } }
        }

        @Test
        fun `register with valid data should send verification email`() {
            // Arrange
            val createUserRequest = TestDataUtils.createTestUserRegisterRequest()

            // Act
            mockMvc
                .post(REGISTER_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(createUserRequest)
                }.andExpect { status { isCreated() } }

            // Assert
            val receivedMessages = greenMail.receivedMessages
            assertEquals(1, receivedMessages.size)

            val message = receivedMessages[0]
            assertEquals(TestDataUtils.DEFAULT_EMAIL, message.allRecipients[0].toString())
            assertTrue(GreenMailUtil.getBody(message).contains("Verify Your Email Address", ignoreCase = true))
        }

        @Test
        fun `register with existing username should return conflict`() {
            // Arrange
            val createUserRequest = TestDataUtils.createTestUserRequest()
            userService.create(createUserRequest)

            // Act
            val duplicateRequest = createUserRequest.copy(email = "test.user2@example.com")
            val result =
                mockMvc
                    .post(REGISTER_URI) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(duplicateRequest)
                    }.andReturn()
            // Assert
            assertEquals(HttpStatus.CONFLICT.value(), result.response.status)
        }

        @Test
        fun `register with existing email should return conflict`() {
            // Arrange
            val createUserRequest = TestDataUtils.createTestUserRequest()
            userService.create(createUserRequest)

            // Act
            val duplicateRequest = createUserRequest.copy(username = "test.user2")
            val result =
                mockMvc
                    .post(REGISTER_URI) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(duplicateRequest)
                    }.andReturn()

            // Assert
            assertEquals(HttpStatus.CONFLICT.value(), result.response.status)
        }

        @Test
        fun `verify email with valid token should return OK`() {
            // Arrange
            val user = createTestUser()
            val token = emailVerificationService.generateToken(user)

            // Act
            val uri =
                UriComponentsBuilder
                    .fromPath(VERIFY_EMAIL_URI)
                    .queryParam("token", token)
                    .build()
                    .toUri()

            val result =
                mockMvc
                    .get(uri)
                    .andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val response =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    VerifyEmailResponse::class.java,
                )

            assertTrue(response.success)
        }

        @Test
        @Transactional(readOnly = true)
        fun `verify email with invalid token should return Unauthorized`() {
            // Arrange
            val invalidToken = "invalid-token-123"

            val uri =
                UriComponentsBuilder
                    .fromPath(VERIFY_EMAIL_URI)
                    .queryParam("token", invalidToken)
                    .build()
                    .toUri()

            // Act & Assert
            mockMvc
                .get(uri)
                .andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `register should send verification email containing token link`() {
            // Arrange
            val userRegisterRequest = TestDataUtils.createTestUserRegisterRequest()

            // Act
            mockMvc
                .post(REGISTER_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(userRegisterRequest)
                }.andExpect { status { isCreated() } }

            // Assert
            val receivedMessages = greenMail.receivedMessages
            assertEquals(1, receivedMessages.size)
            val body = GreenMailUtil.getBody(receivedMessages[0])
            assertTrue(body.contains("?token="))
        }

        @Test
        fun `register with invalid data should return BadRequest`() {
            // Arrange
            val invalidRequest =
                TestDataUtils.createTestUserRequest(
                    username = "",
                    email = "not-an-email",
                    name = "",
                    password = "123",
                )

            // Act & Assert
            mockMvc
                .post(REGISTER_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(invalidRequest)
                }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `admin can access admin-only endpoint`() {
            // Arrange: create admin user and get JWT token
            val admin =
                userService.createAdminUser(
                    CreateAdminUserRequest(
                        username = "test.admin1",
                        email = "test.admin1@example.com",
                        name = "Test Admin One",
                        password = TestDataUtils.DEFAULT_PASSWORD,
                    ),
                )
            val token = loginAndGetToken(admin.username)

            // Act & Assert
            mockMvc
                .get(ADMIN_USERS_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isOk() } }
        }

        @Test
        fun `normal user access restrictions`() {
            // Arrange: create normal user and get JWT token
            val (_, token) = createTestUserAndGetToken()

            // Act & Assert
            mockMvc
                .get(ADMIN_USERS_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isForbidden() } }

            // Act & Assert
            mockMvc
                .get(USER_ME_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isOk() } }
        }

        @Test
        fun `requestPasswordReset with registered email should send email and return OK`() {
            // Arrange
            val user = createTestUser()

            // Act
            mockMvc
                .post(PASSWORD_RESET_REQUEST_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(PasswordResetRequest(user.email))
                }.andExpect { status { isOk() } }

            // Assert
            val receivedMessages = greenMail.receivedMessages
            assertEquals(1, receivedMessages.size)
            val message = receivedMessages[0]
            assertEquals(user.email, message.allRecipients[0].toString())
            val body = GreenMailUtil.getBody(message)
            assertTrue(body.contains("password reset", ignoreCase = true))
            assertTrue(body.contains("?token="))
        }

        @Test
        fun `requestPasswordReset with unregistered email should return OK and not send email`() {
            // Arrange
            val request = PasswordResetRequest("notfound@example.com")

            // Act
            mockMvc
                .post(PASSWORD_RESET_REQUEST_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(request)
                }.andExpect { status { isOk() } }

            // Assert
            assertEquals(0, greenMail.receivedMessages.size)
        }

        @Test
        fun `confirmPasswordReset with valid token should reset password and allow login`() {
            // Arrange
            val user = createTestUser()
            val passwordResetToken = passwordResetService.generatePasswordResetToken(user.email)
            val newPassword = "NewSecureP@ssw0rd123"

            val confirmRequest =
                PasswordResetConfirmRequest(
                    token = passwordResetToken,
                    newPassword = newPassword,
                )

            // Act
            mockMvc
                .post(PASSWORD_RESET_CONFIRM_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(confirmRequest)
                }.andExpect { status { isOk() } }

            // Assert
            val loginRequest = LoginRequest(user.email, newPassword)
            mockMvc
                .post(TestUris.LOGIN_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(loginRequest)
                }.andExpect { status { isOk() } }
        }

        @Test
        fun `confirmPasswordReset with invalid token should return BadRequest`() {
            // Arrange
            val confirmRequest =
                PasswordResetConfirmRequest(
                    token = "invalid-token-123",
                    newPassword = "AnyPassword123",
                )

            // Act & Assert
            mockMvc
                .post(PASSWORD_RESET_CONFIRM_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(confirmRequest)
                }.andExpect { status { isBadRequest() } }
        }

        companion object {
            @JvmField
            @RegisterExtension
            val greenMail = GreenMailExtension(ServerSetupTest.SMTP)

            private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.AUTH}"
            private const val REGISTER_URI = "$BASE_URI${ApiPaths.REGISTER}"
            private const val PASSWORD_RESET_REQUEST_URI = "$BASE_URI${ApiPaths.PASSWORD_RESET}/request"
            private const val PASSWORD_RESET_CONFIRM_URI = "$BASE_URI${ApiPaths.PASSWORD_RESET}/confirm"
            private const val VERIFY_EMAIL_URI = "$BASE_URI${ApiPaths.VERIFY_EMAIL}"
            private const val ADMIN_USERS_URI = "${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}"
            private const val USER_ME_URI = "${ApiPaths.V1}${ApiPaths.USERS_ME}"
        }
    }
