package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.model.LoginUiState
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

    private val _userUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val userUiState: StateFlow<LoginUiState> = _userUiState.asStateFlow()
    private val userRepository = UserRepository()

    fun getAllUsers() {
        viewModelScope.launch {
            _userUiState.value = LoginUiState.Loading
            try {
                val users = userRepository.getAllUsers()

            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.value = LoginUiState.Error("Error al obtener usuarios")
            }
        }
    }

    fun getUserById(userId: Long) {
        viewModelScope.launch {
            _userUiState.value = LoginUiState.Loading
            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    // Adaptamos el User a un AuthResponse solucionando los nulos con ?: ""
                    val mappedResponse = AuthResponse(
                        token = "",
                        userId = user.id ?: 0L,
                        name = user.name ?: "",
                        surname = user.surname ?: "",
                        email = user.email ?: "",
                        role = user.role ?: "USER"
                    )
                    _userUiState.value = LoginUiState.Success(mappedResponse)
                } else {
                    _userUiState.value = LoginUiState.Error("Usuario no encontrado")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.value = LoginUiState.Error("Error al cargar datos")
            }
        }
    }

    fun login(mail: String, pass: String) {
        viewModelScope.launch {
            _userUiState.value = LoginUiState.Loading
            try {
                // userRepository.login devuelve un AuthResponse
                val authResponse = userRepository.login(mail, pass)

                if (authResponse != null) {
                    // Simplemente pasamos el authResponse directo al estado
                    _userUiState.value = LoginUiState.Success(authResponse)
                } else {
                    _userUiState.value = LoginUiState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.value = LoginUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }
    fun setLoggedInUser(authResponse: AuthResponse) {
        _userUiState.value = LoginUiState.Success(authResponse)
    }
}

