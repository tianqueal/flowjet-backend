package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.user.CreateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.enums.RoleEnum
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AdminUserControllerIntegrationTests : AuthenticatableControllerTest() {
    @Test
    fun `create user with valid data should return Created and user data`() {
        // Arrange
        val request = TestDataUtils.createTestUserRequest()

        // Act
        val result = mockMvc.post(BASE_URI) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(request)
            with(adminAuth())
        }
            .andExpect { status { isCreated() } }
            .andReturn()

        // Assert
        val userResponse = objectMapper.readValue(
            result.response.contentAsByteArray,
            UserResponse::class.java
        )
        assertEquals(request.username, userResponse.username)
        assertEquals(request.email, userResponse.email)
        assertNotNull(userResponse.id)
    }

    @Test
    fun `get user by id should return user when exists`() {
        // Arrange
        val created = createTestUser()

        // Act
        val result = mockMvc.get("$BASE_URI/${created.id}") {
            with(adminAuth())
        }
            .andExpect { status { isOk() } }
            .andReturn()

        // Assert
        val userResponse = objectMapper.readValue(
            result.response.contentAsByteArray,
            UserResponse::class.java
        )
        assertEquals(created.id, userResponse.id)
        assertEquals(TestDataUtils.DEFAULT_USERNAME, userResponse.username)
    }

    @Test
    fun `get all users should return paginated list`() {
        // Arrange
        createTestUser("john.doe")
        createTestUser("test.user1")
        createTestUser("test.user2")

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
        val page = objectMapper.readTree(result.response.contentAsByteArray)
        assertTrue(page["content"].size() == 2)
        assertTrue(page["content"].any { it["username"].asText().contains("user") })
    }

    @Test
    fun `update user should modify user data`() {
        // Arrange
        val created = createTestUser()
        val updateRequest = UpdateUserRequest(
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
            content = objectMapper.writeValueAsBytes(updateRequest)
            with(adminAuth())
        }
            .andExpect { status { isOk() } }
            .andReturn()

        // Assert
        val userResponse = objectMapper.readValue(
            result.response.contentAsByteArray,
            UserResponse::class.java
        )
        assertEquals(updateRequest.name, userResponse.name)
        assertEquals(updateRequest.email, userResponse.email)
        assertEquals(updateRequest.username, userResponse.username)
    }

    @Test
    fun `create user with duplicate email should return Conflict`() {
        // Arrange
        createTestUser("dup.user")
        val request = CreateUserRequest(
            username = "other.user",
            email = "dup.user@example.com",
            name = "Other User",
            password = TestDataUtils.DEFAULT_PASSWORD
        )

        // Act & Assert
        mockMvc.post(BASE_URI) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(request)
            with(adminAuth())
        }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `non-admin cannot access admin endpoints`() {
        // Arrange
        createTestUser("not.admin")

        // Act & Assert
        mockMvc.get(BASE_URI) {
            with(
                SecurityMockMvcRequestPostProcessors
                    .user("not.admin").roles(RoleEnum.ROLE_USER.shortName())
            )
        }
            .andExpect { status { isForbidden() } }
    }

    private fun adminAuth() = SecurityMockMvcRequestPostProcessors
        .user("admin")
        .roles(RoleEnum.ROLE_ADMIN.shortName())

    companion object {
        private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.ADMIN}${ApiPaths.USERS}"
    }
}
