package com.example.dynalar_frontend_v1.viewmodel

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dynalar_frontend_v1.network.RetrofitClient

class UserViewModel : ViewModel() {

    // Estado para el Login (Guarda AuthResponse)
    private val _userUiState = MutableStateFlow<InterfaceGlobal<AuthResponse>>(InterfaceGlobal.Idle)
    val userUiState: StateFlow<InterfaceGlobal<AuthResponse>> = _userUiState.asStateFlow()

    // NUEVO: Estado separado para el Perfil del Usuario (Guarda User)
    private val _profileUiState = MutableStateFlow<InterfaceGlobal<User>>(InterfaceGlobal.Idle)
    val profileUiState: StateFlow<InterfaceGlobal<User>> = _profileUiState.asStateFlow()

    private val userRepository = UserRepository()

    var staffList by mutableStateOf<List<User>>(emptyList())
        private set

    // 2. Variable para guardar el trabajador seleccionado
    var selectedUser by mutableStateOf<User?>(null)
        private set

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
    fun deleteUser(userId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApiService.deleteUser(userId)
                if (response.isSuccessful) {
                    getAllStaff() // Recarga la lista de empleados
                    onSuccess()
                } else {
                    Log.e("UserViewModel", "Error al eliminar usuario: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Excepción al eliminar usuario: ${e.message}")
            }
        }
    }
    fun getAllStaff() {
        viewModelScope.launch {
            try {
                // userRepository debe llamar a tu UserApiService.getAllUsers()
                // Si aún no lo tienes en UserRepository, puedes llamar a la API directamente
                val response = RetrofitClient.userApiService.getAllUsers()
                if (response.isSuccessful) {
                    staffList = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUserById(id: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApiService.getUserById(id)
                if (response.isSuccessful) {
                    selectedUser = response.body()
                } else {
                    // Fallback de seguridad: buscarlo en la lista si falla la petición
                    selectedUser = staffList.find { it.id == id }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                selectedUser = staffList.find { it.id == id }
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