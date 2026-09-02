package com.example.dynalar_frontend_v1.service

import retrofit2.Response
import retrofit2.http.POST

interface AttendanceApiService {
    // Fichar entrada
    @POST("api/attendance/clock-in")
    suspend fun clockIn(): Response<Unit>

    // Fichar salida
    @POST("api/attendance/clock-out")
    suspend fun clockOut(): Response<Unit>

}