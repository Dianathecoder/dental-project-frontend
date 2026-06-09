package com.example.dynalar_frontend_v1.service




import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.model.AutoAssignRequest
import com.example.dynalar_frontend_v1.model.DaySummary
import com.example.dynalar_frontend_v1.model.PageResponse
import com.example.dynalar_frontend_v1.model.SlotRequest
import retrofit2.Response
import retrofit2.http.*


interface AppointmentApiService {
    @GET("appointment/index")
    suspend fun getAllAppointments(@Query("page") page: Int = 0,
                                   @Query("size") size: Int = 100,
                                   @Query("start") start: String? = null,
                                   @Query("end") end: String? = null): PageResponse<Appointment>

    @GET("appointment/patient/{patientId}")
    suspend fun getAppointmentsByPatientId(@Path("patientId") patientId: Long): List<Appointment>

    @GET("appointment/calendar-summary")
    suspend fun getCalendarSummary(@Query("start") start: String,
                                   @Query("end") end: String): List<DaySummary>

    @POST("appointment/available-slots")
    suspend fun getAvailableSlots(@Body request: SlotRequest): Map<String, List<String>>

    @POST("appointment/auto-assign")
    suspend fun autoAssignAppointment(@Body request: AutoAssignRequest): Response<Appointment>

    @DELETE("appointment/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    @PUT("appointment/update")
    suspend fun updateAppointment(@Body appointment: Appointment): Response<Appointment>
}