package com.example.dynalar_frontend_v1.model

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("page") val pageMetadata: PageMetadata? = null,
    
    // Estos campos se mantienen por si el backend los enviara en la raíz también
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 0,
    val number: Int = 0
)

data class PageMetadata(
    val size: Int,
    val number: Int,
    val totalElements: Long,
    val totalPages: Int
)
