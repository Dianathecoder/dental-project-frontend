package com.example.dynalar_frontend_v1.ui.components

import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.CountryInfo
import com.example.dynalar_frontend_v1.model.patient.Sex

// Lista completa de imágenes disponibles
val patientImages = listOf(
    R.drawable.usuario1,  // Índice 0
    R.drawable.usuario2,  // Índice 1
    R.drawable.usuario3,  // Índice 2
    R.drawable.usuario4,  // Índice 3
    R.drawable.usuario5,  // Índice 4
    R.drawable.usuario6,  // Índice 5
    R.drawable.usuario7,  // Índice 6
    R.drawable.usuario8,  // Índice 7
    R.drawable.usuario9,  // Índice 8
    R.drawable.usuario10, // Índice 9
    R.drawable.usuario11, // Índice 10
    R.drawable.usuario12, // Índice 11
    R.drawable.usuario13, // Índice 12
    R.drawable.usuario20 // Índice 13
)


fun getPatientImage(patientId: Long?, sex: Sex?): Int {
    val id = patientId ?: 0L

    return when (sex) {
        Sex.FEMALE -> {
            val femaleOptions = listOf(
                R.drawable.usuario1, R.drawable.usuario4, R.drawable.usuario5,
                R.drawable.usuario6, R.drawable.usuario7, R.drawable.usuario8,
                R.drawable.usuario11
            )
            femaleOptions[(id % femaleOptions.size).toInt()]
        }

        else -> {
            val maleOptions = listOf(
                R.drawable.usuario2, R.drawable.usuario3, R.drawable.usuario9,
                R.drawable.usuario10, R.drawable.usuario12, R.drawable.usuario13,
                R.drawable.usuario20
            )
            maleOptions[(id % maleOptions.size).toInt()]
        }
    }
}



val countriesList = listOf(
    CountryInfo("+34", "Espanya", "🇪🇸"),
    CountryInfo("+376", "Andorra", "🇦🇩"),
    CountryInfo("+33", "França", "🇫🇷"),
    CountryInfo("+351", "Portugal", "🇵🇹"),
    CountryInfo("+39", "Itàlia", "🇮🇹"),
    CountryInfo("+49", "Alemanya", "🇩🇪"),
    CountryInfo("+44", "Regne Unit", "🇬🇧"),
    CountryInfo("+212", "Marroc", "🇲🇦"),
    CountryInfo("+40", "Romania", "🇷🇴"),
    CountryInfo("+359", "Bulgària", "🇧🇬"),
    CountryInfo("+380", "Ucraïna", "🇺🇦"),
    CountryInfo("+48", "Polònia", "🇵🇱"),
    CountryInfo("+1", "EUA / Canadà", "🇺🇸"),
    CountryInfo("+52", "Mèxic", "🇲🇽"),
    CountryInfo("+57", "Colòmbia", "🇨🇴"),
    CountryInfo("+54", "Argentina", "🇦🇷"),
    CountryInfo("+58", "Veneçuela", "🇻🇪"),
    CountryInfo("+51", "Perú", "🇵🇪"),
    CountryInfo("+56", "Xile", "🇨🇱"),
    CountryInfo("+593", "Equador", "🇪🇨"),
    CountryInfo("+502", "Guatemala", "🇬🇹"),
    CountryInfo("+53", "Cuba", "🇨🇺"),
    CountryInfo("+504", "Hondures", "🇭🇳"),
    CountryInfo("+591", "Bolívia", "🇧🇴"),
    CountryInfo("+55", "Brasil", "🇧🇷"),
    CountryInfo("+507", "Panamà", "🇵🇦"),
    CountryInfo("+506", "Costa Rica", "🇨🇷"),
    CountryInfo("+598", "Uruguai", "🇺🇾"),
    CountryInfo("+595", "Paraguai", "🇵🇾"),
    CountryInfo("+86", "Xina", "🇨🇳"),
    CountryInfo("+92", "Pakistan", "🇵🇰"),
    CountryInfo("+221", "Senegal", "🇸🇳"),
    CountryInfo("+240", "Guinea Eq.", "🇬🇶"),
    CountryInfo("+7", "Rússia", "🇷🇺"),
    CountryInfo("+41", "Suïssa", "🇨🇭"),
    CountryInfo("+32", "Bèlgica", "🇧🇪"),
    CountryInfo("+31", "Països Baixos", "🇳🇱")
).sortedBy { if (it.code == "+34") "" else it.name }