package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.model.LoginUiState
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.network.RetrofitClient
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _userUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val userUiState: StateFlow<LoginUiState> = _userUiState.asStateFlow()
    private val userRepository = UserRepository()

    fun isLoggedIn(): Boolean {
        return _userUiState.value is LoginUiState.Success
    }

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
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _userUiState.value = LoginUiState.Loading
            try {
                // Supongamos que tu repositorio tiene una función googleLogin
                val authResponse = userRepository.googleLogin(idToken)

                if (authResponse != null && authResponse.token.isNotEmpty()) {

                    // ¡ESTA ES LA LÍNEA CLAVE QUE FALTA AQUÍ!
                    RetrofitClient.saveAuthToken(authResponse.token)

                    _userUiState.value = LoginUiState.Success(authResponse)
                } else {
                    _userUiState.value = LoginUiState.Error("Fallo en login de Google")
                }
            } catch (e: Exception) {
                _userUiState.value = LoginUiState.Error("Error: ${e.message}")
            }
        }
    }


    fun login(mail: String, pass: String) {
        viewModelScope.launch {
            _userUiState.value = LoginUiState.Loading
            try {
                val authResponse = userRepository.login(mail, pass)

                // --- AÑADE ESTO PARA VERIFICAR ---
                android.util.Log.d("SEGURIDAD_API", "Respuesta del Backend recibida. Token: ${authResponse?.token}")
                // ---------------------------------

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    RetrofitClient.saveAuthToken(authResponse.token)
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