package com.example.dynalar_frontend_v1.model.user

open class User(
    open val id: Long? = null,
    open val name: String? = null,
    open val password: String? = null,
    open val surname: String? = null,
    open val email: String? = null,
    open val role: String? = null
)