package com.tianqueal.flowjet.backend.utils.constants

object PublicEndpoints {
    val SWAGGER = setOf("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")

    private val AUTH_PATHS =
        setOf(
            ApiPaths.LOGIN,
            ApiPaths.REGISTER,
            ApiPaths.VERIFY_EMAIL,
            "${ApiPaths.PASSWORD_RESET}/request",
            "${ApiPaths.PASSWORD_RESET}/confirm",
        )

    private val NON_AUTH_PATHS =
        setOf(
            "${ApiPaths.PROJECTS}/*${ApiPaths.MEMBERS}/accept-invitation",
        )

    fun auth(version: String) = AUTH_PATHS.map { "$version${ApiPaths.AUTH}$it" }

    fun nonAuthPaths(version: String) = NON_AUTH_PATHS.map { "$version$it" }

    fun forVersion(version: String): Set<String> = SWAGGER + auth(version) + nonAuthPaths(version)
}
