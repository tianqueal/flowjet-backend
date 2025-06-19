package com.tianqueal.flowjet.backend.utils.constants

object PublicEndpoints {
  val SWAGGER = setOf("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")

  val AUTH = setOf(ApiPaths.V1 + "/auth/**")

  val TEST = setOf(ApiPaths.V1 + "/test/**")

  val ALL = SWAGGER + AUTH + TEST
}
