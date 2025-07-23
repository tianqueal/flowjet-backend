package com.tianqueal.flowjet.backend.services

interface PasswordResetService {
    fun generatePasswordResetToken(email: String): String

    fun sendPasswordResetEmail(email: String)

    fun verifyTokenAndResetPassword(
        token: String,
        newPassword: String,
    )
}
