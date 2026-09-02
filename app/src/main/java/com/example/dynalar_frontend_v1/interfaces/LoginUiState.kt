package com.example.dynalar_frontend_v1.interfaces

import com.example.dynalar_frontend_v1.model.auth.AuthResponse

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val authResponse: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}