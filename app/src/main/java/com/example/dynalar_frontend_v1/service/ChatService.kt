package com.example.dynalar_frontend_v1.service

import com.example.dynalar_frontend_v1.model.chat.ContactDto
import com.example.dynalar_frontend_v1.model.chat.ConversationDto
import com.example.dynalar_frontend_v1.model.chat.MessageDto
import com.example.dynalar_frontend_v1.model.chat.SendMessageRequest
import com.example.dynalar_frontend_v1.model.chat.ChatScope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {
    @GET("api/chat/contacts")
    suspend fun chatContacts(@Query("scope") scope: String, @Query("q") q: String? = null): List<ContactDto>

    @GET("api/chat/with/{id}")
    suspend fun chatOpen(@Path("id") id: Long, @Query("scope") scope: String, @Query("after") after: Long? = null): ConversationDto

    @POST("api/chat/with/{id}")
    suspend fun chatSend(@Path("id") id: Long, @Query("scope") scope: String, @Body body: SendMessageRequest): MessageDto

    @GET("api/chat/unread-count")
    suspend fun chatUnread(): Map<String, Long>

}
