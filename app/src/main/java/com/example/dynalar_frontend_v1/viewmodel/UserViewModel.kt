package com.example.dynalar_frontend_v1.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException

class UserViewModel : ViewModel() {

    // Estado para el Login (Guarda AuthResponse)
    private val _userUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val userUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _userUiState.asStateFlow()

    // NUEVO: Estado separado para el Perfil del Usuario (Guarda User)
    private val _profileUiState = MutableStateFlow<InterfaceGlobal<User>>(InterfaceGlobal.Idle)
    val profileUiState: StateFlow<InterfaceGlobal<User>> = _profileUiState.asStateFlow()

    private val userRepository = UserRepository()

    fun login(mail: String, pass: String) {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val authResponse = userRepository.login(mail, pass)

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    _userUiState.value = InterfaceGlobal.Success(authResponse)
                } else {
                    _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_invalid_credentials)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is ConnectException, is UnknownHostException -> {
                        _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_server_connection)
                    }
                    else -> {
                        _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_invalid_credentials)
                    }
                }
            }
        }
    }

    fun googleLogin(idToken: String) {
        // 1. Ver si la interfaz realmente llega a llamar a la función
        android.util.Log.d("VIEWMODEL_GOOGLE", "1. Entrando a googleLogin en el ViewModel")

        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                android.util.Log.d("VIEWMODEL_GOOGLE", "2. Llamando a Retrofit (userRepository)...")

                val authResponse = userRepository.googleLogin(idToken)

                android.util.Log.d("VIEWMODEL_GOOGLE", "3. Respuesta de Retrofit recibida: $authResponse")

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    _userUiState.value = InterfaceGlobal.Success(authResponse)
                } else {
                    _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_invalid_credentials)
                }
            } catch (e: Exception) {
                // 4. Si Retrofit falla (por permisos HTTP o red), caerá aquí
                android.util.Log.e("VIEWMODEL_GOOGLE", "4. ERROR en Retrofit: ${e.message}", e)
                _userUiState.value = InterfaceGlobal.Error(message = e.message)
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _profileUiState.value = InterfaceGlobal.Loading // Usamos el nuevo estado
            try {
                val userProfile = userRepository.getProfile()

                if (userProfile != null) {
                    _profileUiState.value = InterfaceGlobal.Success(userProfile) // Guardamos el User
                } else {
                    _profileUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_user_not_found)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is ConnectException, is UnknownHostException -> {
                        _profileUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_server_connection)
                    }
                    else -> {
                        _profileUiState.value = InterfaceGlobal.Error(message = e.message ?: "Error al carregar el perfil")
                    }
                }
            }
        }
    }

    fun setLocalError(@StringRes stringResId: Int) {
        _userUiState.value = InterfaceGlobal.Error(stringResId = stringResId)
    }

    fun setIdle() {
        _userUiState.value = InterfaceGlobal.Idle
        _profileUiState.value = InterfaceGlobal.Idle
    }
}