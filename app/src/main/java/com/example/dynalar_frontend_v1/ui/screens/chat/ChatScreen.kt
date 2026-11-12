package com.example.dynalar_frontend_v1.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.model.chat.MessageDto
import com.example.dynalar_frontend_v1.model.chat.ChatScope
import com.example.dynalar_frontend_v1.model.chat.ContactDto
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.ui.components.UserAvatar
import com.example.dynalar_frontend_v1.viewmodel.ChatViewModel
import hhmm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: Long,
    scope: ChatScope,
    topic: String? = null,
    onBack: () -> Unit,
    onViewPeerProfile: (ChatScope, ContactDto) -> Unit = { _, _ -> },
    vm: ChatViewModel = viewModel()
) {
    var text by remember(receiverId, topic) { mutableStateOf(topic.orEmpty()) }
    val listState = rememberLazyListState()

    DisposableEffect(receiverId, scope) {
        vm.openChat(receiverId, scope)
        onDispose { vm.closeSocket() }
    }

    LaunchedEffect(vm.messages.size) {
        if (vm.messages.isNotEmpty()) listState.animateScrollToItem(vm.messages.lastIndex)
    }

    Scaffold(
        containerColor = FondoPagina,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (scope == ChatScope.STAFF) onBack()
                                else vm.peer?.let { onViewPeerProfile(scope, it) }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (vm.peer != null) {
                            UserAvatar(
                                avatarUrl = vm.peer?.avatarUrl,
                                userId = vm.peer?.id,
                                sexRaw = null,
                                rolesRaw = vm.peer?.roles.orEmpty(),
                                modifier = Modifier.size(42.dp).clip(CircleShape),
                                contentDescription = "Avatar de ${vm.peer?.fullName}"
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = Color(0xFFE3F2FD)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF537895))
                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                vm.peer?.fullName ?: "…",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                if (vm.connected) "En línea · ${scope.name.lowercase()}" else "Conectando…",
                                fontSize = 12.sp,
                                color = if (vm.connected) Color(0xFF39875A) else Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 2000) text = it },
                    placeholder = { Text("Escriu un missatge…") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        vm.send(text.trim())
                        text = ""
                    },
                    enabled = text.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = ButtonPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    ) { pad ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (vm.error != null) {
                item {
                    Text(vm.error ?: "", color = Color(0xFFB3261E), modifier = Modifier.padding(8.dp))
                }
            }
            items(vm.messages, key = { it.id }) { m -> Bubble(m) }
        }
    }
}

@Composable
private fun Bubble(m: MessageDto) {
    Box(
        Modifier.fillMaxWidth(),
        contentAlignment = if (m.mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (m.mine) ButtonPrimary else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (m.mine) 16.dp else 4.dp,
                bottomEnd = if (m.mine) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(m.text, color = if (m.mine) Color.White else Color(0xFF2C3E50))
                Text(
                    hhmm(m.createdAt) + if (m.mine && m.read) "  ✓✓" else "",
                    fontSize = 10.sp,
                    color = if (m.mine) Color(0xCCFFFFFF) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
