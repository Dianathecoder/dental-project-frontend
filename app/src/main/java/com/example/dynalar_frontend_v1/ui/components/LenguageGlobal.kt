package com.example.dynalar_frontend_v1.utils

import android.content.Context

import java.util.Locale


fun changeLanguage(context: Context, languageCode: String) {
    // Guardar preferencia
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit()
        .putString("language", languageCode)
        .apply()

    // Aplicar idioma al contexto actual SIN reiniciar
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}