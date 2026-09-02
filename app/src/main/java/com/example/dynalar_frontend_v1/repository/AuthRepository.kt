package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.auth.ForgotPasswordRequest
import com.example.dynalar_frontend_v1.model.auth.GoogleAuthRequest
import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.RegisterRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient

class AuthRepository {

    private val api = RetrofitClient.authApiService


    suspend fun register(name: String, surname: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.registerClinic(RegisterRequest(name, surname, email, password))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Credenciales incorrectas"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleLogin(idToken: String): Result<AuthResponse> {
        return try {
            val response = api.googleLogin(GoogleAuthRequest(idToken))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error con Google: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}