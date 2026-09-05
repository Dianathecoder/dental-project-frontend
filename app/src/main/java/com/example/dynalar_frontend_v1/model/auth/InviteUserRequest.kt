package com.example.dynalar_frontend_v1.model.auth

data class InviteUserRequest(
    val name: String,
    val surname: String,
    val email: String,
    val role: String,
    val dni: String,
    val phone: String,
    val sex: String
)