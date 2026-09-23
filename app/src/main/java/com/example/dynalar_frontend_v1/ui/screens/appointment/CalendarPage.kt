package com.example.dynalar_frontend_v1.ui.screens.appointment

import android.app.DatePickerDialog
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.TreatmentColors
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

private const val SLOT_HEIGHT_DP = 80
private const val DAY_START_HOUR = 8
private const val TOTAL_HOURS = 14
private const val TOP_MARGIN_DP = 16

@Composable
fun CalendarPage(
    viewModel: AppointmentViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onAppointmentClick: (Appointment) -> Unit = {},
    onAddAppointmentClick: (LocalDate, Int, Int) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val myUserId = sessionManager.getUserId()

    // 1. EVALUAR ROLES
    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")
    val isAdmin = sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")
    val isAuxiliar = sessionManager.hasRole("AUXILIAR") || sessionManager.hasRole("ROLE_AUXILIAR")
    val isDoctor = sessionManager.hasRole("DOCTOR") || sessionManager.hasRole("ROLE_DENTIST")

    val canViewAll = isSuperAdmin || isOwner || isAdmin || isAuxiliar
    val isManager = isSuperAdmin || isOwner || isAdmin
    val isDoctorOnly = isDoctor && !canViewAll
    val canCreateOrEdit = !isDoctorOnly

    // 2. ESTADO DEL FILTRO (-1L = Ver todos, -2L = Mi calendario)
    var selectedFilterId by remember { mutableStateOf(if (isDoctorOnly) myUserId else -1L) }

    val selectedDate = viewModel.selectedCalendarDate
    val sharedScrollState = rememberScrollState()

    LaunchedEffect(selectedDate) {
        val startOfDay = selectedDate.atStartOfDay()
        val endOfDay = selectedDate.atTime(23, 59, 59)
        viewModel.fetchCalendar(startOfDay, endOfDay)
    }

    LaunchedEffect(Unit) {
        if (canViewAll) userViewModel.getAllStaff()
    }

    val doctorsList = userViewModel.staffList.filter { staff ->
        staff.roles.any { it.uppercase().contains("DOCTOR") || it.uppercase().contains("DENTIST") }
    }

    val uiState = viewModel.uiStateCalendar
    val appointmentsForDay = remember(uiState, selectedDate, selectedFilterId) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data.filter { appointment ->
                appointment.startTime?.startsWith(selectedDate.toString()) == true
            }.filter { appt ->
                when {
                    isDoctorOnly -> appt.dentist?.id == myUserId
                    selectedFilterId == -1L -> true // Global
                    selectedFilterId == -2L -> appt.dentist?.id == myUserId // Mi propio calendario
                    else -> appt.dentist?.id == selectedFilterId // Un doctor en específico
                }
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(12.dp))

                // --- FILA 1: TÍTULO Y BOTÓN + ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTopBar(title = stringResource(R.string.calendar_title), onNavigateBack = onNavigateBack)

                    if (canCreateOrEdit) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF537895))
                                .clickable { onAddAppointmentClick(selectedDate, 9, 0) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // --- FILA 2: CONTROLES DE FECHA (Como en la imagen) ---
                CalendarHeader(
                    selectedDate = selectedDate,
                    onPrevDay = { viewModel.updateSelectedDate(selectedDate.minusDays(1)) },
                    onNextDay = { viewModel.updateSelectedDate(selectedDate.plusDays(1)) },
                    onTodayClick = { viewModel.updateSelectedDate(LocalDate.now()) },
                    onDateSelected = { viewModel.updateSelectedDate(it) }
                )

                // --- FILA 3: FILTRO DESPLEGABLE (Como en la imagen) ---
                if (canViewAll) {
                    var expanded by remember { mutableStateOf(false) }

                    val filterLabel = when (selectedFilterId) {
                        -1L -> "Tots els doctors (Global)"
                        -2L -> "El meu calendari"
                        else -> {
                            val doc = doctorsList.find { it.id == selectedFilterId }
                            doc?.let { "Dr/a. ${it.name} ${it.surname}" } ?: "Filtrar per Doctor"
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                        Surface(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = filterLabel, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White).fillMaxWidth(0.9f)
                        ) {
                            Text("VISTES GLOBALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                            DropdownMenuItem(
                                text = { Text("Tots els doctors (Global)", fontWeight = FontWeight.Medium) },
                                onClick = { selectedFilterId = -1L; expanded = false }
                            )

                            if (isManager || isDoctor) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                                Text("PERSONAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0D47A1), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("El meu calendari", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = { selectedFilterId = -2L; expanded = false }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                            Text("FILTRAR PER DOCTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                            doctorsList.forEach { doctor ->
                                DropdownMenuItem(
                                    text = { Text("Dr/a. ${doctor.name} ${doctor.surname}") },
                                    onClick = { selectedFilterId = doctor.id ?: -1L; expanded = false }
                                )
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (uiState is InterfaceGlobal.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF537895))
            }
            Row(modifier = Modifier.fillMaxWidth().verticalScroll(sharedScrollState)) {
                HoursColumn()
                AppointmentsColumn(
                    appointments = appointmentsForDay,
                    onAppointmentClick = onAppointmentClick,
                    onSlotClick = { hour, minute -> if (canCreateOrEdit) onAddAppointmentClick(selectedDate, hour, minute) }
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }
    val dayNum = selectedDate.dayOfMonth
    val monthName = selectedDate.month.getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDatePicker) {
        val dialog = remember(selectedDate) {
            DatePickerDialog(context, { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day)); showDatePicker = false }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth).also { it.setOnDismissListener { showDatePicker = false } }
        }
        LaunchedEffect(Unit) { dialog.show() }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onTodayClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF537895)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.height(38.dp)
        ) {
            Text(text = "Hoy", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevDay, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFF537895))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onNextDay, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF537895))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier.clickable { showDatePicker = true }.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF537895), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$dayName $dayNum de $monthName", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF537895))
            }
        }
    }
}

