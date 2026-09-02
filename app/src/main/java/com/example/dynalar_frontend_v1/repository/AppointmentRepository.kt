package com.example.dynalar_frontend_v1.repository



import com.example.dynalar_frontend_v1.model.appointment.Appointment


import com.example.dynalar_frontend_v1.model.appointment.AutoAssignRequest
import com.example.dynalar_frontend_v1.model.common.PageResponse
import com.example.dynalar_frontend_v1.model.appointment.SlotRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient
import retrofit2.Response

class AppointmentRepository {

    private val api = RetrofitClient.appointmentApiService

    suspend fun getAll(page: Int = 0, size: Int = 100, start: String? = null,end: String? = null): PageResponse<Appointment> = api.getAllAppointments(page, size, start, end)

    suspend fun getSlots(req: SlotRequest): Map<String, List<String>> = api.getAvailableSlots(req)

    suspend fun autoAssign(req: AutoAssignRequest): Response<Appointment> = api.autoAssignAppointment(req)
}