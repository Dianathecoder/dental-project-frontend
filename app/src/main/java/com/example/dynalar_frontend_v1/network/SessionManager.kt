package com.example.dynalar_frontend_v1.utils

import android.content.Context
import android.content.SharedPreferences



//Sirve para guardar datos de forma persistente en la memoria del teléfono usando una herramienta nativa de Android llamada SharedPreferences
class SessionManager(context: Context) {

    // Archivo de preferencias privado para la app
    private val prefs: SharedPreferences = context.getSharedPreferences("dynalar_prefs", Context.MODE_PRIVATE)

    // Guardar el JWT
    fun saveAuthToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    // Obtener el JWT
    fun fetchAuthToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    // Guardar los roles (Spring Boot envía un array de roles)
    fun saveUserRoles(roles: List<String>) {
        prefs.edit().putStringSet("user_roles", roles.toSet()).apply()
    }

    // Comprobar un rol específico
    fun hasRole(role: String): Boolean {
        val userRoles = prefs.getStringSet("user_roles", setOf()) ?: setOf()
        return userRoles.contains(role)
    }

    // Borrar la sesión (Logout o Token expirado)
    fun clearSession() {
        prefs.edit().clear().apply()
    }


    //comprobación real en la memoria física del teléfono
    fun hasToken(): Boolean {
        return fetchAuthToken() != null
    }
//Recuperar la ID del usuario
    fun saveUserId(id: Long) {
        prefs.edit().putLong("USER_ID", id).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong("USER_ID", -1L)
    }
}