package com.example.dynalar_frontend_v1.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.Treatment
import com.example.dynalar_frontend_v1.model.management.TreatmentMaterialRequest
import com.example.dynalar_frontend_v1.repository.TreatmentRepository
import kotlinx.coroutines.launch

class TreatmentViewModel(
    private val repository: TreatmentRepository = TreatmentRepository()
) : ViewModel() {

    var uiStateTreatment by mutableStateOf<InterfaceGlobal<List<Treatment>>>(InterfaceGlobal.Idle)
        private set

    var uiStateTreatmentDetail by mutableStateOf<InterfaceGlobal<Treatment>>(InterfaceGlobal.Idle)
        private set

    var treatmentList by mutableStateOf<List<Treatment>>(emptyList())
        private set

    fun getTreatments() {
        viewModelScope.launch {
            uiStateTreatment = InterfaceGlobal.Loading
            try {
                val data = repository.getAllTreatments()

                treatmentList = data // <--- ¡AQUÍ ESTABA EL FALLO PRINCIPAL! Ahora sí se actualiza la lista para la UI

                uiStateTreatment = if (data.isEmpty()) InterfaceGlobal.NotFound
                else InterfaceGlobal.Success(data)
            } catch (e: Exception) {
                uiStateTreatment = InterfaceGlobal.Error(e.message ?: "Error desconocido")
                Log.e("TreatmentViewModel", "Error al obtener tratamientos", e)
            }
        }
    }

    fun getTreatmentById(treatmentId: Long) {
        viewModelScope.launch {
            uiStateTreatmentDetail = InterfaceGlobal.Loading
            try {
                val response = repository.getTreatmentById(treatmentId)
                if (response.isSuccessful) {
                    val data = response.body()
                    uiStateTreatmentDetail = if (data == null) {
                        InterfaceGlobal.NotFound
                    } else {
                        InterfaceGlobal.Success(data)
                    }
                } else {
                    uiStateTreatmentDetail = InterfaceGlobal.Error("Error en servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                uiStateTreatmentDetail = InterfaceGlobal.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun addMaterialToTreatment(treatmentId: Long, materialId: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                val requestBody = TreatmentMaterialRequest(
                    materialId = materialId,
                    quantityRequired = quantity
                )
                repository.addMaterialToTreatment(treatmentId, requestBody)
                getTreatmentById(treatmentId)
            } catch (e: Exception) {
                uiStateTreatmentDetail = InterfaceGlobal.Error(e.message ?: "Error al añadir material")
            }
        }
    }

    fun updateMaterialToTreatment(treatmentId: Long, materialId: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                val requestBody = TreatmentMaterialRequest(
                    materialId = materialId,
                    quantityRequired = quantity
                )
                repository.updateMaterialToTreatment(treatmentId, materialId, requestBody)
                getTreatmentById(treatmentId)
            } catch (e: Exception) {
                Log.e("TreatmentViewModel", "Error al actualizar material", e)
            }
        }
    }

    fun deleteMaterialToTreatment(treatmentId: Long, materialId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.deleteMaterialToTreatment(treatmentId, materialId)
                if (response.isSuccessful) {
                    getTreatmentById(treatmentId)
                } else {
                    uiStateTreatmentDetail = InterfaceGlobal.Error("Error al eliminar material del tratamiento")
                }
            } catch (e: Exception) {
                uiStateTreatmentDetail = InterfaceGlobal.Error(e.message ?: "Error al eliminar material")
            }
        }
    }

    fun deleteTreatment(treatmentId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteTreatment(treatmentId) // Usamos repository
                if (response.isSuccessful) {
                    getTreatments()
                    onSuccess()
                } else {
                    Log.e("TreatmentViewModel", "Error al eliminar: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TreatmentViewModel", "Exception en deleteTreatment", e)
            }
        }
    }

    fun createTreatment(treatment: Treatment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createTreatment(treatment)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    Log.e("TreatmentViewModel", "Error al crear tratamiento: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TreatmentViewModel", "Exception en createTreatment", e)
            }
        }
    }
}