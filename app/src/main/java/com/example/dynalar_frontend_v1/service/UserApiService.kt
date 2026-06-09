package com.example.dynalar_frontend_v1.service

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.example.dynalar_frontend_v1.model.user.User
import retrofit2.http.Path

interface UserApiService {
    @GET("user/all")
    suspend fun getAllUsers(): List<User>

    @POST("user/login")
    suspend fun login(@Body user: User): User

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): User?
}