package com.example.dynalar_frontend_v1.repository

import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.network.RetrofitClient

class UserRepository {
    private val userApiService = RetrofitClient.userApiService;

    suspend fun getAllUsers(): List<User>{
        return userApiService.getAllUsers();
    }

    suspend fun login(user: User): User{
        return userApiService.login(user);
    }
    // Obtener usuario por id
    suspend fun getUserById(userId: Long): User? {
        return userApiService.getUserById(userId)
    }
}