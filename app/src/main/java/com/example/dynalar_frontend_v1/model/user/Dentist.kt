package com.example.dynalar_frontend_v1.model.user

import com.example.dynalar_frontend_v1.model.management.Treatment

data class Dentist(
    val mondayMorning: Boolean? = null,
    val mondayAfternoon: Boolean? = null,
    val tuesdayMorning: Boolean? = null,
    val tuesdayAfternoon: Boolean? = null,
    val wednesdayMorning: Boolean? = null,
    val wednesdayAfternoon: Boolean? = null,
    val thursdayMorning: Boolean? = null,
    val thursdayAfternoon: Boolean? = null,
    val fridayMorning: Boolean? = null,
    val fridayAfternoon: Boolean? = null,
    val treatments: List<Treatment>? = emptyList()
) : User()