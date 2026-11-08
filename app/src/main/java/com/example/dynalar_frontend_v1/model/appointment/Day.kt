package com.example.dynalar_frontend_v1.model.appointment

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val isCurrentMonth: Boolean = true
)