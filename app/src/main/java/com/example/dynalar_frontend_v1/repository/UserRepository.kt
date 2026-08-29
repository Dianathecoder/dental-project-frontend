package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.auth.LoginRequest
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.auth.GoogleAuthRequest
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.network.RetrofitClient

class UserRepository {
    // Si pasaste el login a AuthApiService, cambia esto a RetrofitClient.authApiService
    // Si lo dejaste en userApiService, esto está perfecto:
    private val userApiService = RetrofitClient.userApiService

    suspend fun getAllUsers(): List<User> {
        val response = userApiService.getAllUsers()
        if (response.isSuccessful) {
            // Extraemos el cuerpo de la respuesta (la lista). Si es nulo, devolvemos lista vacía
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error del servidor al obtener usuarios: ${response.code()}")
        }
    }
    suspend fun googleLogin(idToken: String): AuthResponse? {
        val request = GoogleAuthRequest(idToken)

        try {
            val response = userApiService.googleLogin(request)

            if (response.isSuccessful) {
                return response.body()
            } else {
                throw Exception("Error de Google Login (Código ${response.code()})")
            }
        } catch (e: Exception) {
            throw Exception("No se pudo conectar con el servidor: ${e.message}")
        }
    }


    suspend fun login(email: String, password: String): AuthResponse? {
        val request = LoginRequest(email, password)

        try {
            val response = userApiService.login(request)

            if (response.isSuccessful) {
                // El guardado del token (RetrofitClient.saveAuthToken) ya lo hace el UserViewModel
                return response.body()
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
        val response = userApiService.getUserById(userId)
        if (response.isSuccessful) {
            // Extraemos el cuerpo de la respuesta (el objeto User)
            return response.body()
        } else {
            throw Exception("Error del servidor al obtener el usuario: ${response.code()}")
        }
    }
}