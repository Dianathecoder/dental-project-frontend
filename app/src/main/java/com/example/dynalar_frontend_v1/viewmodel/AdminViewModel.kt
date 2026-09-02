package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.InviteUserRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val api = RetrofitClient.userApiService // O adminApiService

    private val _inviteState = MutableStateFlow<InterfaceGlobal<String>>(InterfaceGlobal.Idle)
    val inviteState: StateFlow<InterfaceGlobal<String>> = _inviteState.asStateFlow()

    fun inviteUser(name: String, surname: String, email: String, role: String) {
        viewModelScope.launch {
            _inviteState.value = InterfaceGlobal.Loading
            try {
                val response = api.inviteUser(InviteUserRequest(name, surname, email, role))
                if (response.isSuccessful) {
                    _inviteState.value = InterfaceGlobal.Success("Invitació enviada correctament")
                } else {
                    _inviteState.value = InterfaceGlobal.Error(message = "Error al convidar usuari")
                }
            } catch (e: Exception) {
                _inviteState.value = InterfaceGlobal.Error(message = e.message)
            }
        }
    }

    fun resetInviteState() {
        _inviteState.value = InterfaceGlobal.Idle
    }
}