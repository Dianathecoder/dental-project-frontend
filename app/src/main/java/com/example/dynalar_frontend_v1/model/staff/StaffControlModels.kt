package com.example.dynalar_frontend_v1.model.staff

import java.time.LocalDate
import java.time.LocalTime

data class AttendanceResponseDTO(
    val id: Long,
    val staffName: String,
    val role: String,
    val roles: List<String>,
    val sex: String,
    val date: String,
    val checkInTime: String?,
    val checkOutTime: String? 
)

data class AbsenceResponseDTO(
    val id: Long,
    val title: String,
    val staffName: String?,
    val type: String,
    val startDate: String,
    val endDate: String
)

enum class AttendanceStatusType { ON_TIME, LATE_CLOCKED, ABSENT_RED, PENDING }
enum class AbsenceType { VACATION, HOLIDAY, SICK_LEAVE }
enum class StaffRoleFilter { ALL, OWNER, ADMIN, DOCTOR, AUXILIAR }
enum class StaffClockFilter { ALL, CLOCKED_IN, NOT_CLOCKED, LATE_WARNING }

data class AttendanceEntry(
    val id: Long,
    val staffName: String,
    val role: String,
    val roles: List<String>,
    val sex: String,
    val date: LocalDate,
    val checkInTime: LocalTime?,
    val checkOutTime: LocalTime?, // <-- AÑADIDO
    val expectedTime: LocalTime = LocalTime.of(8, 0)
) {
    val status: AttendanceStatusType
        get() {
            if (checkInTime != null) {
                return if (checkInTime.isAfter(expectedTime.plusHours(1))) AttendanceStatusType.LATE_CLOCKED
                else AttendanceStatusType.ON_TIME
            }
            val now = LocalTime.now()
            return if (now.isAfter(expectedTime.plusHours(1))) AttendanceStatusType.ABSENT_RED
            else AttendanceStatusType.PENDING
        }
}

data class AbsenceEvent(
    val id: Long,
    val title: String,
    val staffName: String?,
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate
)