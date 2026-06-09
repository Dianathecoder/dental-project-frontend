package com.example.dynalar_frontend_v1.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.model.user.TreatmentMaterialRequest
import com.example.dynalar_frontend_v1.repository.TreatmentRepository
import kotlinx.coroutines.launch

class TreatmentViewModel(
    private val repository: TreatmentRepository = TreatmentRepository()
) : ViewModel() {

    var uiStateTreatment by mutableStateOf<InterfaceGlobal<List<Treatment>>>(InterfaceGlobal.Idle)
        private set

    var uiStateTreatmentDetail by mutableStateOf<InterfaceGlobal<Treatment>>(InterfaceGlobal.Idle)
        private set

    fun getTreatments() {
        viewModelScope.launch {
            uiStateTreatment = InterfaceGlobal.Loading
            try {
                val data = repository.getAllTreatments()
                uiStateTreatment = if (data.isEmpty()) InterfaceGlobal.NotFound
                else InterfaceGlobal.Success(data)
            } catch (e: Exception) {
                uiStateTreatment = InterfaceGlobal.Error(e.message ?: "Error desconocido")
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
                repository.deleteMaterialToTreatment(treatmentId, materialId)
                getTreatmentById(treatmentId)
            } catch (e: Exception) {
                Log.e("TreatmentViewModel", "Error al eliminar material", e)
            }
        }
    }
}
