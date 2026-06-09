package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.network.RetrofitClient
import retrofit2.Response

class OdontogramRepository {
    private val odontogramApiService = RetrofitClient.odontogramApiService
    suspend fun getOdontogramById(id: Long): Odontogram {
        val response = odontogramApiService.getOdontogramById(id)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Error ${response.code()}")
        }
    }

    suspend fun updateOdontogram(id: Long,  odontogram: Odontogram): Response<Void> {
        return odontogramApiService.updateOdontogram(id, odontogram)
    }
}