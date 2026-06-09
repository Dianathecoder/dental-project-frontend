package com.example.dynalar_frontend_v1.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.repository.MaterialRepository
import kotlinx.coroutines.launch

class MaterialViewModel : ViewModel() {
    private val repository = MaterialRepository()

    var materialsState by mutableStateOf<InterfaceGlobal<List<Material>>>(InterfaceGlobal.Idle)
        private set

    var materialDetailState by mutableStateOf<InterfaceGlobal<Material>>(InterfaceGlobal.Idle)
        private set

    fun getAllMaterials() {
        viewModelScope.launch {
            materialsState = InterfaceGlobal.Loading
            try {
                val materials = repository.getAllMaterials()
                materialsState = InterfaceGlobal.Success(materials)
            } catch (e: Exception) {
                materialsState = InterfaceGlobal.Error("Error al cargar materiales: ${e.message}")
            }
        }
    }


    fun getMaterialById(id: Long) {
        viewModelScope.launch {
            materialDetailState = InterfaceGlobal.Loading
            try {
                val material = repository.getMaterialById(id)
                materialDetailState = InterfaceGlobal.Success(material)
            } catch (e: Exception) {
                materialDetailState =
                    InterfaceGlobal.Error("Error al obtener detalle: ${e.message}")
            }
        }
    }

    fun createMaterial(material: Material) {
        viewModelScope.launch {
            try {
                repository.createMaterial(material)
                getAllMaterials()
            } catch (e: Exception) {
                materialsState = InterfaceGlobal.Error("Error al crear material: ${e.message}")
            }
        }
    }

    fun updateMaterial(id: Long, material: Material) {
        viewModelScope.launch {
            try {
                repository.updateMaterial(id, material)
                getAllMaterials()
            } catch (e: Exception) {
                materialsState = InterfaceGlobal.Error("Error al actualizar material: ${e.message}")
            }
        }
    }

    fun deleteMaterial(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteMaterial(id)
                getAllMaterials()
            } catch (e: Exception) {
                materialsState = InterfaceGlobal.Error("Error al eliminar material: ${e.message}")
            }
        }
    }

    fun decreaseStock(id: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                repository.decreaseStock(id, quantity)
                getMaterialById(id)
            } catch (e: Exception) {
                Log.e("MaterialViewModel", "Error al actualizar stock: ${e.message}")
            }
        }
    }

    fun increaseStock(id: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                repository.increaseStock(id, quantity)
                getMaterialById(id)
            } catch (e: Exception) {
                Log.e("MaterialViewModel", "Error al actualizar stock: ${e.message}")
            }
        }
    }
}
