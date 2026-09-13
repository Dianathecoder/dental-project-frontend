package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.staff.AbsenceRequestDTO
import com.example.dynalar_frontend_v1.model.staff.AbsenceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.AttendanceResponseDTO
import com.example.dynalar_frontend_v1.model.staff.ClockRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StaffControlApiService {

    @GET("staff-control/daily")
    suspend fun getDailyAttendance(@Query("date") date: String): Response<List<AttendanceResponseDTO>>

    @GET("staff-control/monthly-absences")
    suspend fun getMonthlyAbsences(@Query("yearMonth") yearMonth: String): Response<List<AbsenceResponseDTO>>

    @POST("staff-control/clock")
    suspend fun registerClock(@Body request: ClockRequestDTO): Response<Void>

    @POST("staff-control/absence")
    suspend fun createAbsence(@Body request: AbsenceRequestDTO): Response<Void>
}