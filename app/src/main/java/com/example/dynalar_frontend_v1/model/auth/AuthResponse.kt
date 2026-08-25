package com.example.dynalar_frontend_v1.model.auth

data class AuthResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val surname: String?,
    val email: String?,
    val role: String
)
