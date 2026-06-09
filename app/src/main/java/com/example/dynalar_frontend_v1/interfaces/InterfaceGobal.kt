package com.example.dynalar_frontend_v1.interfaces


sealed interface InterfaceGlobal<out T> {

    object Idle : InterfaceGlobal<Nothing>

    object Loading : InterfaceGlobal<Nothing>

    data class Success<T>(val data: T) : InterfaceGlobal<T>

    data class Error(val message: String? = null) : InterfaceGlobal<Nothing>

    object NotFound : InterfaceGlobal<Nothing>
}