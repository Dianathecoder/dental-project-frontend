package com.example.dynalar_frontend_v1.service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import com.example.dynalar_frontend_v1.model.management.AttendanceRecord // El import correcto

interface AttendanceApiService {
    // Fichar entrada
    @POST("api/attendance/clock-in")
    suspend fun clockIn(): Response<Unit>

    // Fichar salida
    @POST("api/attendance/clock-out")
    suspend fun clockOut(): Response<Unit>

    @GET("api/attendance/all")
    suspend fun getAllAttendance(): Response<List<AttendanceRecord>>
}