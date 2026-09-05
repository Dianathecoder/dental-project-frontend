package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.repository.AuthRepository
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val authUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _authUiState.asStateFlow()
    private val userRepository = UserRepository()
    private val _forgotPasswordState = MutableStateFlow<InterfaceGlobal<Unit>>(InterfaceGlobal.Idle)
    val forgotPasswordState: StateFlow<InterfaceGlobal<Unit>> = _forgotPasswordState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<InterfaceGlobal<String>>(InterfaceGlobal.Idle)
    val changePasswordState: StateFlow<InterfaceGlobal<String>> = _changePasswordState.asStateFlow()

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
        android.util.Log.d("AUTH_VIEWMODEL", "1. Entrando a googleLogin en AuthViewModel")

        viewModelScope.launch {
            _authUiState.value = InterfaceGlobal.Loading
            try {
                android.util.Log.d("AUTH_VIEWMODEL", "2. Llamando a Retrofit...")

                val authResponse = userRepository.googleLogin(idToken)

                android.util.Log.d("AUTH_VIEWMODEL", "3. Respuesta recibida: $authResponse")

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    _authUiState.value = InterfaceGlobal.Success(authResponse)
                } else {
                    _authUiState.value = InterfaceGlobal.Error(message = "Token vacío o credenciales inválidas")
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_VIEWMODEL", "4. ERROR en Retrofit (Red/Conexión): ${e.message}", e)
                _authUiState.value = InterfaceGlobal.Error(message = e.message ?: "Error de connexió")
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

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = InterfaceGlobal.Loading
            repository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _changePasswordState.value = InterfaceGlobal.Success("Contrasenya canviada correctament")
                }
                .onFailure { e ->
                    _changePasswordState.value = InterfaceGlobal.Error(e.message ?: "Error en canviar la contrasenya")
                }
        }
    }

    fun resetState() {
        _authUiState.value = InterfaceGlobal.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = InterfaceGlobal.Idle
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = InterfaceGlobal.Idle
    }
}