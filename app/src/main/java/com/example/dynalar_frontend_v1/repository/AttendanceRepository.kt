package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.network.RetrofitClient

class AttendanceRepository {
    private val api = RetrofitClient.attendanceApiService

    suspend fun clockIn(): Result<Unit> {
        return try {
            val response = api.clockIn()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al fitxar entrada"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clockOut(): Result<Unit> {
        return try {
            val response = api.clockOut()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al fitxar sortida"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}