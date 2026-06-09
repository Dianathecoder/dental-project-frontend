package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.PageResponse
import com.example.dynalar_frontend_v1.model.patient.Patient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PatientApiService {


    @GET("patient/index")
    suspend fun getAllPatients(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20): Response <PageResponse<Patient>>

    @GET("patient/search")
    suspend fun searchPatients(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20): Response<PageResponse<Patient>>


    @GET("patient/{id}")
    suspend fun getIdPatient(@Path("id") id: Long): Response<Patient>

    @POST("patient")
    suspend fun createPatient(@Body patient: Patient): Response<Patient>

    @PUT("patient/update")
    suspend fun updatePatient(@Body patient: Patient): Response<Patient>

    @DELETE("patient/{id}")
    suspend fun deletePatient(@Path("id") id: Long): Response<Unit>

    @Multipart
    @POST("document/patient/{patientId}/upload")
    suspend fun uploadPatientDocumentByPath(
        @Path("patientId") patientId: Long,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<ResponseBody>

    @DELETE("document/{documentId}")
    suspend fun deletePatientDocument(
        @Path("documentId") documentId: Long
    ): Response<Unit>
}