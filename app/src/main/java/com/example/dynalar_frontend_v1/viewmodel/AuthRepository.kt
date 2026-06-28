package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.auth.ForgotPasswordRequest
import com.example.dynalar_frontend_v1.model.auth.GoogleAuthRequest
import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.RegisterRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient

class AuthRepository {

    private val authApiService = RetrofitClient.authApiService

    suspend fun register(name: String, surname: String, email: String, password: String): AuthResponse {
        val response = authApiService.register(RegisterRequest(name, surname, email, password))
        RetrofitClient.authToken = response.token  // guarda el JWT para peticiones siguientes
        return response
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val response = authApiService.login(LoginRequest(email, password))
        RetrofitClient.authToken = response.token
        return response
    }

    suspend fun googleLogin(idToken: String): AuthResponse {
        val response = authApiService.googleLogin(GoogleAuthRequest(idToken))
        RetrofitClient.authToken = response.token
        return response
    }

    suspend fun forgotPassword(email: String) {
        authApiService.forgotPassword(ForgotPasswordRequest(email))
    }
}