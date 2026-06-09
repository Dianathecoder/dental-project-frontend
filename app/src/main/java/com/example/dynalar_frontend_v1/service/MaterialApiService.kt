package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.Material
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MaterialApiService {

    @GET("material/index")
    suspend fun getAllMaterials(): List<Material>

    @GET("material/{id}")
    suspend fun getMaterialById(@Path("id") id: Long): Material

    @POST("material")
    suspend fun createMaterial(@Body material: Material): Material

    @PUT("material/{id}")
    suspend fun updateMaterial(@Path("id") id: Long, @Body material: Material): Material

    @DELETE("material/{id}")
    suspend fun deleteMaterial(@Path("id") id: Long): Response<Unit>

    @PUT("material/{id}/increase-stock")
    suspend fun increaseStock(@Path("id") id: Long, @Query("quantity") quantity: Int): Material

    @PUT("material/{id}/decrease-stock")
    suspend fun decreaseStock(@Path("id") id: Long, @Query("quantity") quantity: Int): Material

}
