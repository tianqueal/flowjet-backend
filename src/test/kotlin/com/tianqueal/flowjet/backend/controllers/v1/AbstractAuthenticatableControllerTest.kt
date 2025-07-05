package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.auth.LoginResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.user.UserResponse
import com.tianqueal.flowjet.backend.repositories.UserRepository
import com.tianqueal.flowjet.backend.services.UserService
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class AbstractAuthenticatableControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var userService: UserService

    @BeforeEach
    fun authenticatableControllerSetUp() {
        userRepository.hardDeleteAll()
    }

    /**
     * Creates a test user with the given username, email, and password.
     * @param username The username for the test user
     * @param email The email for the test user
     * @param password The password for the test user
     * @return The created UserResponse
     */
    protected fun createTestUser(
        username: String = TestDataUtils.DEFAULT_USERNAME,
        email: String = "$username@example.com",
        password: String = TestDataUtils.DEFAULT_PASSWORD,
    ): UserResponse =
        userService.create(
            TestDataUtils.createTestUserRequest(username = username, email = email, password = password),
        )

    /**
     * Logs in a user and retrieves the JWT token.
     * @param usernameOrEmail The username or email of the user
     * @param password The password of the user
     * @return The JWT token as a Bearer token string
     */
    protected fun loginAndGetToken(
        usernameOrEmail: String,
        password: String = TestDataUtils.DEFAULT_PASSWORD,
    ): String {
        val loginRequest = LoginRequest(usernameOrEmail, password)
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
        return "Bearer ${loginResponse.jwtResponse.token}"
    }

    /**
     * Creates a test user and retrieves the JWT token for that user.
     * @param username The username for the test user
     * @param email The email for the test user
     * @param password The password for the test user
     * @return A pair containing the UserResponse and the JWT token
     */
    protected fun createTestUserAndGetToken(
        username: String = TestDataUtils.DEFAULT_USERNAME,
        email: String = "$username@example.com",
        password: String = TestDataUtils.DEFAULT_PASSWORD,
    ): Pair<UserResponse, String> {
        val userResponse = createTestUser(username = username, email = email, password = password)
        return userResponse to loginAndGetToken(usernameOrEmail = username, password = password)
    }

    /**
     * Creates multiple test users and retrieves their JWT tokens.
     * @param count The number of test users to create
     * @return A list of pairs containing UserResponse and JWT token for each user
     */
    protected fun createMultipleTestUsersAndGetTokens(count: Int): List<Pair<UserResponse, String>> =
        (1..count).map { index ->
            val username = "test.user$index"
            createTestUserAndGetToken(username = username, email = "$username@example.com")
        }
}
