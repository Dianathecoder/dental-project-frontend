package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.auth.GoogleAuthRequest
import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.auth.ChangePasswordRequest
import com.example.dynalar_frontend_v1.model.auth.InviteUserRequest
import com.example.dynalar_frontend_v1.model.auth.MessageResponse
import com.example.dynalar_frontend_v1.model.staff.dentist.DentistAvailabilityDTO
import com.example.dynalar_frontend_v1.model.user.User
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    @GET("user/all")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<User>

    @PUT("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @POST("user/invite-user")
    suspend fun inviteUser(@Body request: InviteUserRequest): Response<MessageResponse>

    @PUT("user/update/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: InviteUserRequest
    ): Response<Unit>

    @GET("user/me")
    suspend fun getProfile(): Response<User>

    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") userId: Long): Response<Unit>

    @POST("user/update-avatar")
    suspend fun updateAvatar(@Body body: Map<String, String>): retrofit2.Response<Any>

    @GET("dentist/user/{userId}/availability")
    suspend fun getDentistAvailability(@Path("userId") userId: Long): Response<DentistAvailabilityDTO>

    @PUT("dentist/user/{userId}/availability")
    suspend fun updateDentistAvailability(
        @Path("userId") userId: Long,
        @Body dto: DentistAvailabilityDTO
    ): Response<Void>
}