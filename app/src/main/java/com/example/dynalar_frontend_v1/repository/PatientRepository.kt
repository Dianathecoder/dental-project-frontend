package com.example.dynalar_frontend_v1.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.dynalar_frontend_v1.model.PageResponse
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class PatientRepository {

    private val patientApiService = RetrofitClient.patientApiService

    suspend fun getAllPatients(page: Int = 0, size: Int = 100): PageResponse<Patient> {
        Log.e("CHIVATO", "ENTRADA EN getAllPatients")

        try {
            val response = patientApiService.getAllPatients(page, size)

            Log.e("CHIVATO", "CODI RESPOSTA: ${response.code()}")

            if (response.isSuccessful) {
                Log.e("CHIVATO", "RESPOSTA CORRECTE")
                return response.body() ?: PageResponse(content = emptyList())
            } else {
                Log.e("CHIVATO", "ERROR BODY: ${response.errorBody()?.string()}")
                throw Exception("Error del servidor: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("CHIVATO", "EXCEPCIO REAL: ${e.message}", e)
            throw e
        }
    }

    suspend fun searchPatients(query: String, page: Int = 0, size: Int = 100): PageResponse<Patient> {
        try {
            val response = patientApiService.searchPatients(query, page, size)
            if (response.isSuccessful) {
                return response.body() ?: PageResponse(content = emptyList())
            } else {
                throw Exception("Error del servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PatientRepository", "Error searching patients: ${e.message}")
            throw e
        }
    }

    suspend fun getIdPatient(id: Long): Response<Patient> {
        return patientApiService.getIdPatient(id)
    }

    suspend fun deletePatient(id: Long): Response<Unit> {
        return patientApiService.deletePatient(id)
    }

    suspend fun updatePatient(patient: Patient): Response<Patient> {
        return patientApiService.updatePatient(patient)
    }

    suspend fun createPatient(patient: Patient): Response<Patient> {
        return patientApiService.createPatient(patient)
    }

    suspend fun uploadPatientDocuments(
        contentResolver: ContentResolver,
        patientId: Long,
        uris: List<Uri>
    ) {
        uris.forEach { uri ->
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("No se pudo leer el archivo seleccionado")

            val fileName = resolveDisplayName(contentResolver, uri)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                bytes.toRequestBody(mimeType.toMediaType())
            )
            val typePart = mimeType.toRequestBody("text/plain".toMediaType())

            val primaryResponse = patientApiService.uploadPatientDocumentByPath(
                patientId = patientId,
                file = filePart,
                type = typePart
            )
            if (!primaryResponse.isSuccessful) {
                val primaryError = primaryResponse.errorBody()?.string().orEmpty()
                throw Exception(
                    "Error subiendo archivo (${primaryResponse.code()}): $primaryError"
                )
            }
        }
    }

    suspend fun deletePatientDocument(documentId: Long): Response<Unit> {
        return patientApiService.deletePatientDocument(documentId)
    }

    private fun resolveDisplayName(contentResolver: ContentResolver, uri: Uri): String {
        val fallbackName = "archivo_${System.currentTimeMillis()}"
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
            } else {
                fallbackName
            }
        } ?: fallbackName
    }
}