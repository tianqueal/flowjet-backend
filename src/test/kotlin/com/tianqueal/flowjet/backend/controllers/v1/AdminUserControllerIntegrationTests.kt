package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserByAdminRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.enums.RoleName
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerIntegrationTests @Autowired constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val userRepository: UserRepository,
  private val userService: UserService,
) {
  @BeforeEach
  fun setUp() {
    userRepository.hardDeleteAll()
  }

  @Test
  fun `create user with valid data should return Created and user data`() {
    // Arrange
    val request = TestDataUtils.createTestUserRequest()

    // Act
    val result = mockMvc.post(BASE_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(request)
      with(adminAuth())
    }
      .andExpect { status { isCreated() } }
      .andReturn()

    // Assert
    val responseBody = result.response.contentAsString
    val userResponse = objectMapper.readValue(responseBody, UserResponse::class.java)
    assertEquals(request.username, userResponse.username)
    assertEquals(request.email, userResponse.email)
    assertNotNull(userResponse.id)
  }

  @Test
  fun `get user by id should return user when exists`() {
    // Arrange
    val created = createTestUser()

    // Act
    val uri = UriComponentsBuilder
      .fromUriString(BASE_URI)
      .pathSegment(created.id.toString())
      .build()
      .toUri()

    val result = mockMvc.get(uri) {
      with(adminAuth())
    }
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    val userResponse = objectMapper.readValue(result.response.contentAsString, UserResponse::class.java)
    assertEquals(created.id, userResponse.id)
    assertEquals(TestDataUtils.DEFAULT_USERNAME, userResponse.username)
  }

  @Test
  fun `get all users should return paginated list`() {
    // Arrange
    createTestUser("test.user1", "test.user1@example.com")
    createTestUser("test.user2", "test.user2@example.com")

    // Act
    val uri = UriComponentsBuilder
      .fromUriString(BASE_URI)
      .queryParam("username", "user")
      .build()
      .toUri()

    val result = mockMvc.get(uri) {
      with(adminAuth())
    }
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    val page = objectMapper.readTree(result.response.contentAsString)
    assertTrue(page["content"].size() == 2)
    assertTrue(page["content"].any { it["username"].asText().contains("user") })
  }

  @Test
  fun `update user should modify user data`() {
    // Arrange
    val created = createTestUser()
    val updateRequest = UpdateUserByAdminRequest(
      name = "Updated Name",
      email = "updated.user@example.com",
      username = "updated.user"
    )

    // Act
    val uri = UriComponentsBuilder
      .fromUriString(BASE_URI)
      .pathSegment(created.id.toString())
      .build()
      .toUri()

    val result = mockMvc.put(uri) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(updateRequest)
      with(adminAuth())
    }
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    val userResponse = objectMapper.readValue(result.response.contentAsString, UserResponse::class.java)
    assertEquals("Updated Name", userResponse.name)
    assertEquals("updated.user@example.com", userResponse.email)
    assertEquals("updated.user", userResponse.username)
  }

  @Test
  fun `create user with duplicate email should return Conflict`() {
    // Arrange
    createTestUser("dup.user", "dup.user@example.com")
    val request = CreateUserRequest(
      username = "other.user",
      email = "dup.user@example.com",
      name = "Other User",
      password = TestDataUtils.DEFAULT_PASSWORD
    )

    // Act & Assert
    mockMvc.post(BASE_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(request)
      with(adminAuth())
    }
      .andExpect { status { isConflict() } }
  }

  @Test
  fun `non-admin cannot access admin endpoints`() {
    // Arrange
    userService.createUserByAdmin(
      CreateUserRequest(
        username = "not.admin",
        email = "not.admin@example.com",
        name = "Not Admin",
        password = TestDataUtils.DEFAULT_PASSWORD
      )
    )

    // Act & Assert
    mockMvc.get(BASE_URI) {
      with(
        SecurityMockMvcRequestPostProcessors
          .user("not.admin").roles(RoleName.ROLE_USER.shortName())
      )
    }
      .andExpect { status { isForbidden() } }
  }

  private fun adminAuth() = SecurityMockMvcRequestPostProcessors
    .user("admin")
    .roles(RoleName.ROLE_ADMIN.shortName())

  private fun createTestUser(
    username: String = TestDataUtils.DEFAULT_USERNAME,
    email: String = TestDataUtils.DEFAULT_EMAIL
  ) =
    userService.createUserByAdmin(
      TestDataUtils.createTestUserRequest(
        username = username,
        email = email
      )
    )

  companion object {
    private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}"
  }
}
