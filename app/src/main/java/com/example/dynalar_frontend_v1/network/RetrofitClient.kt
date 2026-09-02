package com.example.dynalar_frontend_v1.network

import android.content.Context
import com.example.dynalar_frontend_v1.service.*
import com.example.dynalar_frontend_v1.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var sessionManager: SessionManager? = null

    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
            android.util.Log.d("SEGURIDAD_API", "SessionManager INICIALIZADO")
        }
    }

    private val appInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        // Añadir Idioma
        requestBuilder.header("Accept-Language", Locale.getDefault().language)

        // Añadir Token si existe
        sessionManager?.fetchAuthToken()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // Control de sesión expirada
        if (response.code == 401 || response.code == 403) {
            sessionManager?.clearSession()
            android.util.Log.e("SEGURIDAD_API", "Token expirado o inválido")
        }

        response
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(appInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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