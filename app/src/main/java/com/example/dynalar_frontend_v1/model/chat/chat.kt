package com.example.dynalar_frontend_v1.model.chat

enum class ChatScope {
    STAFF,
    SUPPORT
}

data class ContactDto(
    val id: Long,
    val name: String?,
    val surname: String?,
    val avatarUrl: String?,
    val role: String?,
    val roles: List<String>?,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unread: Long,
    val patientId: Long? = null
) {
    val fullName get() = "${name ?: ""} ${surname ?: ""}".trim()
}

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val text: String,
    val createdAt: String?,
    val mine: Boolean,
    val read: Boolean
)

data class ConversationDto(val contact: ContactDto, val messages: List<MessageDto>)
data class SendMessageRequest(val text: String)
