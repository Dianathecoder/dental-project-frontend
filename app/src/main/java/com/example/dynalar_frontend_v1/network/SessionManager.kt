package com.example.dynalar_frontend_v1.utils

import android.content.Context
import android.content.SharedPreferences

// Sirve para guardar datos de forma persistente en la memoria del teléfono usando una herramienta nativa de Android llamada SharedPreferences
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

    // Guardar los roles de forma segura convirtiendo cualquier objeto/lista a texto en mayúsculas
    fun saveUserRoles(roles: Collection<*>?) {
        val rolesSet = roles?.map { it.toString().uppercase() }?.toSet() ?: emptySet()
        prefs.edit().putStringSet("user_roles", rolesSet).apply()
    }

    // Comprobar un rol específico (busca la palabra clave, así si es "ROLE_ADMIN", detecta "ADMIN")
    fun hasRole(role: String): Boolean {
        val userRoles = prefs.getStringSet("user_roles", emptySet()) ?: emptySet()
        return userRoles.any { it.contains(role.uppercase()) }
    }

    // Borrar la sesión (Logout o Token expirado)
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // Comprobación real en la memoria física del teléfono
    fun hasToken(): Boolean {
        return fetchAuthToken() != null
    }

    // Recuperar la ID del usuario
    fun saveUserId(id: Long) {
        prefs.edit().putLong("USER_ID", id).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong("USER_ID", -1L)
    }
}