package com.tianqueal.flowjet.backend.utils.constants

object PublicEndpoints {
  val SWAGGER = setOf("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")

  private val AUTH_PATHS = listOf(
    ApiPaths.LOGIN,
    ApiPaths.REGISTER,
    ApiPaths.VERIFY_EMAIL
  )

  fun auth(version: String) = AUTH_PATHS.map { "$version${ApiPaths.AUTH}$it" }.toSet()

  fun forVersion(version: String): Set<String> =
    SWAGGER + auth(version)
}
