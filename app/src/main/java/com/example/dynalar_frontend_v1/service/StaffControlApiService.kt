package com.example.dynalar_frontend_v1.service

// Importaciones explícitas de los DTOs
import com.example.dynalar_frontend_v1.model.staff.AbsenceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.AttendanceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.ClockRequestDTO

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StaffControlApiService {
    @GET("api/staff-control/daily")
    suspend fun getDailyAttendance(@Query("date") date: String): Response<List<AttendanceResponseDTO>>

    @GET("api/staff-control/absences/monthly")
    suspend fun getMonthlyAbsences(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<AbsenceResponseDTO>>

    @POST("api/staff-control/clock")
    suspend fun registerClock(@Body request: ClockRequestDTO): Response<Unit>

}