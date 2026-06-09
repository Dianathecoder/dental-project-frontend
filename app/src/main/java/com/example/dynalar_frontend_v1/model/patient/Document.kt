package com.example.dynalar_frontend_v1.model.patient

import com.google.gson.annotations.SerializedName

data class Document(
    val id: Long? = null,
    @SerializedName(value = "patientId", alternate = ["patient_id"])
    val patientId: Long? = null,
    @SerializedName(value = "type", alternate = ["documentType"])
    val type: String? = null,
    @SerializedName(value = "documentUrl", alternate = ["document_url", "url"])
    val documentUrl: String? = null,
    @SerializedName(value = "creationDate", alternate = ["creation_date"])
    val creationDate: String? = null
)