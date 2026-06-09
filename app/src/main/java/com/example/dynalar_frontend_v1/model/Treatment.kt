package com.example.dynalar_frontend_v1.model

import com.example.dynalar_frontend_v1.model.user.Dentist

data class Treatment(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val durationMinutes: Int? = null,
    val materials: List<TreatmentMaterial>? = emptyList(),
    val dentists: List<Dentist>? = emptyList()
)