package com.example.dynalar_frontend_v1.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Box
import com.example.dynalar_frontend_v1.repository.BoxRepository
import kotlinx.coroutines.launch

class BoxViewModel : ViewModel() {
    private val repository = BoxRepository()

    var boxesState by mutableStateOf<InterfaceGlobal<List<Box>>>(InterfaceGlobal.Idle)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun getAllBoxes() {
        viewModelScope.launch {
            boxesState = InterfaceGlobal.Loading
            try {
                val boxes = repository.getAllBoxes()
                boxesState = InterfaceGlobal.Success(boxes)
            } catch (e: Exception) {
                boxesState = InterfaceGlobal.Error("Error al carregar boxes: ${e.message}")
            }
        }
    }

    fun createBox(box: Box) {
        viewModelScope.launch {
            try {
                val response = repository.createBox(box)
                if (response.isSuccessful) {
                    getAllBoxes()
                } else {
                    errorMessage = "No s'ha pogut crear el box."
                }
            } catch (e: Exception) {
                errorMessage = "Error de connexió al crear."
            }
        }
    }

    fun deleteBox(number: Long) {
        viewModelScope.launch {
            try {
                val response = repository.deleteBox(number)
                if (response.isSuccessful) {
                    getAllBoxes()
                } else {
                    errorMessage = "El box no ha pogut ser eliminat perquè té una cita ja definida per al futur."
                }
            } catch (e: Exception) {
                errorMessage = "Error de xarxa: El box no s'ha pogut eliminar."
            }
        }
    }
}
