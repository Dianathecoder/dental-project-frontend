package com.example.dynalar_frontend_v1.repository


import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.model.user.TreatmentMaterialRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient
import retrofit2.Response

class TreatmentRepository {

    private val treatmentApiService = RetrofitClient.treatmentApiService

    suspend fun getAllTreatments(): List<Treatment> {
        return treatmentApiService.getAllTreatments()
    }

    suspend fun getTreatmentById(id: Long): Response<Treatment> {
        return treatmentApiService.getTreatmentById(id)
    }

    suspend fun createTreatment(treatment: Treatment): Response<Treatment> {
        return treatmentApiService.createTreatment(treatment)
    }

    suspend fun updateTreatment(treatment: Treatment): Response<Treatment> {
        return treatmentApiService.updateTreatment(treatment)
    }

    suspend fun deleteTreatment(id: Long): Response<Unit> {
        return treatmentApiService.deleteTreatment(id)
    }

    suspend fun addMaterialToTreatment(treatmentId: Long, treatmentMaterialRequest: TreatmentMaterialRequest): Response<Unit> {
        return treatmentApiService.addMaterialToTreatment(treatmentId, treatmentMaterialRequest)
    }
    suspend fun updateMaterialToTreatment(treatmentId: Long, materialId: Long, treatmentMaterialRequest: TreatmentMaterialRequest): Response<Unit> {
        return treatmentApiService.updateMaterialToTreatment(treatmentId, materialId, treatmentMaterialRequest)
    }
    suspend fun deleteMaterialToTreatment(treatmentId: Long, materialId: Long): Response<Unit> {
        return treatmentApiService.deleteMaterialToTreatment(treatmentId, materialId)
    }
}