package com.example.dynalar_frontend_v1.model.auth


data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)