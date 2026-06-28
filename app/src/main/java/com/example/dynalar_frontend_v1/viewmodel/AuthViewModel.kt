package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val authUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _authUiState.asStateFlow()

    private val authRepository = AuthRepository()

    fun register(name: String, surname: String, email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            try {
                val response = authRepository.register(name, surname, email, password)
                _authUiState.value = InterfaceGlobal.Success(response)
            } catch (e: Exception) {
                _authUiState.value = InterfaceGlobal.Error("Error al registrarse. Comprova les dades.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            try {
                val response = authRepository.login(email, password)
                _authUiState.value = InterfaceGlobal.Success(response)
            } catch (e: Exception) {
                _authUiState.value = InterfaceGlobal.Error("Credencials incorrectes")
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            try {
                val response = authRepository.googleLogin(idToken)
                _authUiState.value = InterfaceGlobal.Success(response)
            } catch (e: Exception) {
                _authUiState.value = InterfaceGlobal.Error("Error al iniciar sessió amb Google")
            }
        }
    }

    fun resetState() { _authUiState.value = InterfaceGlobal.Idle }
}