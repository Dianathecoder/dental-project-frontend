package com.example.dynalar_frontend_v1.model.appointment

data class AutoAssignRequest(
    val patientId: Long,
    val treatmentId: Long,
    val requestedTime: String,
    val reason: String
)