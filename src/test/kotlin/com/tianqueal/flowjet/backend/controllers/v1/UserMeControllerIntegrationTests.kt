package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserSelfRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.DefaultRoles
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
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserMeControllerIntegrationTests @Autowired constructor(
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
  fun `getCurrentUser should return user profile for authenticated user`() {
    // Arrange
    val (user, auth) = createAndAuthUser()

    // Act
    val result = mockMvc.get(BASE_URI) {
      with(auth)
    }
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    val userResponse = objectMapper.readValue(result.response.contentAsString, UserResponse::class.java)
    assertEquals(user.username, userResponse.username)
    assertEquals(user.email, userResponse.email)
    assertEquals(user.name, userResponse.name)
  }

  @Test
  fun `getCurrentUser should return 401 for unauthenticated request`() {
    // Arrange: No authentication

    // Act & Assert
    mockMvc.get(BASE_URI)
      .andExpect { status { isUnauthorized() } }
  }

  @Test
  fun `updateCurrentUser should update user profile for authenticated user`() {
    // Arrange
    val (user, auth) = createAndAuthUser()

    val updateRequest = UpdateUserSelfRequest(
      name = "Updated Name",
      avatarUrl = "https://example.com/avatar.png"
    )

    // Act
    val result = mockMvc.put(BASE_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(updateRequest)
      with(auth)
    }
      .andExpect { status { isOk() } }
      .andReturn()

    // Assert
    val userResponse = objectMapper.readValue(result.response.contentAsString, UserResponse::class.java)
    assertEquals("Updated Name", userResponse.name)
    assertEquals(user.username, userResponse.username)
    assertEquals(user.email, userResponse.email)
    assertEquals("https://example.com/avatar.png", userResponse.avatarUrl)
  }

  @Test
  fun `updateCurrentUser should return 401 for unauthenticated request`() {
    // Arrange: No authentication
    val updateRequest = UpdateUserSelfRequest(
      name = "Should Not Update",
    )

    // Act & Assert
    mockMvc.put(BASE_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(updateRequest)
    }
      .andExpect { status { isUnauthorized() } }
  }

  @Test
  fun `updateCurrentUser should return 400 for invalid data`() {
    // Arrange
    val (_, auth) = createAndAuthUser()
    val invalidRequest = UpdateUserSelfRequest(
      name = "", // Not blank
      avatarUrl = "" // Not blank and valid URL
    )

    // Act & Assert
    mockMvc.put(BASE_URI) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsString(invalidRequest)
      with(auth)
    }
      .andExpect { status { isBadRequest() } }
  }

  private fun createAndAuthUser(
    username: String = TestDataUtils.DEFAULT_USERNAME,
    email: String = TestDataUtils.DEFAULT_EMAIL,
    name: String = TestDataUtils.DEFAULT_NAME,
    password: String = TestDataUtils.DEFAULT_PASSWORD
  ) = Pair(
    userService.createUserByAdmin(
      CreateUserRequest(
        username = username,
        email = email,
        name = name,
        password = password
      )
    ),
    SecurityMockMvcRequestPostProcessors.user(username)
      .roles(*DefaultRoles.USER.map { it.shortName() }.toTypedArray())
  )

  companion object {
    private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.USERS_ME}"
  }
}
