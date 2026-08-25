package com.example.dynalar_frontend_v1.model

import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    // Asegúrate de que reciba la respuesta del backend
    data class Success(val authResponse: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}