package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.patient.Patient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface OdontogramApiService {

    @GET("odontogram/{id}")
    suspend fun getOdontogramById(@Path("id") id: Long): Response<Odontogram>

    @PUT("odontogram/{id}")
    suspend fun updateOdontogram(@Path("id") id: Long, @Body odontogram: Odontogram): Response<Void>
}