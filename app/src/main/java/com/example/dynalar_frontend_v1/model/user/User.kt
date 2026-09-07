package com.example.dynalar_frontend_v1.model.user


open class User(
    open val id: Long? = null,
    open val name: String? = null,
    open val password: String? = null,
    open val surname: String? = null,
    open val email: String? = null,

    // NUEVOS CAMPOS
    open val phone: String? = null,
    open val dni: String? = null,
    open val sex: String? = null,
    open val roles: List<String> = emptyList(),
    open val avatarUrl: String? = null
)