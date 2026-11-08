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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.model.chat.ContactDto
import com.example.dynalar_frontend_v1.model.chat.ChatScope
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.viewmodel.ChatViewModel
import kotlinx.coroutines.delay




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContactsScreen(
    scope: ChatScope,
    topic: String? = null,
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    vm: ChatViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedRole by remember(scope) { mutableStateOf("ALL") }
    val roleFilters = listOf(
        "ALL" to "Todos",
        "DENTIST" to "Doctores",
        "ADMIN" to "Admin",
        "AUXILIAR" to "Auxiliares",
        "OWNER" to "Owner",
        "SUPERADMIN" to "Superadmin"
    )
    val visibleContacts = vm.contacts.filter { contact ->
        selectedRole == "ALL" || when (selectedRole) {
            "DENTIST" -> contact.roles.orEmpty().any { it.equals("DENTIST", true) || it.equals("DOCTOR", true) }
            else -> contact.roles.orEmpty().any { it.equals(selectedRole, true) }
        }
    }

    // Recarga al abrir, al buscar y cada 10s (para ver nuevos no leídos)
    LaunchedEffect(query, scope) {
        delay(if (query.isEmpty()) 0 else 300) // debounce
        while (true) { vm.loadContacts(scope, query); delay(10_000) }
    }

    Scaffold(
        containerColor = FondoPagina,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            scope == ChatScope.STAFF -> "Chats de compañeros"
                            topic != null -> "Elige un auxiliar"
                            else -> "Chats con auxiliares"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Cercar persona…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            )
            if (scope == ChatScope.STAFF) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(roleFilters, key = { it.first }) { (role, label) ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(label) }
                        )
                    }
                }
            }
            if (vm.contactsLoading && vm.contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = ButtonPrimary) }
            } else if (visibleContacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        if (vm.contacts.isEmpty()) "No hay contactos disponibles" else "No hay contactos con este filtro",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleContacts, key = { it.id }) { c -> ContactRow(c) { onOpenChat(c.id) } }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(c: ContactDto, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), Alignment.Center) {
                Text(
                    "${c.name?.firstOrNull() ?: ""}${c.surname?.firstOrNull() ?: ""}".uppercase(),
                    fontWeight = FontWeight.Bold, color = Color(0xFF1A5BB2)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.fullName, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(c.role ?: "", fontSize = 11.sp, color = Color(0xFF537895))
                Text(c.lastMessage ?: "Inicia la conversa", fontSize = 13.sp, color = Color.Gray,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(hhmm(c.lastMessageAt), fontSize = 11.sp, color = Color.Gray)
                if (c.unread > 0) {
                    Spacer(Modifier.height(4.dp))
                    Badge(containerColor = ButtonPrimary) { Text(c.unread.toString()) }
                }
            }
        }
    }
}

fun hhmm(iso: String?): String = iso?.substringAfter('T', "")?.take(5) ?: ""
