package com.example.dynalar_frontend_v1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.dynalar_frontend_v1.R

private val DarkColorScheme = darkColorScheme(
    primary   = Purple80,
    secondary = PurpleGrey80,
    tertiary  = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary   = ButtonPrimary,
    secondary = PurpleGrey40,
    tertiary  = Pink40,
    background = FondoPagina,      // fondo global coherente con la app
    surface    = BannerWhite
)

val AbyssinicaSIL = FontFamily(Font(R.font.abyssinicasil_regular, FontWeight.Normal))
val AGBookRounded = FontFamily(Font(R.font.ag_book_rounded_regular, FontWeight.Normal))
val RegularFont   = FontFamily(Font(R.font.regular, FontWeight.Normal))
val SFCompactDisplayBold = FontFamily(Font(R.font.sf_compact_display_bold, FontWeight.Bold))

@Composable
fun Dynalar_frontend_v1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // FALSE — con true Android 12+ ignora todos tus colores y usa los del sistema
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}