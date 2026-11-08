package com.example.dynalar_frontend_v1.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynalar_frontend_v1.model.chat.ChatScope
import com.example.dynalar_frontend_v1.model.chat.ContactDto
import com.example.dynalar_frontend_v1.model.chat.MessageDto
import com.example.dynalar_frontend_v1.model.chat.SendMessageRequest
import com.example.dynalar_frontend_v1.network.RetrofitClient
import com.google.gson.Gson
import com.example.dynalar_frontend_v1.service.ChatService
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ChatViewModel : ViewModel() {

    private val api get() = RetrofitClient.chatService
    private val gson = Gson()
    private var socket: WebSocket? = null

    var contacts by mutableStateOf<List<ContactDto>>(emptyList()); private set
    var recentStaffChats by mutableStateOf<List<ContactDto>>(emptyList()); private set
    var recentSupportChats by mutableStateOf<List<ContactDto>>(emptyList()); private set
    var contactsLoading by mutableStateOf(false); private set
    var unreadTotal by mutableLongStateOf(0L); private set
    var peer by mutableStateOf<ContactDto?>(null); private set
    var connected by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    val messages = mutableStateListOf<MessageDto>()

    fun loadContacts(scope: ChatScope, q: String? = null) = viewModelScope.launch {
        contactsLoading = true
        error = null
        try { contacts = api.chatContacts(scope.name, q?.ifBlank { null }) }
        catch (e: Exception) { error = e.message }
        finally { contactsLoading = false }
    }

    fun loadRecentChats(scope: ChatScope) = viewModelScope.launch {
        try {
            val recent = api.chatContacts(scope.name, null)
                .filter { !it.lastMessageAt.isNullOrBlank() }
                .sortedByDescending { it.lastMessageAt }
                .take(8)
            if (scope == ChatScope.STAFF) recentStaffChats = recent else recentSupportChats = recent
        } catch (e: Exception) {
            error = e.message
        }
    }

    fun refreshUnread() = viewModelScope.launch {
        try { unreadTotal = api.chatUnread()["count"] ?: 0L } catch (_: Exception) {}
    }

    fun openChat(id: Long, scope: ChatScope) {
        closeSocket()
        messages.clear()
        peer = null
        error = null

        socket = RetrofitClient.openChatWebSocket(scope.name, id, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModelScope.launch { connected = true }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = runCatching { gson.fromJson(text, MessageDto::class.java) }.getOrNull() ?: return
                viewModelScope.launch {
                    if (messages.none { it.id == message.id }) messages.add(message)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                viewModelScope.launch { connected = false }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModelScope.launch { connected = false }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                viewModelScope.launch {
                    connected = false
                    error = "No se pudo conectar al chat en tiempo real"
                }
            }
        })

        viewModelScope.launch {
            try {
                val conversation = api.chatOpen(id, scope.name)
                peer = conversation.contact
                val known = messages.map { it.id }.toSet()
                messages.addAll(conversation.messages.filter { it.id !in known })
            } catch (e: Exception) { error = e.message }
        }
    }

    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val activeSocket = socket
        if (activeSocket == null || !connected) {
            error = "El chat no está conectado. Vuelve a abrir la conversación."
            return
        }
        if (!activeSocket.send(gson.toJson(SendMessageRequest(clean)))) {
            error = "No se pudo enviar el mensaje"
        } else {
            error = null
        }
    }

    fun closeSocket() {
        connected = false
        socket?.close(1000, "chat closed")
        socket = null
    }

    override fun onCleared() {
        closeSocket()
        super.onCleared()
    }
}
