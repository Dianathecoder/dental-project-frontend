package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.auth.GoogleAuthRequest
import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.auth.ChangePasswordRequest
import com.example.dynalar_frontend_v1.model.auth.InviteUserRequest
import com.example.dynalar_frontend_v1.model.auth.MessageResponse
import com.example.dynalar_frontend_v1.model.user.User
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    // Rutas sincronizadas con el controlador /user de Spring Boot:
    @GET("user/all")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<User>

    @PUT("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @POST("user/invite-user")
    suspend fun inviteUser(@Body request: InviteUserRequest): Response<MessageResponse>

    @GET("user/me")
    suspend fun getProfile(): Response<User>
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") userId: Long): Response<Unit>
}