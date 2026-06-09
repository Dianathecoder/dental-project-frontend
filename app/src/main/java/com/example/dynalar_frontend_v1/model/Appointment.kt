package com.example.dynalar_frontend_v1.model

import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.model.user.Dentist

data class Appointment(
    val id: Long? = null,
    val treatment: Treatment? = null,
    val dentist: Dentist? = null,
    val patient: Patient? = null,
    val box: Box? = null,
    val odontogram: Odontogram? = null,
    val creationDate: String? = null,
    val reason: String? = null,
    val durationMinutes: Int? = null,
    val startTime: String? = null, // Ej: "10:30:00"
    val endTime: String? = null    // Ej: "11:00:00"
)