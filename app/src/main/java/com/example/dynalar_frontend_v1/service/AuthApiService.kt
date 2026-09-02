package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.auth.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST




interface AuthApiService {

    // 1. Registro Orgánico Público (Crea la clínica y da ROLE_ADMIN)
    @POST("api/auth/register")
    suspend fun registerClinic(@Body request: RegisterRequest): Response<AuthResponse>

    // 2. Login normal (Para Admins, Doctores, Auxiliares y Pacientes)
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // 3. Login con Google (El backend asignará ADMIN o PATIENT automáticamente)
    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    // 4. Activar cuenta de paciente (Viene desde el enlace del email)
    @POST("api/auth/activate-patient")
    suspend fun activatePatient(@Body request: ResetPasswordRequest): Response<MessageResponse>

    // 5. Solicitar email de recuperación
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    // 6. Restablecer contraseña olvidada
    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
}