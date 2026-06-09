package com.example.dynalar_frontend_v1.model.user

import com.example.dynalar_frontend_v1.model.Treatment

// 1. Hemos quitado la palabra 'override' de los primeros campos
// 2. Hemos quitado el ': User(...)' del final
/*data class Dentist( // De recuerdo :)
    val id: Long? = null,
    val name: String,
    val surname: String,
    val email: String,
    val role: String? = null,
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
    val treatments: List<Treatment>? = null
)
*/

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