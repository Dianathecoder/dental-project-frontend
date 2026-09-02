package com.example.dynalar_frontend_v1.model.auth

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
