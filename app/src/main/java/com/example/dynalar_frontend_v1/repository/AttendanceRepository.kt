package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.network.RetrofitClient
import com.example.dynalar_frontend_v1.model.management.AttendanceRecord

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

    suspend fun getAllAttendance(): Result<List<AttendanceRecord>> {
        return try {
            val response = api.getAllAttendance()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al carregar els fitxatges"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}