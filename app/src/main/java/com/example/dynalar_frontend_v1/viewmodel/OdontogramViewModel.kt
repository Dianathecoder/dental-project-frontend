package com.example.dynalar_frontend_v1.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.interfaces.OdontogramUiState
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
    private val _dentalProcessesError = MutableStateFlow(false)
    val dentalProcessesError: StateFlow<Boolean> = _dentalProcessesError.asStateFlow()
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

    fun updateOdontogram(id: Long, odontogram: Odontogram, onResult: (Boolean) -> Unit = {}){
        _odontogramUiState.value = OdontogramUiState.Success(odontogram)
        viewModelScope.launch {
            try {
                val response = odontogramRepository.updateOdontogram(id, odontogram)
                if (response.isSuccessful) {
                    val savedOdontogram = odontogramRepository.getOdontogramById(id)
                    _odontogramUiState.value = OdontogramUiState.Success(savedOdontogram)
                    onResult(true)
                } else {
                    android.util.Log.e("ODONTOGRAMA", "No se guardaron los cambios: HTTP ${response.code()}")
                    val currentOdontogram = odontogramRepository.getOdontogramById(id)
                    _odontogramUiState.value = OdontogramUiState.Success(currentOdontogram)
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runCatching { odontogramRepository.getOdontogramById(id) }
                    .onSuccess { _odontogramUiState.value = OdontogramUiState.Success(it) }
                onResult(false)
            }
        }
    }

    fun getAllDentalProcesses(){
        viewModelScope.launch {
            _dentalProcessesError.value = false
            try {
                _dentalProcessesState.value = dentalProcessRepository.getAllDentalProcesses()
            } catch (e: Exception) {
                e.printStackTrace()
                _dentalProcessesError.value = true
            }
        }
    }
}
