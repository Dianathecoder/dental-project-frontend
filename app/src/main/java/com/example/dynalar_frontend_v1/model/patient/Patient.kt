package com.example.dynalar_frontend_v1.model.patient

import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.model.odontogram.Odontogram

data class Patient(
    val id: Long? = null,
    val name: String? = null,
    val lastName: String? = null,
    val sex: Sex? = null,
    val email: String? = null,
    val dni: String? = null,
    val socialSecurityNumber: String? = null,
    val phone: String? = null,
    val treatmentConsent: Boolean? = null,
    val anesthesiaConsent: Boolean? = null,
    val billing: String? = null,
    val medicalRecord: MedicalRecord? = null,
    val odontogram: Odontogram? = null,
    val creationDate: String? = null,
    val documents: List<Document>? = emptyList(),
    val appointments: List<Appointment>? = emptyList()
)