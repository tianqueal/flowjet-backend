package com.tianqueal.flowjet.backend.controllers.v1

import com.fasterxml.jackson.core.type.TypeReference
import com.tianqueal.flowjet.backend.domain.dto.v1.common.PageResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.CreateProjectRequest
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectListResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.ProjectResponse
import com.tianqueal.flowjet.backend.domain.dto.v1.project.UpdateProjectRequest
import com.tianqueal.flowjet.backend.repositories.ProjectRepository
import com.tianqueal.flowjet.backend.services.ProjectService
import com.tianqueal.flowjet.backend.utils.constants.TestUris
import com.tianqueal.flowjet.backend.utils.functions.TestDataUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@DisplayName("Project Controller Integration Tests")
class ProjectControllerIntegrationTests
    @Autowired
    constructor(
        private val projectRepository: ProjectRepository,
        private val projectService: ProjectService,
    ) : AbstractAuthenticatableControllerTest() {
        @BeforeEach
        fun setUp() {
            projectRepository.deleteAll()
        }

        @Test
        fun `getAllProjects with valid token should return OK and paginated projects`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            listOf("Project Alpha", "Project Beta").forEach {
                projectService.create(
                    userId = user.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest(it),
                )
            }

            // Act
            val result =
                mockMvc
                    .get(TestUris.PROJECTS_URI) {
                        header(HttpHeaders.AUTHORIZATION, token)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val response =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    object : TypeReference<PageResponse<ProjectListResponse>>() {},
                )
            val projectNames = response.content.map { it.name }

            assertEquals(2, response.content.size)
            assertEquals(2L, response.totalElements)
            assertThat(projectNames).contains("Project Alpha", "Project Beta")
        }

        @Test
        fun `getAllProjects without token should return Unauthorized`() {
            // Arrange - No token provided

            // Act & Assert
            mockMvc
                .get(TestUris.PROJECTS_URI)
                .andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `getAllProjects should return only owned projects`() {
            // Arrange
            val (user1, user2, user3) = createMultipleTestUsersAndGetTokens(3)

            // Create project
            listOf(user1, user2).forEach {
                projectService.create(
                    userId = it.first.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest("${it.first.username} Project"),
                )
            }

            // Act
            val result =
                mockMvc
                    .get(TestUris.PROJECTS_URI) {
                        header(HttpHeaders.AUTHORIZATION, user3.second)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert - member should not see the owned project
            val response =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    object : TypeReference<PageResponse<ProjectResponse>>() {},
                )
            assertEquals(0L, response.totalElements)
        }

        @Test
        fun `getAllProjects with name filter should return filtered results`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            listOf("Alpha Project", "Beta Project", "Gamma Plan").forEach {
                projectService.create(
                    userId = user.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest(it),
                )
            }

            // Act
            val result =
                mockMvc
                    .get(TestUris.PROJECTS_URI) {
                        header(HttpHeaders.AUTHORIZATION, token)
                        queryParam("name", "Project")
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val response =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    object : TypeReference<PageResponse<ProjectListResponse>>() {},
                )
            val projectNames = response.content.map { it.name }

            assertEquals(2, response.content.size)
            assertEquals(2L, response.totalElements)
            assertThat(projectNames).contains("Alpha Project", "Beta Project")
            assertFalse(projectNames.contains("Gamma Plan"))
        }

        @Test
        fun `getAllProjects with pagination should respect page parameters`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            // Create 5 projects
            repeat(5) { index ->
                projectService.create(
                    userId = user.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest("Project $index"),
                )
            }

            // Act
            val result =
                mockMvc
                    .get(TestUris.PROJECTS_URI) {
                        header(HttpHeaders.AUTHORIZATION, token)
                        queryParam("p", "0")
                        queryParam("s", "2")
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val response =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    object : TypeReference<PageResponse<ProjectListResponse>>() {},
                )
            assertEquals(2, response.content.size)
            assertEquals(5L, response.totalElements)
        }

        @Test
        fun `getProjectById with valid ID and permission should return project`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            val project =
                projectService.create(
                    userId = user.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest(),
                )

            // Act
            val result =
                mockMvc
                    .get("${TestUris.PROJECTS_URI}/${project.id}") {
                        header(HttpHeaders.AUTHORIZATION, token)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val projectResponse =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    ProjectResponse::class.java,
                )
            assertEquals(project.id, projectResponse.id)
            assertEquals(project.name, projectResponse.name)
        }

        @Test
        fun `getProjectById with invalid ID should return NotFound`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()
            val nonExistentId = 99999L

            // Act & Assert
            mockMvc
                .get("${TestUris.PROJECTS_URI}/$nonExistentId") {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `getProjectById without permission should return Forbidden`() {
            // Arrange
            val user1 = createTestUser("test.user1")
            val (_, token) = createTestUserAndGetToken("test.user2")

            val project =
                projectService.create(
                    userId = user1.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest(),
                )

            // Act & Assert
            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `getProjectById without token should return Unauthorized`() {
            // Arrange
            val user = createTestUser()
            val project =
                projectService.create(
                    userId = user.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            // Act & Assert
            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}")
                .andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `createProject with valid data should return Created with Location header`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()
            val createRequest = TestDataUtils.createTestProjectRequest()

            // Act
            val result =
                mockMvc
                    .post(TestUris.PROJECTS_URI) {
                        header(HttpHeaders.AUTHORIZATION, token)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(createRequest)
                    }.andExpect { status { isCreated() } }
                    .andReturn()

            // Assert
            val locationHeader = result.response.getHeader(HttpHeaders.LOCATION)
            assertThat(locationHeader).isNotNull
            assertThat(locationHeader).contains(TestUris.PROJECTS_URI)

            val projectResponse =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    ProjectResponse::class.java,
                )
            assertEquals(createRequest.name, projectResponse.name)
            assertEquals(createRequest.description, projectResponse.description)
        }

        @Test
        fun `createProject with invalid data should return BadRequest`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()

            val invalidRequest =
                CreateProjectRequest(
                    name = "", // Invalid: empty name
                    description = null,
                    statusId = -1, // Invalid: negative ID
                )

            // Act & Assert
            mockMvc
                .post(TestUris.PROJECTS_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(invalidRequest)
                }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `createProject without token should return Unauthorized`() {
            // Arrange
            val createRequest = TestDataUtils.createTestProjectRequest()

            // Act & Assert
            mockMvc
                .post(TestUris.PROJECTS_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(createRequest)
                }.andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `createProject with malformed JSON should return BadRequest`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()
            val malformedJson = """{"name": "Test", "invalid": }"""

            // Act & Assert
            mockMvc
                .post(TestUris.PROJECTS_URI) {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = malformedJson
                }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `updateProject with valid data and ownership should return updated project`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            val project =
                projectService.create(
                    userId = user.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest("Original Name"),
                )

            val updateRequest =
                UpdateProjectRequest(
                    name = "Updated Name",
                    description = "Updated Description",
                    statusId = 1,
                )

            // Act
            val result =
                mockMvc
                    .put("${TestUris.PROJECTS_URI}/${project.id}") {
                        header(HttpHeaders.AUTHORIZATION, token)
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsBytes(updateRequest)
                    }.andExpect { status { isOk() } }
                    .andReturn()

            // Assert
            val updatedProject =
                objectMapper.readValue(
                    result.response.contentAsByteArray,
                    ProjectResponse::class.java,
                )
            assertEquals(updateRequest.name, updatedProject.name)
            assertEquals(updateRequest.description, updatedProject.description)
        }

        @Test
        fun `updateProject with invalid ID should return NotFound`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()
            val nonExistentId = 99999L

            val updateRequest =
                UpdateProjectRequest(
                    name = "Updated Name",
                    description = "Updated Description",
                    statusId = 1,
                )

            // Act & Assert
            mockMvc
                .put("${TestUris.PROJECTS_URI}/$nonExistentId") {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(updateRequest)
                }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `updateProject without ownership should return Forbidden`() {
            // Arrange
            val (user1, user2) = createMultipleTestUsersAndGetTokens(2)

            val project =
                projectService.create(
                    userId = user1.first.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            val updateRequest =
                UpdateProjectRequest(
                    name = "Malicious Update",
                    description = "Should not work",
                    statusId = 1,
                )

            // Act & Assert
            mockMvc
                .put("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, user2.second)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(updateRequest)
                }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `updateProject with invalid data should return BadRequest`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            val project =
                projectService.create(
                    userId = user.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            val invalidUpdateRequest =
                UpdateProjectRequest(
                    name = "", // Invalid: empty name
                    description = "Valid description",
                    statusId = -1, // Invalid: negative ID
                )

            // Act & Assert
            mockMvc
                .put("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, token)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(invalidUpdateRequest)
                }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `deleteProject with valid ID and ownership should return NoContent`() {
            // Arrange
            val (user, token) = createTestUserAndGetToken()

            val project =
                projectService.create(
                    userId = user.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            // Act
            mockMvc
                .delete("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isNoContent() } }

            // Assert - Verify project is actually deleted
            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `deleteProject with invalid ID should return NotFound`() {
            // Arrange
            val (_, token) = createTestUserAndGetToken()
            val nonExistentId = 99999L

            // Act & Assert
            mockMvc
                .delete("${TestUris.PROJECTS_URI}/$nonExistentId") {
                    header(HttpHeaders.AUTHORIZATION, token)
                }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `deleteProject without ownership should return Forbidden`() {
            // Arrange
            val (user1, user2) = createMultipleTestUsersAndGetTokens(2)

            val project =
                projectService.create(
                    userId = user1.first.id,
                    createProjectRequest = TestDataUtils.createTestProjectRequest(),
                )

            // Act & Assert
            mockMvc
                .delete("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, user2.second)
                }.andExpect { status { isForbidden() } }

            // Assert - Verify project still exists
            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, user1.second)
                }.andExpect { status { isOk() } }
        }

        @Test
        fun `deleteProject without token should return Unauthorized`() {
            // Arrange
            val user = createTestUser()

            val project =
                projectService.create(
                    userId = user.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            // Act & Assert
            mockMvc
                .delete("${TestUris.PROJECTS_URI}/${project.id}")
                .andExpect { status { isUnauthorized() } }
        }

        // ================================
        // Cross-cutting Concerns & Edge Cases
        // ================================

        @Test
        fun `all endpoints should require authentication`() {
            // Arrange
            val createRequest = TestDataUtils.createTestProjectRequest()
            val updateRequest = UpdateProjectRequest("name", "desc", 1)

            // Act & Assert - All endpoints should return 401 without token
            mockMvc.get(TestUris.PROJECTS_URI).andExpect { status { isUnauthorized() } }
            mockMvc.get("${TestUris.PROJECTS_URI}/1").andExpect { status { isUnauthorized() } }
            mockMvc
                .post(TestUris.PROJECTS_URI) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(createRequest)
                }.andExpect { status { isUnauthorized() } }
            mockMvc
                .put("${TestUris.PROJECTS_URI}/1") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsBytes(updateRequest)
                }.andExpect { status { isUnauthorized() } }
            mockMvc.delete("${TestUris.PROJECTS_URI}/1").andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `all endpoints should reject invalid JWT tokens`() {
            // Arrange
            val invalidToken = "Bearer invalid-jwt-token-123"

            // Act & Assert
            mockMvc
                .get(TestUris.PROJECTS_URI) {
                    header(HttpHeaders.AUTHORIZATION, invalidToken)
                }.andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `endpoints should handle concurrent access properly`() {
            // Arrange
            val (user1, user2) = createMultipleTestUsersAndGetTokens(2)

            val project =
                projectService.create(
                    userId = user1.first.id,
                    TestDataUtils.createTestProjectRequest(),
                )

            // Act & Assert - User1 (owner) can access, User2 cannot
            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, user1.second)
                }.andExpect { status { isOk() } }

            mockMvc
                .get("${TestUris.PROJECTS_URI}/${project.id}") {
                    header(HttpHeaders.AUTHORIZATION, user2.second)
                }.andExpect { status { isForbidden() } }
        }
    }
