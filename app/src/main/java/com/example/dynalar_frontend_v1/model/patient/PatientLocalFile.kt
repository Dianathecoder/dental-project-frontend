package com.example.dynalar_frontend_v1.model.patient

data class PatientLocalFile(
    val id: Long,
    val patientId: Long,
    val displayName: String,
    val uri: String,
    val mimeType: String,
    val createdAtMillis: Long
)

