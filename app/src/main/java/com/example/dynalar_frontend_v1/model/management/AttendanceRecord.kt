package com.example.dynalar_frontend_v1.model.management

import com.example.dynalar_frontend_v1.model.user.User

data class AttendanceRecord(
    val id: Long?,
    val user: User?,
    val clockInTime: String?,
    val clockOutTime: String?
)
