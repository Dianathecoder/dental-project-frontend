package com.example.dynalar_frontend_v1.model.staff

data class ClockRequestDTO(
    val type: String, // "IN" o "OUT"
    val time: String? // "HH:mm" o null
)