package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.user.UpdateUserProfileRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals

class UserMeControllerIntegrationTests : AuthenticatableControllerTest() {
    @Test
    fun `getCurrentUser should return user profile for authenticated user`() {
        // Arrange
        val (user, token) = createTestUserAndGetToken()

        // Act
        val result =
            mockMvc
                .get(BASE_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isOk() } }
                .andReturn()

        // Assert
        val userResponse =
            objectMapper.readValue(
                result.response.contentAsByteArray,
                UserResponse::class.java,
            )
        assertEquals(user.username, userResponse.username)
        assertEquals(user.email, userResponse.email)
        assertEquals(user.name, userResponse.name)
    }

    @Test
    fun `getCurrentUser should return 401 for unauthenticated request`() {
        // Arrange: No authentication

        // Act & Assert
        mockMvc
            .get(BASE_URI)
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `updateCurrentUser should update user profile for authenticated user`() {
        // Arrange
        val (user, token) = createTestUserAndGetToken()

        val updateRequest =
            UpdateUserProfileRequest(
                name = "Updated Name",
                avatarUrl = "https://example.com/avatar.png",
            )

        // Act
        val result =
            mockMvc
                .put(BASE_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(updateRequest)
                }.andExpect { status { isOk() } }
                .andReturn()

        // Assert
        val userResponse =
            objectMapper.readValue(
                result.response.contentAsByteArray,
                UserResponse::class.java,
            )
        assertEquals(updateRequest.name, userResponse.name)
        assertEquals(user.username, userResponse.username)
        assertEquals(user.email, userResponse.email)
        assertEquals(updateRequest.avatarUrl, userResponse.avatarUrl)
    }

    @Test
    fun `updateCurrentUser should return 401 for unauthenticated request`() {
        // Arrange: No authentication
        val updateRequest = UpdateUserProfileRequest(name = "Should Not Update")

        // Act & Assert
        mockMvc
            .put(BASE_URI) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(updateRequest)
            }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `updateCurrentUser should return 400 for invalid data`() {
        // Arrange
        val (_, token) = createTestUserAndGetToken()
        val invalidRequest =
            UpdateUserProfileRequest(
                name = "", // Not blank Constraint
                avatarUrl = "", // Not blank and valid URL Constraint
            )

        // Act & Assert
        mockMvc
            .put(BASE_URI) {
                header(HttpHeaders.AUTHORIZATION, token)
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(invalidRequest)
            }.andExpect { status { isBadRequest() } }
    }

    companion object {
        private const val BASE_URI = "${ApiPaths.V1}${ApiPaths.USERS_ME}"
    }
}
