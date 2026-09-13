package com.example.dynalar_frontend_v1.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
// IMPORTACIONES EXPLÍCITAS PARA EVITAR ERRORES
import com.example.dynalar_frontend_v1.model.staff.AbsenceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.AttendanceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.ClockRequestDTO
import com.example.dynalar_frontend_v1.network.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class StaffControlViewModel : ViewModel() {

    var uiStateAttendance by mutableStateOf<InterfaceGlobal<List<AttendanceResponseDTO>>>(InterfaceGlobal.Idle)
        private set

    var uiStateAbsences by mutableStateOf<InterfaceGlobal<List<AbsenceResponseDTO>>>(InterfaceGlobal.Idle)
        private set

    fun fetchDailyAttendance(date: LocalDate) {
        viewModelScope.launch {
            uiStateAttendance = InterfaceGlobal.Loading
            try {
                val response = RetrofitClient.staffControlApiService.getDailyAttendance(date.toString())
                if (response.isSuccessful && response.body() != null) {
                    uiStateAttendance = InterfaceGlobal.Success(response.body()!!)
                } else {
                    uiStateAttendance = InterfaceGlobal.Error("Error al cargar los datos")
                }
            } catch (e: Exception) {
                uiStateAttendance = InterfaceGlobal.Error(e.message ?: "Error de red")
            }
        }
    }

    fun fetchMonthlyAbsences(yearMonth: YearMonth) {
        viewModelScope.launch {
            uiStateAbsences = InterfaceGlobal.Loading
            try {
                val response = RetrofitClient.staffControlApiService.getMonthlyAbsences(yearMonth.year, yearMonth.monthValue)
                if (response.isSuccessful && response.body() != null) {
                    uiStateAbsences = InterfaceGlobal.Success(response.body()!!)
                } else {
                    uiStateAbsences = InterfaceGlobal.Error("Error al cargar ausencias")
                }
            } catch (e: Exception) {
                uiStateAbsences = InterfaceGlobal.Error(e.message ?: "Error de red")
            }
        }
    }


    fun registerClock(type: String, time: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Creamos la petición. Si time es null, el backend usará la hora actual.
                val request = ClockRequestDTO(type = type, time = time)
                val response = RetrofitClient.staffControlApiService.registerClock(request)

                if (response.isSuccessful) {
                    onResult(true)
                    // Recargar los datos de hoy para que la UI se actualice
                    fetchDailyAttendance(LocalDate.now())
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
