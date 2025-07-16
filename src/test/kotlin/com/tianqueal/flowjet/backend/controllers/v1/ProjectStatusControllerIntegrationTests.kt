package com.tianqueal.flowjet.backend.controllers.v1

import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectStatusResponse
import com.tianqueal.flowjet.backend.repositories.ProjectStatusRepository
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.enums.ProjectStatusEnum
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

@DisplayName("Project Status Controller Integration Tests")
class ProjectStatusControllerIntegrationTests
    @Autowired
    constructor(
        private val projectStatusRepository: ProjectStatusRepository,
    ) : AbstractAuthenticatableControllerTest() {
        // ===================================
        // Security and Authorization Tests
        // ===================================

        @Nested
        @DisplayName("Security and Authorization Tests")
        inner class SecurityTests {
            @Test
            fun `getAllProjectStatuses without authentication should return Unauthorized`() {
                // Act & Assert
                mockMvc
                    .get(TestUris.PROJECT_STATUSES)
                    .andExpect { status { isUnauthorized() } }
            }

            @Test
            fun `getAllProjectStatuses with invalid token should return Unauthorized`() {
                // Act & Assert
                mockMvc
                    .get(TestUris.PROJECT_STATUSES) {
                        header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                    }.andExpect { status { isUnauthorized() } }
            }

            @ParameterizedTest
            @ValueSource(strings = ["regular.user", "project.owner", "project.viewer"])
            fun `getAllProjectStatuses should work for any authenticated user role`(username: String) {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken(username)

                // Act & Assert
                mockMvc
                    .get(TestUris.PROJECT_STATUSES) {
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
            fun `getAllProjectStatuses should return OK and a valid list of statuses`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("test.user")

                // Act
                val result =
                    mockMvc
                        .get(TestUris.PROJECT_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect {
                            status { isOk() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }.andReturn()

                // Assert
                val projectStatuses: List<ProjectStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            ProjectStatusResponse::class.java,
                        ),
                    )

                assertTrue(projectStatuses.isNotEmpty(), "The list of project statuses should not be empty.")

                // Verify the structure of the first element as a sample
                val firstStatus = projectStatuses.first()
                assertTrue(firstStatus.id > 0, "Status ID must be a positive integer.")
                assertTrue(firstStatus.name.isNotBlank(), "Status name must not be blank.")
                assertTrue(firstStatus.code.name.isNotBlank(), "Status code must not be blank.")
            }

            @Test
            fun `getAllProjectStatuses should return all statuses from the database`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("data.integrity.user")
                val expectedCount = projectStatusRepository.count()
                assertTrue(expectedCount > 0, "Database should contain project statuses for this test to be meaningful.")

                // Act
                val result =
                    mockMvc
                        .get(TestUris.PROJECT_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val projectStatuses: List<ProjectStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            ProjectStatusResponse::class.java,
                        ),
                    )

                assertEquals(
                    expectedCount,
                    projectStatuses.size.toLong(),
                    "The number of returned statuses should match the count in the database.",
                )
            }

            @Test
            fun `getAllProjectStatuses should include all essential statuses defined in the enum`() {
                // Arrange
                val (_, userToken) = createTestUserAndGetToken("essential.status.user")
                val expectedStatusCodes = ProjectStatusEnum.entries

                // Act
                val result =
                    mockMvc
                        .get(TestUris.PROJECT_STATUSES) {
                            header(HttpHeaders.AUTHORIZATION, userToken)
                        }.andExpect { status { isOk() } }
                        .andReturn()

                // Assert
                val projectStatuses: List<ProjectStatusResponse> =
                    objectMapper.readValue(
                        result.response.contentAsByteArray,
                        objectMapper.typeFactory.constructCollectionType(
                            List::class.java,
                            ProjectStatusResponse::class.java,
                        ),
                    )

                val returnedCodes = projectStatuses.map { it.code }
                assertTrue(
                    returnedCodes.containsAll(expectedStatusCodes),
                    "The response must contain all essential statuses defined in ProjectStatusEnum.",
                )
            }
        }
    }
