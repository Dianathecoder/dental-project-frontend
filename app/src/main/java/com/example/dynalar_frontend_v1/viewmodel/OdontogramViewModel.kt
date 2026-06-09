package com.example.dynalar_frontend_v1.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.odontogram.OdontogramUiState
import com.example.dynalar_frontend_v1.model.odontogram.DentalProcess
import com.example.dynalar_frontend_v1.repository.OdontogramRepository
import com.example.dynalar_frontend_v1.repository.DentalProcessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OdontogramViewModel(): ViewModel(){
    private val _odontogramUiState = MutableStateFlow<OdontogramUiState>(OdontogramUiState.Idle)
    val odontogramUiState: StateFlow<OdontogramUiState> = _odontogramUiState.asStateFlow()

    private val _dentalProcessesState = MutableStateFlow<List<DentalProcess>>(emptyList())

    val dentalProcessesState: StateFlow<List<DentalProcess>> = _dentalProcessesState.asStateFlow()
    private val odontogramRepository = OdontogramRepository()

    private val dentalProcessRepository = DentalProcessRepository()

    var isDeleteHintShown by mutableStateOf(false)
    fun getOdontogramById(id: Long) {
        viewModelScope.launch {
            _odontogramUiState.value = OdontogramUiState.Loading
            try {
                val odontogram = odontogramRepository.getOdontogramById(id)
                _odontogramUiState.value = OdontogramUiState.Success(odontogram)
            } catch (e: Exception) {
                e.printStackTrace()
                _odontogramUiState.value = OdontogramUiState.Error("Error al obtener el odontograma")
            }
        }
    }

    fun updateOdontogram(id: Long, odontogram: Odontogram){
        _odontogramUiState.value = OdontogramUiState.Success(odontogram)
        viewModelScope.launch {
        try {
            odontogramRepository.updateOdontogram(id, odontogram)
        }
        catch (e: Exception) {
            e.printStackTrace()
            }
        }
    }

    fun getAllDentalProcesses(){
        viewModelScope.launch {
            try {
                _dentalProcessesState.value = dentalProcessRepository.getAllDentalProcesses()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}