package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.odontogram.DentalProcess
import com.example.dynalar_frontend_v1.network.RetrofitClient

class DentalProcessRepository {

    private val dentalProcessApiService = RetrofitClient.dentalProcessApiService

    suspend fun getAllDentalProcesses(): List<DentalProcess> {
        val response = dentalProcessApiService.getAllDentalProcesses()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Error ${response.code()}")
        }
    }
}