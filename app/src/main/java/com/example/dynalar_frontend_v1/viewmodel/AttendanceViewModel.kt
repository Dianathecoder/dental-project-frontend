package com.example.dynalar_frontend_v1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.AttendanceRecord // Usa tu ruta correcta de modelo
import com.example.dynalar_frontend_v1.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AttendanceViewModel : ViewModel() {
    private val repository = AttendanceRepository()

    private val _attendanceState = MutableStateFlow<InterfaceGlobal<String>>(InterfaceGlobal.Idle)
    val attendanceState: StateFlow<InterfaceGlobal<String>> = _attendanceState.asStateFlow()

    private val _attendanceListState = MutableStateFlow<InterfaceGlobal<List<AttendanceRecord>>>(InterfaceGlobal.Idle)
    val attendanceListState: StateFlow<InterfaceGlobal<List<AttendanceRecord>>> = _attendanceListState.asStateFlow()

    fun clockIn() {
        viewModelScope.launch {
            _attendanceState.value = InterfaceGlobal.Loading
            val result = repository.clockIn()
            if (result.isSuccess) _attendanceState.value = InterfaceGlobal.Success("Entrada registrada")
            else _attendanceState.value = InterfaceGlobal.Error(message = result.exceptionOrNull()?.message)
        }
    }

    fun clockOut() {
        viewModelScope.launch {
            _attendanceState.value = InterfaceGlobal.Loading
            val result = repository.clockOut()
            if (result.isSuccess) _attendanceState.value = InterfaceGlobal.Success("Sortida registrada")
            else _attendanceState.value = InterfaceGlobal.Error(message = result.exceptionOrNull()?.message)
        }
    }

    fun fetchAllAttendance() {
        viewModelScope.launch {
            _attendanceListState.value = InterfaceGlobal.Loading
            val result = repository.getAllAttendance()
            if (result.isSuccess) {
                _attendanceListState.value = InterfaceGlobal.Success(result.getOrDefault(emptyList()))
            } else {
                _attendanceListState.value = InterfaceGlobal.Error(message = result.exceptionOrNull()?.message)
            }
        }
    }

    fun resetState() {
        _attendanceState.value = InterfaceGlobal.Idle
    }
}