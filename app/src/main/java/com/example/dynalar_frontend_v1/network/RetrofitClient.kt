package com.example.dynalar_frontend_v1.network

import android.content.Context
import android.content.SharedPreferences
import com.example.dynalar_frontend_v1.service.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var sharedPreferences: SharedPreferences? = null

    // 1. Inicializa SharedPreferences desde tu MainActivity o Application
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("dynalar_prefs", Context.MODE_PRIVATE)
        android.util.Log.d("SEGURIDAD_API", "1. SharedPreferences INICIALIZADO")
    }

    // 2. En la función saveAuthToken
    fun saveAuthToken(token: String) {
        if (sharedPreferences == null) {
            android.util.Log.e("SEGURIDAD_API", "¡ERROR CRÍTICO! SharedPreferences es NULL al guardar")
        }
        sharedPreferences?.edit()?.putString("jwt_token", token)?.apply()
        android.util.Log.d("SEGURIDAD_API", "2. Token GUARDADO en memoria: $token")
    }

    // 3. Borra el token (llámalo al hacer logout)
    fun clearAuthToken() {
        sharedPreferences?.edit()?.remove("jwt_token")?.apply()
    }

    private val languageInterceptor = Interceptor { chain ->
        val currentLanguage = java.util.Locale.getDefault().language
        val request = chain.request().newBuilder()
            .header("Accept-Language", currentLanguage)
            .build()
        chain.proceed(request)
    }

    // 3. En el interceptor
    private val authInterceptor = Interceptor { chain ->
        if (sharedPreferences == null) {
            android.util.Log.e("SEGURIDAD_API", "¡ERROR CRÍTICO! SharedPreferences es NULL al leer")
        }
        val token = sharedPreferences?.getString("jwt_token", null)

        android.util.Log.d("SEGURIDAD_API", "3. Token ENVIADO en petición: $token")

        val request = token?.let {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: chain.request()
        chain.proceed(request)
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(languageInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val patientApiService: PatientApiService by lazy { retrofit.create(PatientApiService::class.java) }
    val appointmentApiService: AppointmentApiService by lazy { retrofit.create(AppointmentApiService::class.java) }
    val odontogramApiService: OdontogramApiService by lazy { retrofit.create(OdontogramApiService::class.java) }
    val treatmentApiService: TreatmentApiService by lazy { retrofit.create(TreatmentApiService::class.java) }
    val materialApiService: MaterialApiService by lazy { retrofit.create(MaterialApiService::class.java) }
    val dentalProcessApiService: DentalProcessApiService by lazy { retrofit.create(DentalProcessApiService::class.java) }
    val boxApiService: BoxApiService by lazy { retrofit.create(BoxApiService::class.java) }
}