package com.example.dynalar_frontend_v1.repository


import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.network.RetrofitClient

class UserRepository {
    private val userApiService = RetrofitClient.userApiService

    suspend fun getAllUsers(): List<User> {
        return userApiService.getAllUsers()
    }

    // Cambiado para que devuelva AuthResponse
    suspend fun login(email: String, password: String): AuthResponse? {
        val request = LoginRequest(email, password)

        try {
            val response = userApiService.login(request)

            if (response.isSuccessful) {
                val authBody = response.body()
                authBody?.let {
                    RetrofitClient.authToken = it.token
                }
                return authBody
            } else {
                // Evaluamos el código que devuelve el backend
                if (response.code() == 401 || response.code() == 403) {
                    throw Exception("La contraseña o el email son incorrectos")
                } else {
                    throw Exception("Error del servidor (Código ${response.code()})")
                }
            }
        } catch (e: Exception) {
            // Si hay un fallo de red o el servidor está apagado entra aquí
            if (e.message?.contains("incorrectos") == true) {
                throw e
            }
            throw Exception("No se pudo conectar con el servidor. ¿Está encendido?")
        }
    }

    suspend fun getUserById(userId: Long): User? {
        return userApiService.getUserById(userId)
    }
}