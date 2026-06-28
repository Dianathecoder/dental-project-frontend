package com.example.dynalar_frontend_v1.network

import com.example.dynalar_frontend_v1.service.AppointmentApiService
import com.example.dynalar_frontend_v1.service.AuthApiService
import com.example.dynalar_frontend_v1.service.BoxApiService
import com.example.dynalar_frontend_v1.service.OdontogramApiService
import com.example.dynalar_frontend_v1.service.DentalProcessApiService
import com.example.dynalar_frontend_v1.service.MaterialApiService
import com.example.dynalar_frontend_v1.service.PatientApiService
import com.example.dynalar_frontend_v1.service.TreatmentApiService
import com.example.dynalar_frontend_v1.service.UserApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val request = authToken?.let {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: chain.request()
        chain.proceed(request)
    }

    //Configure the HTTP Client
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    //Configure the Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //Create services using the retrofit instance, not the okHttpClient
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