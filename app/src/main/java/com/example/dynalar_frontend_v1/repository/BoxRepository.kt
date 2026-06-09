package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.Box
import com.example.dynalar_frontend_v1.network.RetrofitClient
import retrofit2.Response

class BoxRepository {
    private val boxApiService = RetrofitClient.boxApiService

    suspend fun getAllBoxes(): List<Box> {
        return boxApiService.getAllBoxes()
    }

    suspend fun createBox(box: Box): Response<Box> {
        return boxApiService.createBox(box)
    }

    suspend fun deleteBox(number: Long): Response<Unit> {
        return boxApiService.deleteBox(number)
    }
}
