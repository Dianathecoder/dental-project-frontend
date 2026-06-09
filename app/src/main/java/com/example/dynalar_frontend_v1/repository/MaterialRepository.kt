package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.network.RetrofitClient

class MaterialRepository {
    private val materialApiService = RetrofitClient.materialApiService

    suspend fun getAllMaterials(): List<Material> {
        return materialApiService.getAllMaterials()
    }

    suspend fun getMaterialById(id: Long): Material {
        return materialApiService.getMaterialById(id)
    }

    suspend fun createMaterial(material: Material): Material {
        return materialApiService.createMaterial(material)
    }

    suspend fun updateMaterial(id: Long, material: Material): Material {
        return materialApiService.updateMaterial(id, material)
    }

    suspend fun deleteMaterial(id: Long) {
        val response = materialApiService.deleteMaterial(id)
        if (!response.isSuccessful) {
            throw Exception("Error al eliminar. Código HTTP: ${response.code()}")
        }
    }

    suspend fun increaseStock(id: Long, quantity: Int){
        materialApiService.increaseStock(id, quantity)
    }

    suspend fun decreaseStock(id: Long, quantity: Int){
        materialApiService.decreaseStock(id, quantity)
    }
}
