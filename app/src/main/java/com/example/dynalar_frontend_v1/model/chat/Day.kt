package com.example.dynalar_frontend_v1.model.chat

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val isCurrentMonth: Boolean = true
)
