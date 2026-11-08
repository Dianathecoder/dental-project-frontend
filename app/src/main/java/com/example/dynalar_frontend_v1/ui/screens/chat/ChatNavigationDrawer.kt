package com.example.dynalar_frontend_v1.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.example.dynalar_frontend_v1.model.chat.ChatScope
import com.example.dynalar_frontend_v1.model.chat.ContactDto
import com.example.dynalar_frontend_v1.viewmodel.ChatViewModel

@Composable
fun ChatNavigationDrawer(
    isStaff: Boolean,
    isAuxiliar: Boolean,
    isSupportManager: Boolean,
    isPatient: Boolean,
    onInternalChats: () -> Unit,
    onSupportChats: () -> Unit,
    onOpenRecentChat: (ChatScope, Long) -> Unit,
    vm: ChatViewModel
) {
    LaunchedEffect(isStaff, isAuxiliar, isSupportManager, isPatient) {
        if (isStaff) vm.loadRecentChats(ChatScope.STAFF)
        if (isAuxiliar || isSupportManager || isPatient) vm.loadRecentChats(ChatScope.SUPPORT)
    }

    Column(Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
        Text("Mensajes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp))
        Text("Dynalar Dental", modifier = Modifier.padding(start = 16.dp, bottom = 18.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        if (isStaff) {
            Text("COMPAÑEROS", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
            RecentChatsLabel()
            RecentChatItems(vm.recentStaffChats, ChatScope.STAFF, onOpenRecentChat)
            NavigationDrawerItem(
                label = { Text("Ver todos los compañeros") },
                selected = false,
                icon = { androidx.compose.material3.Icon(Icons.Default.Groups, contentDescription = null) },
                onClick = onInternalChats
            )
        }

        if (isAuxiliar || isSupportManager) {
            Text("CLIENTES", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
            RecentChatsLabel()
            RecentChatItems(vm.recentSupportChats, ChatScope.SUPPORT, onOpenRecentChat)
            NavigationDrawerItem(
                label = { Text("Ver todos los chats de clientes") },
                selected = false,
                icon = { androidx.compose.material3.Icon(Icons.Default.SupportAgent, contentDescription = null) },
                onClick = onSupportChats
            )
        } else if (isPatient) {
            Text("ATENCIÓN AL CLIENTE", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
            RecentChatsLabel()
            RecentChatItems(vm.recentSupportChats, ChatScope.SUPPORT, onOpenRecentChat)
            NavigationDrawerItem(
                label = { Text("Hablar con un auxiliar") },
                selected = false,
                icon = { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                onClick = onSupportChats
            )
        }
    }
}

@Composable
private fun RecentChatsLabel() {
    Text(
        "Chats recientes",
        color = Color(0xFF537895).copy(alpha = 0.62f),
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 16.dp, top = 3.dp, bottom = 2.dp)
    )
}

@Composable
private fun RecentChatItems(
    contacts: List<ContactDto>,
    scope: ChatScope,
    onOpenRecentChat: (ChatScope, Long) -> Unit
) {
    if (contacts.isEmpty()) {
        Text(
            "Aún no hay conversaciones",
            color = Color(0xFF537895).copy(alpha = 0.62f),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 3.dp, bottom = 8.dp)
        )
        return
    }

    contacts.forEach { contact ->
        NavigationDrawerItem(
            label = {
                Column {
                    Text(
                        contact.fullName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF145A8D),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        contact.lastMessage.orEmpty(),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            },
            selected = false,
            icon = {
                androidx.compose.foundation.layout.Box(
                    Modifier.width(40.dp).height(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.dynalar_frontend_v1.ui.components.UserAvatar(
                        avatarUrl = contact.avatarUrl,
                        userId = contact.id,
                        sexRaw = null,
                        rolesRaw = contact.roles.orEmpty(),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentDescription = "Avatar de ${contact.fullName}"
                    )
                }
            },
            badge = {
                if (contact.unread > 0) {
                    androidx.compose.material3.Badge { Text(contact.unread.toString()) }
                }
            },
            onClick = { onOpenRecentChat(scope, contact.id) }
        )
    }
}

@Composable
fun RightSideChatDrawer(
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.fillMaxSize().pointerInput(isOpen) {
            var dragStartX = 0f
            var dragDistance = 0f
            detectHorizontalDragGestures(
                onDragStart = { start ->
                    dragStartX = start.x
                    dragDistance = 0f
                },
                onHorizontalDrag = { change, amount ->
                    val fromRightEdge = dragStartX > size.width - 32.dp.toPx()
                    val fromDrawer = isOpen && dragStartX > size.width - 320.dp.toPx()
                    if ((!isOpen && fromRightEdge && amount < 0f) || (fromDrawer && amount > 0f)) {
                        dragDistance += amount
                        change.consume()
                    }
                },
                onDragEnd = {
                    if (!isOpen && dragDistance < -72.dp.toPx()) onOpen()
                    if (isOpen && dragDistance > 72.dp.toPx()) onClose()
                }
            )
        }
    ) {
        content()
        if (isOpen) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onClose)
            )
            Surface(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                tonalElevation = 8.dp,
                color = Color.White
            ) {
                drawerContent()
            }
        }
    }
}
