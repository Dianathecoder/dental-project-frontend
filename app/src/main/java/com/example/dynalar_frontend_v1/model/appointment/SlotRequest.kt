package com.example.dynalar_frontend_v1.model.appointment

data class SlotRequest(
    val patientId: Long,
    val treatmentId: Long,
    val startDate: String,
    val endDate: String
)