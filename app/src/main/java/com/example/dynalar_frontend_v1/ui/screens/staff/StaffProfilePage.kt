package com.example.dynalar_frontend_v1.ui.screens.staff

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.getStaffImage
import com.example.dynalar_frontend_v1.utils.SessionManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// 1. Nous Estats de Fitxatge
enum class ClockInStatus {
    COMPLETED, // Verd: Ha fitxat entrada i sortida
    WORKING,   // Blau: Ha fitxat entrada però encara no ha sortit
    MISSED,    // Vermell: No ha fitxat (ni entrada ni sortida)
    PENDING    // Gris: Dies futurs o no computables
}

// 2. Model de dades per a cada dia
data class DailyAttendance(
    val status: ClockInStatus,
    val checkInTime: LocalTime? = null,
    val checkOutTime: LocalTime? = null
)

// 3. Lògica de simulació
fun getAttendanceForDate(date: LocalDate, isClockedInToday: Boolean?): DailyAttendance {
    val today = LocalDate.now()
    return when {
        date.isAfter(today) || date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> {
            DailyAttendance(ClockInStatus.PENDING)
        }
        date == today -> {
            if (isClockedInToday == true) {
                DailyAttendance(ClockInStatus.WORKING, LocalTime.of(8, 5), null)
            } else {
                DailyAttendance(ClockInStatus.MISSED, null, null)
            }
        }
        else -> {
            if (date.dayOfMonth == 15 || date.dayOfMonth == 5) {
                DailyAttendance(ClockInStatus.MISSED, null, null)
            } else if (date.dayOfMonth % 2 == 0) {
                DailyAttendance(ClockInStatus.COMPLETED, LocalTime.of(8, 0), LocalTime.of(17, 0))
            } else {
                DailyAttendance(ClockInStatus.COMPLETED, LocalTime.of(7, 55), LocalTime.of(17, 10))
            }
        }
    }
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
    onNavigateToSchedule: (Long) -> Unit = {},
    isClockedIn: Boolean? = false
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val canManageStaff = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN") ||
            sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER") ||
            sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")

    val roles = staff.roles.map { it.uppercase() }
    val isDoctor = roles.any { it.contains("DOCTOR") || it.contains("DENTIST") }

    val todayAttendance = remember(isClockedIn) { getAttendanceForDate(LocalDate.now(), isClockedIn) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val selectedAttendance = remember(selectedDate, isClockedIn) {
        getAttendanceForDate(selectedDate, isClockedIn)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    CustomTopBar(
                        title = "Perfil de l'Empleat",
                        onNavigateBack = onNavigateBack
                    )

                    if (canManageStaff) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // NOMÉS QUEDA EL MENÚ DE 3 PUNTS, L'EDITAR ESTÀ A LA TARGETA
                            Box {
                                IconButton(onClick = { expandedMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opcions", tint = Color(0xFF0D47A1))
                                }
                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Eliminar Empleat", color = Color(0xFFD32F2F)) },
                                        onClick = {
                                            expandedMenu = false
                                            showDeleteDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val minScreenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = minScreenHeight)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // BLOQUE 1: CABECERA Y BOTONES
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StaffHeaderCard(
                        staff = staff,
                        attendance = todayAttendance,
                        canManageStaff = canManageStaff,
                        onEditClick = { staff.id?.let { onEditClick(it) } }
                    )

                    StaffActionGridSection(
                        isDoctor = isDoctor,
                        onDoctorAgendaClick = onDoctorAgendaClick,
                        onDoctorPatientsClick = onDoctorPatientsClick,
                        onNavigateToChat = { staff.id?.let { onNavigateToChat(it) } },
                        onNavigateToSchedule = { staff.id?.let { onNavigateToSchedule(it) } }
                    )
                }

                // BLOQUE 2: CALENDARIO (MÁS COMPACTO) Y ESTADO DETALLADO
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Control de Fitxatges",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    VisualMonthCalendarCard(
                        selectedDate = selectedDate,
                        onDateSelected = { newDate -> selectedDate = newDate },
                        isClockedInToday = isClockedIn
                    )

                    StaffAttendanceCardForDate(
                        selectedDate = selectedDate,
                        attendance = selectedAttendance
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))
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
fun StaffHeaderCard(
    staff: User,
    attendance: DailyAttendance,
    canManageStaff: Boolean,
    onEditClick: () -> Unit
) {
    val roles = staff.roles.map { it.uppercase() }
    val roleLabel = when {
        roles.any { it.contains("SUPERADMIN") } -> "SuperAdmin"
        roles.any { it.contains("OWNER") } -> "Propietari/a"
        roles.any { it.contains("ADMIN") } -> "Administrador/a"
        roles.any { it.contains("DOCTOR") || it.contains("DENTIST") } -> "Doctor/a"
        roles.any { it.contains("AUXILIAR") } -> "Auxiliar"
        else -> "Personal"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // BOTÓ D'EDITAR A DALT A LA DRETA DINS LA TARGETA
            if (canManageStaff) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp) // Marge per no enganxar-se massa a la vora
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 👇 AQUÍ USAMOS EL NUEVO COMPONENTE UNIVERSAL 👇
                com.example.dynalar_frontend_v1.ui.components.UserAvatar(
                    avatarUrl = staff.avatarUrl,
                    userId = staff.id,
                    sexRaw = staff.sex,
                    rolesRaw = staff.roles,
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${staff.name ?: ""} ${staff.surname ?: ""}".trim(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = roleLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val (statusText, statusBg, statusTextColor) = when (attendance.status) {
                    ClockInStatus.COMPLETED -> Triple("Jornada Completada", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    ClockInStatus.WORKING -> Triple("Treballant ara", Color(0xFFE3F2FD), Color(0xFF1565C0))
                    ClockInStatus.MISSED -> Triple("Sense Fitxar", Color(0xFFFFEBEE), Color(0xFFC62828))
                    ClockInStatus.PENDING -> Triple("Pendent", Color(0xFFF1F5F9), Color(0xFF64748B))
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val userEmail = staff.email
                    if (!userEmail.isNullOrBlank()) {
                        InfoDetailRow(icon = Icons.Default.Email, label = "Email", value = userEmail)
                    }
                    val userPhone = staff.phone
                    if (!userPhone.isNullOrBlank()) {
                        InfoDetailRow(icon = Icons.Default.Phone, label = "Telèfon", value = userPhone)
                    }
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
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
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
    onNavigateToChat: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isDoctor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
            }
        }

        StaffActionCard(
            title = "Xat Intern",
            icon = Icons.AutoMirrored.Filled.Chat,
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToChat
        )

        StaffActionCard(
            title = "Calendari Laboral i Vacances",
            icon = Icons.Default.EventNote,
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToSchedule
        )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .height(55.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF0D47A1),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun VisualMonthCalendarCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    isClockedInToday: Boolean?
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = LocalDate.now()

    val locale = Locale.forLanguageTag("ca")
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    val monthYearText = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() }

    val attendanceData = remember(currentMonth, isClockedInToday) {
        val map = mutableMapOf<LocalDate, DailyAttendance>()
        for (i in 1..currentMonth.lengthOfMonth()) {
            val date = currentMonth.atDay(i)
            map[date] = getAttendanceForDate(date, isClockedInToday)
        }
        map
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // PADDINGS MÁS PEQUEÑOS EN EL CALENDARIO PARA QUE SEA MÁS COMPACTO
        Column(modifier = Modifier.padding(10.dp)) {
            // Cabecera: Canvi de Mes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = Color(0xFF0D47A1))
                }

                Text(
                    text = monthYearText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, "Mes següent", tint = Color(0xFF0D47A1))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dies de la setmana
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val daysOfWeek = listOf("Dl", "Dt", "Dc", "Dj", "Dv", "Ds", "Dg")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Graella del mes (Filas más juntas)
            val offset = currentMonth.atDay(1).dayOfWeek.value - 1
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), // Menos espacio entre semanas
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - offset + 1

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                val date = currentMonth.atDay(day)
                                val isSelected = date == selectedDate
                                val attendance = attendanceData[date]
                                val isToday = date == today

                                val bgColor = when {
                                    isSelected -> Color(0xFF0D47A1)
                                    attendance?.status == ClockInStatus.COMPLETED -> Color(0xFFE8F5E9)
                                    attendance?.status == ClockInStatus.WORKING -> Color(0xFFE3F2FD)
                                    attendance?.status == ClockInStatus.MISSED -> Color(0xFFFFEBEE)
                                    isToday -> Color(0xFFEFF6FF)
                                    else -> Color.Transparent
                                }

                                val textColor = when {
                                    isSelected -> Color.White
                                    attendance?.status == ClockInStatus.COMPLETED -> Color(0xFF2E7D32)
                                    attendance?.status == ClockInStatus.WORKING -> Color(0xFF1565C0)
                                    attendance?.status == ClockInStatus.MISSED -> Color(0xFFC62828)
                                    isToday -> Color(0xFF0D47A1)
                                    else -> Color(0xFF1E293B)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp) // CÍRCULOS MÁS PEQUEÑOS (Antes 36.dp, ahora 28.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .clickable {
                                            onDateSelected(date)
                                            if (date.monthValue != currentMonth.monthValue) {
                                                currentMonth = YearMonth.from(date)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 12.sp, // Fuente un pelín más pequeña
                                        color = textColor,
                                        fontWeight = if (isSelected || attendance?.status != ClockInStatus.PENDING) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AttendanceStyle(
    val icon: ImageVector,
    val tintColor: Color,
    val bgColor: Color,
    val title: String
)

@Composable
private fun StaffAttendanceCardForDate(
    selectedDate: LocalDate,
    attendance: DailyAttendance
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val checkInStr = attendance.checkInTime?.format(timeFormatter) ?: "---"
    val checkOutStr = attendance.checkOutTime?.format(timeFormatter) ?: "---"

    val style = when (attendance.status) {
        ClockInStatus.COMPLETED -> AttendanceStyle(Icons.Default.CheckCircle, Color(0xFF2E7D32), Color(0xFFE8F5E9), "Jornada Completada")
        ClockInStatus.WORKING -> AttendanceStyle(Icons.Default.PlayCircle, Color(0xFF1565C0), Color(0xFFE3F2FD), "Treballant Actualment")
        ClockInStatus.MISSED -> AttendanceStyle(Icons.Default.Cancel, Color(0xFFC62828), Color(0xFFFFEBEE), "Sense Fitxar")
        ClockInStatus.PENDING -> AttendanceStyle(Icons.Default.Schedule, Color(0xFF64748B), Color(0xFFF1F5F9), "No Computable")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(style.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = style.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = style.tintColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Data: ${selectedDate.format(dateFormatter)}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Entrada", fontSize = 11.sp, color = Color.Gray)
                        Text(checkInStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Sortida", fontSize = 11.sp, color = Color.Gray)
                        Text(checkOutStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
            }
        }
    }
}