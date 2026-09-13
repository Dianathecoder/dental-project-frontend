package com.example.dynalar_frontend_v1.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.staff.AbsenceRequestDTO
// IMPORTACIONES EXPLÍCITAS PARA EVITAR ERRORES
import com.example.dynalar_frontend_v1.model.staff.AbsenceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.AttendanceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.ClockRequestDTO
import com.example.dynalar_frontend_v1.network.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class StaffControlViewModel : ViewModel() {

    var uiStateAttendance by mutableStateOf<InterfaceGlobal<List<AttendanceResponseDTO>>>(
        InterfaceGlobal.Idle
    )
        private set

    var uiStateAbsences by mutableStateOf<InterfaceGlobal<List<AbsenceResponseDTO>>>(InterfaceGlobal.Idle)
        private set

    fun fetchDailyAttendance(date: LocalDate) {
        viewModelScope.launch {
            uiStateAttendance = InterfaceGlobal.Loading
            try {
                val response =
                    RetrofitClient.staffControlApiService.getDailyAttendance(date.toString())
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
                val response = RetrofitClient.staffControlApiService.getMonthlyAbsences(
                    yearMonth.toString()
                )
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
                    fetchDailyAttendance(LocalDate.now())
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun registerAbsence(
        title: String,
        type: String,
        startDate: LocalDate,
        endDate: LocalDate,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // USAR AbsenceRequestDTO EN LUGAR DE ClockRequestDTO
                val dto = AbsenceRequestDTO(
                    title = title,
                    type = type,
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )

                val response = RetrofitClient.staffControlApiService.createAbsence(dto)
                if (response.isSuccessful) {
                    fetchMonthlyAbsences(YearMonth.from(startDate))
                    onSuccess()
                } else {
                    Log.e("StaffControlVM", "Error registrante ausencia: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("StaffControlVM", "Excepción al registrar ausencia: ${e.message}")
            }
        }
    }
}
