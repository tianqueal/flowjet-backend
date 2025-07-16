package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.task.TaskStatusResponse
import com.tianqueal.flowjet.backend.repositories.TaskStatusRepository
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.enums.TaskStatusEnum
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("TaskStatus Controller Integration Tests")
class TaskStatusControllerIntegrationTests
    @Autowired
    constructor(
        private val taskStatusRepository: TaskStatusRepository,
    ) : AbstractAuthenticatableControllerTest() {
        // ===================================
        // Security and Authorization Tests
        // ===================================

        @Nested
        @DisplayName("Security and Authorization Tests")
        inner class SecurityTests {
            @Test
            fun `getAllTaskStatuses without authentication should return Unauthorized`() {
                // Act & Assert
                mockMvc
                    .get(TestUris.TASK_STATUSES)
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `getAllTaskStatuses with invalid token should return Unauthorized`() {
                // Act & Assert
                mockMvc
                    .get(TestUris.TASK_STATUSES) {
                        header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                    }.andExpect { status { isUnauthorized() } }
            }

            @ParameterizedTest
            @ValueSource(strings = ["regular.user", "project.owner", "project.viewer"])
            fun `getAllTaskStatuses should work for any authenticated user role`(username: String) {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken(username)

                // Act & Assert
                mockMvc
                    .get(TestUris.TASK_STATUSES) {
                        header(HttpHeaders.AUTHORIZATION, userToken)
                    }.andExpect { status { isOk() } }
            }
        }

        // ===================================
        // Functional and Data Tests
        // ===================================

        @Nested
        @DisplayName("Functional and Data Tests")
        inner class FunctionalTests {
            @Test
            fun `getAllTaskStatuses should return OK and a valid list of statuses with correct structure`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("test.user")

                // Act
                val result =
                    mockMvc
                        .get(TestUris.TASK_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect {
                            status { isOk() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }.andReturn()

                // Assert
                val taskStatuses: List<TaskStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            TaskStatusResponse::class.java,
                        ),
                    )

                assertTrue(taskStatuses.isNotEmpty(), "The list of task statuses should not be empty.")

                // Verify the structure of the first element as a sample (API contract validation)
                val firstStatus = taskStatuses.first()
                assertTrue(firstStatus.id > 0, "Status ID must be a positive integer.")
                assertTrue(firstStatus.name.isNotBlank(), "Status name must not be blank.")
                assertTrue(firstStatus.code.name.isNotBlank(), "Status code must not be blank.")
            }

            @Test
            fun `getAllTaskStatuses should return all statuses from the database`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("data.integrity.user")
                val expectedCount = taskStatusRepository.count()
                assertTrue(expectedCount > 0, "Database should contain task statuses for this test to be meaningful.")

                // Act
                val result =
                    mockMvc
                        .get(TestUris.TASK_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val taskStatuses: List<TaskStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            TaskStatusResponse::class.java,
                        ),
                    )

                assertEquals(
                    expectedCount,
                    taskStatuses.size.toLong(),
                    "The number of returned statuses should match the count in the database.",
                )
            }

            @Test
            fun `getAllTaskStatuses should include all essential statuses defined in the enum`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("essential.status.user")
                val expectedStatusCodes = TaskStatusEnum.entries // Gets all values from the enum

                // Act
                val result =
                    mockMvc
                        .get(TestUris.TASK_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val taskStatuses: List<TaskStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            TaskStatusResponse::class.java,
                        ),
                    )

                val returnedCodes = taskStatuses.map { it.code }
                assertTrue(
                    returnedCodes.containsAll(expectedStatusCodes),
                    "The response must contain all essential statuses defined in TaskStatusEnum.",
                )
            }
        }
    }
