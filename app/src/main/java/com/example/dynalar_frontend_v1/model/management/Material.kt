package com.example.dynalar_frontend_v1.model.management

data class Material(
    val id: Long? = null,
    val name: String,
    val category: String? = null,
    val minimumStock: Int,
    val availableStock: Int
)