package com.example.dynalar_frontend_v1.model.appointment

data class DaySummary(
    val date: String,
    val hasAppointments: Boolean,
    val hasinfeciousPatient: Boolean
)