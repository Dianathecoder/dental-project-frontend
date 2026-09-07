package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.InviteUserRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val api = RetrofitClient.userApiService

    private val _inviteState = MutableStateFlow<InterfaceGlobal<Unit>>(InterfaceGlobal.Idle)
    val inviteState: StateFlow<InterfaceGlobal<Unit>> = _inviteState.asStateFlow()

    fun inviteUser(name: String, surname: String, email: String, role: String, dni: String, phone: String, sex: String) {
        viewModelScope.launch {
            _inviteState.value = InterfaceGlobal.Loading
            try {
                val response = api.inviteUser(InviteUserRequest(name, surname, email, role, dni, phone, sex))
                if (response.isSuccessful) {
                    _inviteState.value = InterfaceGlobal.Success(Unit)
                } else {
                    val backendError = response.errorBody()?.string()
                    if (backendError.isNullOrBlank()) {
                        _inviteState.value = InterfaceGlobal.Error(stringResId = R.string.error_unknown_server)
                    } else {
                        _inviteState.value = InterfaceGlobal.Error(message = backendError)
                    }
                }
            } catch (e: Exception) {
                _inviteState.value = InterfaceGlobal.Error(stringResId = R.string.error_connection)
            }
        }
    }

    fun resetInviteState() {
        _inviteState.value = InterfaceGlobal.Idle
    }
}