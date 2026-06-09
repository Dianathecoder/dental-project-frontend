package com.example.dynalar_frontend_v1.service



import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.model.user.TreatmentMaterialRequest
import retrofit2.http.*

interface TreatmentApiService {

    @GET("treatment/index")
    suspend fun getAllTreatments(): List<Treatment>

    @GET("treatment/{id}")
    suspend fun getTreatmentById(@Path("id") id: Long): retrofit2.Response<Treatment>

    @POST("treatment")
    suspend fun createTreatment(@Body treatment: Treatment): retrofit2.Response<Treatment>

    @PUT("treatment/update")
    suspend fun updateTreatment(@Body treatment: Treatment): retrofit2.Response<Treatment>

    @DELETE("treatment/{id}")
    suspend fun deleteTreatment(@Path("id") id: Long): retrofit2.Response<Unit>

    @POST("treatment/{id}/materials")
    suspend fun addMaterialToTreatment(@Path("id") id: Long, @Body treatmentMaterialRequest: TreatmentMaterialRequest): retrofit2.Response<Unit>

    @PUT("treatment/{id}/materials/{materialId}")
    suspend fun updateMaterialToTreatment(@Path("id") id: Long, @Path("materialId") materialId: Long, @Body treatmentMaterialRequest: TreatmentMaterialRequest): retrofit2.Response<Unit>

    @DELETE("treatment/{id}/materials/{materialId}")
    suspend fun deleteMaterialToTreatment(@Path("id") id: Long, @Path("materialId") materialId: Long): retrofit2.Response<Unit>

}