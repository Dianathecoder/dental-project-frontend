package com.example.dynalar_frontend_v1.model.user

import com.example.dynalar_frontend_v1.model.management.Treatment

data class Dentist(
    val id: Long? = null,
    val user: User? = null,

    // --- LUNES ---
    val mondayMorningActive: Boolean? = null,
    val mondayMorningStart: String? = null,
    val mondayMorningEnd: String? = null,
    val mondayAfternoonActive: Boolean? = null,
    val mondayAfternoonStart: String? = null,
    val mondayAfternoonEnd: String? = null,
    val mondayEveningActive: Boolean? = null,
    val mondayEveningStart: String? = null,
    val mondayEveningEnd: String? = null,

    // --- MARTES ---
    val tuesdayMorningActive: Boolean? = null,
    val tuesdayMorningStart: String? = null,
    val tuesdayMorningEnd: String? = null,
    val tuesdayAfternoonActive: Boolean? = null,
    val tuesdayAfternoonStart: String? = null,
    val tuesdayAfternoonEnd: String? = null,
    val tuesdayEveningActive: Boolean? = null,
    val tuesdayEveningStart: String? = null,
    val tuesdayEveningEnd: String? = null,

    // --- MIÉRCOLES ---
    val wednesdayMorningActive: Boolean? = null,
    val wednesdayMorningStart: String? = null,
    val wednesdayMorningEnd: String? = null,
    val wednesdayAfternoonActive: Boolean? = null,
    val wednesdayAfternoonStart: String? = null,
    val wednesdayAfternoonEnd: String? = null,
    val wednesdayEveningActive: Boolean? = null,
    val wednesdayEveningStart: String? = null,
    val wednesdayEveningEnd: String? = null,

    // --- JUEVES ---
    val thursdayMorningActive: Boolean? = null,
    val thursdayMorningStart: String? = null,
    val thursdayMorningEnd: String? = null,
    val thursdayAfternoonActive: Boolean? = null,
    val thursdayAfternoonStart: String? = null,
    val thursdayAfternoonEnd: String? = null,
    val thursdayEveningActive: Boolean? = null,
    val thursdayEveningStart: String? = null,
    val thursdayEveningEnd: String? = null,

    // --- VIERNES ---
    val fridayMorningActive: Boolean? = null,
    val fridayMorningStart: String? = null,
    val fridayMorningEnd: String? = null,
    val fridayAfternoonActive: Boolean? = null,
    val fridayAfternoonStart: String? = null,
    val fridayAfternoonEnd: String? = null,
    val fridayEveningActive: Boolean? = null,
    val fridayEveningStart: String? = null,
    val fridayEveningEnd: String? = null,

    val treatments: List<Treatment>? = emptyList()
) {
    val userId: Long?
        get() = user?.id

    val name: String?
        get() = user?.name

    val surname: String?
        get() = user?.surname

    val email: String?
        get() = user?.email

    val phone: String?
        get() = user?.phone

    val roles: List<String>
        get() = user?.roles ?: emptyList()
}