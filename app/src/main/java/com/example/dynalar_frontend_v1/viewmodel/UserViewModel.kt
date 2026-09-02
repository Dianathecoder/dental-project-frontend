package com.example.dynalar_frontend_v1.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException

class UserViewModel : ViewModel() {

    private val _userUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val userUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _userUiState.asStateFlow()

    private val userRepository = UserRepository()

    fun login(mail: String, pass: String) {
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val authResponse = userRepository.login(mail, pass)

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    // Solo emitimos el éxito. La Vista se encarga de guardar la sesión.
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
        viewModelScope.launch {
            _userUiState.value = InterfaceGlobal.Loading
            try {
                val authResponse = userRepository.googleLogin(idToken)

                if (authResponse != null && authResponse.token.isNotEmpty()) {
                    _userUiState.value = InterfaceGlobal.Success(authResponse)
                } else {
                    _userUiState.value = InterfaceGlobal.Error(stringResId = R.string.error_invalid_credentials)
                }
            } catch (e: Exception) {
                _userUiState.value = InterfaceGlobal.Error(message = e.message)
            }
        }
    }

    fun setLocalError(@StringRes stringResId: Int) {
        _userUiState.value = InterfaceGlobal.Error(stringResId = stringResId)
    }

    fun setIdle() {
        _userUiState.value = InterfaceGlobal.Idle
    }
}