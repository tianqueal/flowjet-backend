package com.tianqueal.flowjet.backend.utils.functions

import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder

object AuthFunctions {
  fun getAuthenticatedUsername(): String {
    val auth = SecurityContextHolder.getContext().authentication
    if (auth == null || !auth.isAuthenticated || auth.principal == null) {
      throw InsufficientAuthenticationException(
        "User is not authenticated or principal is null"
      )
    }
    return auth.name
  }
}
