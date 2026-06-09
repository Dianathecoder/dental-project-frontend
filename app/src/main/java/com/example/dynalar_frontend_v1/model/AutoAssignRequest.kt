package com.example.dynalar_frontend_v1.model

import java.time.LocalDateTime

data class AutoAssignRequest(
    val patientId: Long,
    val treatmentId: Long,
    val requestedTime: String,
    val reason: String
)
