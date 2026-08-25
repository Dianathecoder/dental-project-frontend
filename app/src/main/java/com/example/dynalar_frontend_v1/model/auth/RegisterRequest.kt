package com.example.dynalar_frontend_v1.model.auth

data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String
)