package com.example.dynalar_frontend_v1.model

import com.example.dynalar_frontend_v1.model.user.User

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}