// ... Mantén aquí debajo tus funciones HoursColumn, AppointmentsColumn, AppointmentCard, etc. que tenías sin cambiar ...
@Composable
fun HoursColumn() {
    Column(modifier = Modifier.width(58.dp)) {
        Spacer(modifier = Modifier.height(TOP_MARGIN_DP.dp))
        (DAY_START_HOUR until DAY_START_HOUR + TOTAL_HOURS).forEach { hour ->
            Box(modifier = Modifier.height(SLOT_HEIGHT_DP.dp).fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Text(text = "%02d:00".format(hour), fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp, top = 3.dp))
            }
        }
    }
}

@Composable
fun AppointmentsColumn(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    onSlotClick: (hour: Int, minute: Int) -> Unit
) {
    val totalHeightDp = TOTAL_HOURS * SLOT_HEIGHT_DP

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(top = TOP_MARGIN_DP.dp).height(totalHeightDp.dp)
    ) {
        val columnMaxWidth = maxWidth

        Column(modifier = Modifier.height(totalHeightDp.dp).fillMaxWidth()) {
            (0 until TOTAL_HOURS).forEach { hourOffset ->
                val hour = DAY_START_HOUR + hourOffset
                Box(
                    modifier = Modifier
                        .height(SLOT_HEIGHT_DP.dp)
                        .fillMaxWidth()
                        .clickable { onSlotClick(hour, 0) }
                        .drawBehind {
                            drawLine(color = Color(0xFFDDDDDD), start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                            val midY = size.height / 2f
                            val nativePaint = Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.parseColor("#E8E8E8")
                                strokeWidth = 1.dp.toPx()
                                pathEffect = DashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
                            }
                            drawContext.canvas.nativeCanvas.drawLine(0f, midY, size.width, midY, nativePaint)
                        }
                )
            }
        }

        appointments.forEach { appointment ->
            val startMinutes = parseTimeToMinutes(appointment.startTime)
            val duration = appointment.treatment?.durationMinutes ?: 30

            if (startMinutes != null) {
                val endMinutes = startMinutes + duration

                val overlappingApps = appointments.filter { other ->
                    val oStart = parseTimeToMinutes(other.startTime) ?: 0
                    val oEnd = oStart + (other.treatment?.durationMinutes ?: 30)
                    startMinutes < oEnd && endMinutes > oStart
                }.sortedBy { it.id ?: 0L }

                val totalColumns = overlappingApps.size
                val columnIndex = overlappingApps.indexOf(appointment).coerceAtLeast(0)

                val cardWidth = columnMaxWidth / totalColumns
                val offsetX = cardWidth * columnIndex

                val topOffsetMinutes = startMinutes - (DAY_START_HOUR * 60)
                if (topOffsetMinutes >= 0) {
                    val topDp = topOffsetMinutes * SLOT_HEIGHT_DP / 60f
                    val heightDp = appointmentHeightDp(appointment)

                    AppointmentCard(
                        appointment = appointment,
                        color = colorForTreatment(appointment.treatment?.id),
                        modifier = Modifier
                            .width(cardWidth)
                            .offset(x = offsetX, y = topDp.dp)
                            .height(heightDp.dp)
                            .padding(end = 2.dp),
                        onClick = { onAppointmentClick(appointment) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val startLabel = formatTime(appointment.startTime)
    val endLabel = run {
        val startMin = parseTimeToMinutes(appointment.startTime)
        val duration = appointment.treatment?.durationMinutes
        if (startMin != null && duration != null) {
            val endMin = startMin + duration
            "%02d:%02d".format(endMin / 60, endMin % 60)
        } else {
            formatTime(appointment.endTime)
        }
    }
    val boxInfo = appointment.box?.number?.let { "Box $it" } ?: ""

    val patientName = "${appointment.patient?.name ?: ""} ${appointment.patient?.lastName ?: ""}".trim()
    val doctorName = "Dr/a. ${appointment.dentist?.surname ?: ""}".trim()
    val treatmentName = appointment.treatment?.name ?: ""
    val infectiousDeceases = appointment.patient?.medicalRecord?.infectiousDeceases
    val allergies = appointment.patient?.medicalRecord?.allergies
    val hasAllergies = !allergies.isNullOrBlank()
    val hasInfectious = !infectiousDeceases.isNullOrBlank()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(width = 3.dp, color = color, shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
            .clickable { onClick() }
            .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (boxInfo.isNotEmpty()) "$startLabel - $endLabel | $boxInfo" else "$startLabel - $endLabel",
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = patientName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (hasAllergies) {
                Text(
                    text = stringResource(R.string.patient_allergies_prefix, allergies!!),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasInfectious) {
                Text(
                    text = stringResource(R.string.patient_infectious_prefix, infectiousDeceases!!),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (treatmentName.isNotBlank()) {
                Text(
                    text = "$treatmentName | $doctorName",
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.appointment_view_summary),
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun colorForTreatment(treatmentId: Long?): Color {
    if (treatmentId == null) return Color(0xFF4DB6AC)
    return TreatmentColors[(treatmentId % TreatmentColors.size).toInt()]
}

fun appointmentHeightDp(appointment: Appointment): Float {
    val durationMinutes = appointment.treatment?.durationMinutes ?: 30
    return (durationMinutes * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f)
}

fun extractTimeOnly(dateTimeString: String?): String? {
    if (dateTimeString == null) return null
    return if (dateTimeString.contains("T")) {
        dateTimeString.split("T").lastOrNull()
    } else {
        dateTimeString.split(" ").lastOrNull()
    }
}

fun parseTimeToMinutes(time: String?): Int? {
    val cleanTime = extractTimeOnly(time) ?: return null
    return try {
        val parts = cleanTime.split(":")
        parts[0].toInt() * 60 + parts[1].toInt()
    } catch (e: Exception) { null }
}

fun formatTime(time: String?): String {
    val cleanTime = extractTimeOnly(time) ?: return ""
    return try {
        val parts = cleanTime.split(":")
        "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) { cleanTime }
}