package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.odontogram.DentalProcess
import retrofit2.Response
import retrofit2.http.GET

interface DentalProcessApiService {
    @GET("process/index")
    suspend fun getAllDentalProcesses(): Response<List<DentalProcess>>
}