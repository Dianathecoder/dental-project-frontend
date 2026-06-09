package com.example.dynalar_frontend_v1.network

import com.example.dynalar_frontend_v1.service.AppointmentApiService
import com.example.dynalar_frontend_v1.service.BoxApiService
import com.example.dynalar_frontend_v1.service.OdontogramApiService
import com.example.dynalar_frontend_v1.service.DentalProcessApiService
import com.example.dynalar_frontend_v1.service.MaterialApiService
import com.example.dynalar_frontend_v1.service.PatientApiService
import com.example.dynalar_frontend_v1.service.TreatmentApiService
import com.example.dynalar_frontend_v1.service.UserApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApiService: UserApiService by lazy { instance.create(UserApiService::class.java) }
    val patientApiService: PatientApiService by lazy { instance.create(PatientApiService::class.java) }
    val appointmentApiService: AppointmentApiService by lazy { instance.create(AppointmentApiService::class.java) }
    val odontogramApiService: OdontogramApiService by lazy { instance.create(OdontogramApiService::class.java) }
    val treatmentApiService: TreatmentApiService by lazy { instance.create(TreatmentApiService::class.java) }

    val materialApiService: MaterialApiService by lazy { instance.create(MaterialApiService::class.java) }
    val dentalProcessApiService: DentalProcessApiService by lazy { instance.create(DentalProcessApiService::class.java) }
    val boxApiService: BoxApiService by lazy { instance.create(BoxApiService::class.java) }
}
