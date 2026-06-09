package com.example.dynalar_frontend_v1.model.odontogram


sealed class OdontogramUiState {
    object Idle : OdontogramUiState()
    object Loading : OdontogramUiState()
    data class Success(val odontogram: Odontogram) : OdontogramUiState()
    data class Error(val message: String) : OdontogramUiState()
}