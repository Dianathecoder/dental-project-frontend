package com.example.dynalar_frontend_v1.service


import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse> // Usamos AuthResponse que ya la tienes y contiene tu token

    @GET("api/user/all")
    suspend fun getAllUsers(): List<User>

    @GET("api/user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): User?
}