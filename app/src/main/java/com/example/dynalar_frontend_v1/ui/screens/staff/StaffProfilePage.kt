package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.utils.SessionManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class ClockInStatus {
    CLOCKED_IN,
    PENDING,
    LATE_WARNING
}

fun evaluateClockInStatus(isClockedIn: Boolean?, expectedStartTime: LocalTime = LocalTime.of(8, 0)): ClockInStatus {
    if (isClockedIn == true) return ClockInStatus.CLOCKED_IN
    val now = LocalTime.now()
    val warningThreshold = expectedStartTime.plusHours(1)
    return if (now.isAfter(warningThreshold)) ClockInStatus.LATE_WARNING else ClockInStatus.PENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffProfilePage(
    staff: User,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    onDoctorAgendaClick: () -> Unit = {},
    onDoctorPatientsClick: () -> Unit = {},
    onAttendanceHistoryClick: () -> Unit = {},
    isClockedIn: Boolean? = false
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val canManageStaff = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN") ||
            sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER") ||
            sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")

    val roles = staff.roles.map { it.uppercase() }
    val isDoctor = roles.any { it.contains("DOCTOR") || it.contains("DENTIST") }
    val status = remember(isClockedIn) { evaluateClockInStatus(isClockedIn) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(27.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    CustomTopBar(
                        title = "Perfil de l'Empleat",
                        onNavigateBack = onNavigateBack
                    )

                    if (canManageStaff) {
                        IconButton(
                            onClick = { staff.id?.let { onEditClick(it) } },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF0D47A1))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StaffHeaderCard(staff = staff, status = status)

            Spacer(modifier = Modifier.height(24.dp))

            StaffActionGridSection(
                isDoctor = isDoctor,
                onDoctorAgendaClick = onDoctorAgendaClick,
                onDoctorPatientsClick = onDoctorPatientsClick,
                onAttendanceHistoryClick = onAttendanceHistoryClick,
                onNavigateToChat = { staff.id?.let { onNavigateToChat(it) } }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Registre d'Activitat i Fitxatges",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AttendanceStatusRow(status = status)
                }

                // OPCIÓ D'ELIMINAR PERFIL EN UNA SECCIÓ DE PERILL MENYS ACCESSIBLE
                if (canManageStaff) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Gestió de Compte",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Eliminar definitivament aquest registre d'empleat del sistema.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar Empleat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && staff.id != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                staff.id?.let { onDeleteClick(it) }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun StaffHeaderCard(staff: User, status: ClockInStatus) {
    val roles = staff.roles.map { it.uppercase() }
    val roleLabel = when {
        roles.any { it.contains("SUPERADMIN") } -> "SuperAdmin"
        roles.any { it.contains("OWNER") } -> "Propietari/a"
        roles.any { it.contains("ADMIN") } -> "Administrador/a"
        roles.any { it.contains("DOCTOR") || it.contains("DENTIST") } -> "Doctor/a"
        roles.any { it.contains("AUXILIAR") } -> "Auxiliar"
        else -> "Personal"
    }

    val avatarRes = getStaffImage(staff.id, staff.roles, staff.sex)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${staff.name ?: ""} ${staff.surname ?: ""}".trim(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = roleLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0D47A1)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val (statusText, statusBg, statusTextColor) = when (status) {
                ClockInStatus.CLOCKED_IN -> Triple("Fitxat", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                ClockInStatus.PENDING -> Triple("Sense fitxar", Color(0xFFF1F5F9), Color(0xFF64748B))
                ClockInStatus.LATE_WARNING -> Triple("Alerta: +1h sense fitxar", Color(0xFFFFEBEE), Color(0xFFC62828))
            }

            Surface(
                color = statusBg,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userEmail = staff.email
                if (!userEmail.isNullOrBlank()) {
                    InfoDetailRow(icon = Icons.Default.Email, label = "Email", value = userEmail)
                }
                val userDni = staff.dni
                if (!userDni.isNullOrBlank()) {
                    InfoDetailRow(icon = Icons.Default.Badge, label = "DNI", value = userDni)
                }
                val userPhone = staff.phone
                if (!userPhone.isNullOrBlank()) {
                    InfoDetailRow(icon = Icons.Default.Phone, label = "Telèfon", value = userPhone)
                }
            }
        }
    }
}

@Composable
fun InfoDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
fun StaffActionGridSection(
    isDoctor: Boolean,
    onDoctorAgendaClick: () -> Unit,
    onDoctorPatientsClick: () -> Unit,
    onAttendanceHistoryClick: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isDoctor) {
                StaffActionCard(
                    title = "Agenda Cites",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    onClick = onDoctorAgendaClick
                )
                StaffActionCard(
                    title = "Pacients",
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f),
                    onClick = onDoctorPatientsClick
                )
            } else {
                StaffActionCard(
                    title = "Control Fitxatges",
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f),
                    onClick = onAttendanceHistoryClick
                )
                StaffActionCard(
                    title = "Xat Intern",
                    icon = Icons.Default.Chat,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChat
                )
            }
        }
    }
}

@Composable
fun StaffActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .height(95.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun AttendanceStatusRow(
    status: ClockInStatus,
    clockInDate: LocalDate = LocalDate.now(),
    clockInTime: LocalTime? = LocalTime.of(8, 15)
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm'h'")
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    when (status) {
        ClockInStatus.CLOCKED_IN -> {
            val timeStr = clockInTime?.format(timeFormatter) ?: ""
            val dateStr = clockInDate.format(dateFormatter)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fitxat el $dateStr a les $timeStr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jornada registrada correctament.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        ClockInStatus.LATE_WARNING -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFEF5350)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alerta de Fitxatge (+1 Hora)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ha transcorregut més d'una hora des de l'horari d'entrada i no hi ha fitxatge registrat.",
                            color = Color(0xFFC62828),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        ClockInStatus.PENDING -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estat de Jornada: Pendent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aquest treballador encara no ha iniciat el registre de jornada.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}