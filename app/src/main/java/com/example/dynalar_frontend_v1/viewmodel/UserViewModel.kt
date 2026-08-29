package com.example.dynalar_frontend_v1.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.network.RetrofitClient
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException

class UserViewModel : ViewModel() {

    // CAMBIO CLAVE: Se usa InterfaceGlobal<AuthResponse> en lugar de LoginUiState
    private val _userUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val userUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _userUiState.asStateFlow()

    private val userRepository = UserRepository()

    fun isLoggedIn(): Boolean {
        return _userUiState.value is InterfaceGlobal.Success
    }

    fun getAllUsers() {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val users = userRepository.getAllUsers()
            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.value = InterfaceGlobal.Error(message = "Error al obtener usuarios")
            }
        }
    }

    fun getUserById(userId: Long) {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
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
                    _userUiState.value = InterfaceGlobal.Success(mappedResponse)
                } else {
                    _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_user_not_found)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_generic)
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val authResponse = userRepository.googleLogin(idToken)

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    RetrofitClient.saveAuthToken(authResponse.token)
                    _userUiState.value = InterfaceGlobal.Success(authResponse)
                } else {
                    _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_invalid_credentials)
                }
            } catch (e: Exception) {
                _userUiState.value = InterfaceGlobal.Error(message = e.message)
            }
        }
    }

    fun login(mail: String, pass: String) {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val authResponse = userRepository.login(mail, pass)

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    RetrofitClient.saveAuthToken(authResponse.token)
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

    fun setLocalError(@StringRes stringResId: Int) {
        _userUiState.value = InterfaceGlobal.Error(stringResId = stringResId)
    }

    fun setLoggedInUser(authResponse: AuthResponse) {
        _userUiState.value = InterfaceGlobal.Success(authResponse)
    }
}