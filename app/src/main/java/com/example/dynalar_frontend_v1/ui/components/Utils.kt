package com.example.dynalar_frontend_v1.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.common.CountryInfo
import com.example.dynalar_frontend_v1.model.patient.Sex
import java.util.Locale

val patientImages = listOf(
    R.drawable.usuario1, R.drawable.usuario2, R.drawable.usuario3,
    R.drawable.usuario4, R.drawable.usuario5, R.drawable.usuario6,
    R.drawable.usuario7, R.drawable.usuario8, R.drawable.usuario9,
    R.drawable.usuario10, R.drawable.usuario11, R.drawable.usuario12,
    R.drawable.usuario13, R.drawable.usuario20
)

val doctorImages = listOf(
    R.drawable.doctor1, R.drawable.doctor2, R.drawable.doctor3,
    R.drawable.doctor4, R.drawable.doctor5, R.drawable.doctor6,
    R.drawable.doctor7, R.drawable.doctor8, R.drawable.doctor9,
    R.drawable.doctorincog
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
        Sex.MALE -> {
            val maleOptions = listOf(
                R.drawable.usuario2, R.drawable.usuario3, R.drawable.usuario9,
                R.drawable.usuario10, R.drawable.usuario12, R.drawable.usuario13,
                R.drawable.usuario20
            )
            maleOptions[(id % maleOptions.size).toInt()]
        }
        Sex.OTHER, null -> {
            R.drawable.incog
        }
    }
}
@Composable
fun getStaffImage(userId: Long?, roles: List<String>, sex: Any?): Any {
    // Si no hay ID, devolvemos un avatar genérico para que no crashee
    val id = userId ?: 0L
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var customAvatarUri by remember { mutableStateOf(prefs.getString("user_avatar_uri_$id", null)) }
    var customAvatarRes by remember { mutableStateOf(prefs.getInt("user_avatar_$id", 0)) }

    // Este bloque escucha los cambios en memoria en tiempo real
    DisposableEffect(id) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "user_avatar_uri_$id") {
                customAvatarUri = sharedPreferences.getString(key, null)
            }
            if (key == "user_avatar_$id") {
                customAvatarRes = sharedPreferences.getInt(key, 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)


        customAvatarUri = prefs.getString("user_avatar_uri_$id", null)
        customAvatarRes = prefs.getInt("user_avatar_$id", 0)

        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // 1. Si hay foto de galería, la devuelve
    if (customAvatarUri != null) return customAvatarUri!!

    // 2. Si hay avatar por defecto elegido, lo devuelve
    if (customAvatarRes != 0) return customAvatarRes!!

    // SI NO HAY NADA: Lógica de avatares por defecto según el sexo
    val upperRoles = roles.map { it.uppercase() }
    val isDoctor = upperRoles.any { it.contains("DOCTOR") || it.contains("DENTIST") }

    val femaleOptions = listOf(
        R.drawable.usuario1, R.drawable.usuario4, R.drawable.usuario5,
        R.drawable.usuario6, R.drawable.usuario7, R.drawable.usuario8,
        R.drawable.usuario11, R.drawable.doctor1, R.drawable.doctor3, R.drawable.doctor5
    )
    val maleOptions = listOf(
        R.drawable.usuario2, R.drawable.usuario3, R.drawable.usuario9,
        R.drawable.usuario10, R.drawable.usuario12, R.drawable.usuario13,
        R.drawable.usuario20, R.drawable.doctor2, R.drawable.doctor4
    )

    val sexEnum = when (sex) {
        is com.example.dynalar_frontend_v1.model.patient.Sex -> sex
        is String -> try { com.example.dynalar_frontend_v1.model.patient.Sex.valueOf(sex.uppercase()) } catch (e: Exception) { null }
        else -> null
    }

    return when (sexEnum) {
        com.example.dynalar_frontend_v1.model.patient.Sex.FEMALE -> femaleOptions[(id % femaleOptions.size).toInt()]
        com.example.dynalar_frontend_v1.model.patient.Sex.MALE -> maleOptions[(id % maleOptions.size).toInt()]
        else -> {
            // Si el sexo es null u OTHER, mezcla todos
            val mixedOptions = femaleOptions + maleOptions
            mixedOptions[(id % mixedOptions.size).toInt()]
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

val LocalAppLocale = compositionLocalOf { Locale.getDefault() }