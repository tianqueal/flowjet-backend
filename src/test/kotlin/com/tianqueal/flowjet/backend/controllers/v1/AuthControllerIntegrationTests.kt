package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetConfirmRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.PasswordResetRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateAdminUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.EmailVerificationService
import com.tianqueal.flowjet.backend.services.PasswordResetService
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTests @Autowired constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val userRepository: UserRepository,
  private val userService: UserService,
  private val emailVerificationService: EmailVerificationService,
  private val passwordResetService: PasswordResetService,
) {
  @BeforeEach
  fun setUp() {
    userRepository.hardDeleteAll()
  }

  @Test
  fun `login with valid username should return OK and JWT`() {
    userService.createUserByAdmin(
      TestDataUtils.createTestUserRequest()
    )

    val loginRequest = TestDataUtils.createTestLoginRequest()

    val mvcResult = mockMvc.post(LOGIN_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(loginRequest)
    }
      .andExpect { status { isOk() } }
      .andReturn()

    val responseBody = mvcResult.response.contentAsString
    val loginResponse = objectMapper.readValue(responseBody, LoginResponse::class.java)
    val jwtResponse = loginResponse.jwtResponse

    assertThat(jwtResponse.token).isNotBlank
    assertThat(jwtResponse.tokenPurpose).isEqualTo(SecurityConstants.TOKEN_PURPOSE_ACCESS)
  }

  @Test
  fun `login with valid email should return OK and JWT`() {
    userService.createUserByAdmin(
      TestDataUtils.createTestUserRequest()
    )

    val loginRequest = TestDataUtils.createTestLoginRequest(
      usernameOrEmail = TestDataUtils.DEFAULT_EMAIL
    )

    val mvcResult = mockMvc.post(LOGIN_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(loginRequest)
    }
      .andExpect { status { isOk() } }
      .andReturn()

    val responseBody = mvcResult.response.contentAsString
    val loginResponse = objectMapper.readValue(responseBody, LoginResponse::class.java)
    val jwtResponse = loginResponse.jwtResponse

    assertThat(jwtResponse.token).isNotBlank
    assertThat(jwtResponse.tokenPurpose).isEqualTo(SecurityConstants.TOKEN_PURPOSE_ACCESS)
  }

  @Test
  fun `login with unregistered user should return Unauthorized`() {
    val loginRequest = TestDataUtils.createTestLoginRequest()

    mockMvc.post(LOGIN_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(loginRequest)
    }
      .andExpect { status { isUnauthorized() } }
  }

  @Test
  fun `register with valid data should create user and return Created`() {
    val createUserRequest = TestDataUtils.createTestUserRegisterRequest()

    mockMvc.post(REGISTER_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(createUserRequest)
    }
      .andExpect { status { isCreated() } }
  }

  @Test
  fun `register with valid data should send verification email`() {
    // Arrange
    val createUserRequest = TestDataUtils.createTestUserRegisterRequest()

    // Act
    mockMvc.post(REGISTER_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(createUserRequest)
    }
      .andExpect { status { isCreated() } }

    // Assert
    val receivedMessages = greenMail.receivedMessages
    assertEquals(1, receivedMessages.size)

    val message = receivedMessages[0]
    assertEquals(TestDataUtils.DEFAULT_EMAIL, message.allRecipients[0].toString())
    assertTrue(GreenMailUtil.getBody(message).contains("Verify Your Email Address", ignoreCase = true))
  }

  @Test
  fun `register with existing email should return conflict`() {
    // Arrange
    val createUserRequest = TestDataUtils.createTestUserRequest()
    userService.createUserByAdmin(createUserRequest)

    // Act
    val duplicateRequest = createUserRequest.copy(username = "user2")
    val result = mockMvc.post(REGISTER_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(duplicateRequest)
    }
      .andReturn()

    // Assert
    assertEquals(HttpStatus.CONFLICT.value(), result.response.status)
  }

  @Test
  fun `verify email with valid token should return OK`() {
    // Arrange
    val user = userService.createUserByAdmin(
      TestDataUtils.createTestUserRequest()
    )
    val token = emailVerificationService.generateEmailVerificationToken(user)

    // Act
    val uri = UriComponentsBuilder
      .fromPath(VERIFY_EMAIL_URI)
      .queryParam("token", token)
      .build()
      .toUri()

    val result = mockMvc.get(uri)
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    assertTrue(result.response.contentAsString.contains("success"))
  }

  @Test
  fun `verify email with invalid token should return Unauthorized`() {
    // Arrange
    val invalidToken = "invalid-token-123"

    val uri = UriComponentsBuilder
      .fromPath(VERIFY_EMAIL_URI)
      .queryParam("token", invalidToken)
      .build()
      .toUri()

    // Act & Assert
    mockMvc.get(uri)
      .andExpect { status { isUnauthorized() } }
  }

  @Test
  fun `register should send verification email containing token link`() {
    // Arrange
    val userRegisterRequest = TestDataUtils.createTestUserRegisterRequest()

    // Act
    mockMvc.post(REGISTER_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(userRegisterRequest)
    }
      .andExpect { status { isCreated() } }

    // Assert
    val receivedMessages = greenMail.receivedMessages
    assertEquals(1, receivedMessages.size)
    val body = GreenMailUtil.getBody(receivedMessages[0])
    assertTrue(body.contains("?token="))
  }

  @Test
  fun `register with invalid data should return BadRequest`() {
    // Arrange
    val invalidRequest = TestDataUtils.createTestUserRequest(
      username = "",
      email = "not-an-email",
      name = "",
      password = "123"
    )

    // Act & Assert
    mockMvc.post(REGISTER_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(invalidRequest)
    }
      .andExpect { status { isBadRequest() } }
  }

  @Test
  fun `admin can access admin-only endpoint`() {
    // Arrange: create admin user and get JWT token
    val admin = userService.createAdminUser(
      CreateAdminUserRequest(
        username = "admin1",
        email = "admin1@example.com",
        name = "Admin One",
        password = TestDataUtils.DEFAULT_PASSWORD
      )
    )
    val token = loginAndGetToken(admin.username)

    // Act & Assert
    mockMvc.get(ADMIN_USERS_URI) {
      header(HttpHeaders.AUTHORIZATION, token)
    }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `normal user access restrictions`() {
    // Arrange: create normal user and get JWT token
    val user = userService.createUserByAdmin(
      CreateUserRequest(
        username = "user1",
        email = "user1@example.com",
        name = "User One",
        password = TestDataUtils.DEFAULT_PASSWORD
      )
    )
    val token = loginAndGetToken(user.username)

    // Act & Assert
    mockMvc.get(ADMIN_USERS_URI) {
      header(HttpHeaders.AUTHORIZATION, token)
    }
      .andExpect { status { isForbidden() } }

    // Act & Assert
    mockMvc.get(USER_ME_URI) {
      header(HttpHeaders.AUTHORIZATION, token)
    }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `requestPasswordReset with registered email should send email and return OK`() {
    // Arrange
    userService.createUserByAdmin(TestDataUtils.createTestUserRequest())

    // Act
    mockMvc.post(PASSWORD_RESET_REQUEST_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(PasswordResetRequest(TestDataUtils.DEFAULT_EMAIL))
    }
      .andExpect { status { isOk() } }

    // Assert
    val receivedMessages = greenMail.receivedMessages
    assertEquals(1, receivedMessages.size)
    val message = receivedMessages[0]
    assertEquals(TestDataUtils.DEFAULT_EMAIL, message.allRecipients[0].toString())
    val body = GreenMailUtil.getBody(message)
    assertTrue(body.contains("password reset", ignoreCase = true))
    assertTrue(body.contains("?token="))
  }

  @Test
  fun `requestPasswordReset with unregistered email should return OK and not send email`() {
    // Arrange
    val request = PasswordResetRequest("notfound@example.com")

    // Act
    mockMvc.post(PASSWORD_RESET_REQUEST_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(request)
    }
      .andExpect { status { isOk() } }

    // Assert
    assertEquals(0, greenMail.receivedMessages.size)
  }

  @Test
  fun `confirmPasswordReset with valid token should reset password and allow login`() {
    // Arrange
    val user = userService.createUserByAdmin(TestDataUtils.createTestUserRequest())
    val token = passwordResetService.generatePasswordResetToken(user.email)
    val newPassword = "NewSecureP@ssw0rd123"

    val confirmRequest = PasswordResetConfirmRequest(
      token = token,
      newPassword = newPassword
    )

    // Act
    mockMvc.post(PASSWORD_RESET_CONFIRM_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(confirmRequest)
    }
      .andExpect { status { isOk() } }

    // Assert
    val loginRequest = LoginRequest(user.email, newPassword)
    mockMvc.post(LOGIN_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(loginRequest)
    }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `confirmPasswordReset with invalid token should return BadRequest`() {
    // Arrange
    val confirmRequest = PasswordResetConfirmRequest(
      token = "invalid-token-123",
      newPassword = "AnyPassword123"
    )

    // Act & Assert
    mockMvc.post(PASSWORD_RESET_CONFIRM_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(confirmRequest)
    }
      .andExpect { status { isBadRequest() } }
  }

  private fun loginAndGetToken(
    usernameOrEmail: String,
    password: String = TestDataUtils.DEFAULT_PASSWORD
  ): String {
    val loginRequest = LoginRequest(usernameOrEmail, password)
    val result = mockMvc.post(LOGIN_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(loginRequest)
    }
      .andExpect { status { isOk() } }
      .andReturn()

    val loginResponse = objectMapper.readValue(
      result.response.contentAsString,
      LoginResponse::class.java
    )
    return "Bearer ${loginResponse.jwtResponse.token}"
  }

  companion object {
    @JvmField
    @RegisterExtension
    val greenMail = GreenMailExtension(ServerSetupTest.SMTP)

    private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.AUTH}"

    private val LOGIN_URI = UriComponentsBuilder
      .fromPath(BASE_URI)
      .path(ApiPaths.LOGIN)
      .build()
      .toUri()

    private val REGISTER_URI = UriComponentsBuilder
      .fromPath(BASE_URI)
      .path(ApiPaths.REGISTER)
      .build()
      .toUri()

    private val PASSWORD_RESET_REQUEST_URI = UriComponentsBuilder
      .fromPath(BASE_URI)
      .path(ApiPaths.PASSWORD_RESET)
      .pathSegment("request")
      .build()
      .toUri()

    private val PASSWORD_RESET_CONFIRM_URI = UriComponentsBuilder
      .fromPath(BASE_URI)
      .path(ApiPaths.PASSWORD_RESET)
      .pathSegment("confirm")
      .build()
      .toUri()

    private const val VERIFY_EMAIL_URI = "${BASE_URI}${ApiPaths.VERIFY_EMAIL}"
    private const val ADMIN_USERS_URI = "${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}"
    private const val USER_ME_URI = "${ApiPaths.V1}${ApiPaths.USERS_ME}"
  }
}
