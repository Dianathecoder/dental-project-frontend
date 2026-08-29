import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class LanguageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtenemos el idioma actual del móvil (ej: "es", "ca", "en")
        val currentLanguage = Locale.getDefault().language

        val originalRequest = chain.request()

        // Inyectamos la cabecera en todas las peticiones
        val requestWithHeaders = originalRequest.newBuilder()
            .header("Accept-Language", currentLanguage)
            .build()

        return chain.proceed(requestWithHeaders)
    }
}