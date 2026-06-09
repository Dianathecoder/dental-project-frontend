package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.Box
import retrofit2.Response
import retrofit2.http.*

interface BoxApiService {
    @GET("box/all")
    suspend fun getAllBoxes(): List<Box>

    @POST("box")
    suspend fun createBox(@Body box: Box): Response<Box>

    @DELETE("box/{number}")
    suspend fun deleteBox(@Path("number") number: Long): Response<Unit>
}
