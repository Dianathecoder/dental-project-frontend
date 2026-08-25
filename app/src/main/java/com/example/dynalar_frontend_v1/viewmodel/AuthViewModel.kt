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

    private val repository = AuthRepository()

    private val _authUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val authUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _authUiState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<InterfaceGlobal<Unit>>(InterfaceGlobal.Idle)
    val forgotPasswordState: StateFlow<InterfaceGlobal<Unit>> = _forgotPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            repository.login(email, password)
                .onSuccess { _authUiState.value = InterfaceGlobal.Success(it) }
                .onFailure { e ->
                    _authUiState.value = InterfaceGlobal.Error(e.message ?: "Credencials incorrectes")
                }
        }
    }

    fun register(name: String, surname: String, email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            repository.register(name, surname, email, password)
                .onSuccess { _authUiState.value = InterfaceGlobal.Success(it) }
                .onFailure { e ->
                    _authUiState.value = InterfaceGlobal.Error(e.message ?: "Error al registrar-se")
                }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            repository.googleLogin(idToken)
                .onSuccess { _authUiState.value = InterfaceGlobal.Success(it) }
                .onFailure { e ->
                    _authUiState.value = InterfaceGlobal.Error(e.message ?: "Error amb Google")
                }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = InterfaceGlobal.Loading
            repository.forgotPassword(email)
                .onSuccess { _forgotPasswordState.value = InterfaceGlobal.Success(Unit) }
                .onFailure { e ->
                    _forgotPasswordState.value =
                        InterfaceGlobal.Error(e.message ?: "Error en enviar l'email")
                }
        }
    }

    // Vuelve a Idle: LoginPage lo llama tras cerrar el diálogo de bienvenida de Google
    // para que el estado no quede "atascado" en Success al volver a la pantalla.
    fun resetState() {
        _authUiState.value = InterfaceGlobal.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = InterfaceGlobal.Idle
    }
}