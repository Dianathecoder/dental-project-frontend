package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.model.management.Treatment
import com.example.dynalar_frontend_v1.model.staff.AbsenceEvent
import com.example.dynalar_frontend_v1.model.staff.AbsenceType
import com.example.dynalar_frontend_v1.model.staff.dentist.DentistAvailabilityDTO
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.UserAvatar
import com.example.dynalar_frontend_v1.ui.screens.appointment.ResumeDateScreen
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.StaffControlViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffProfilePage(
    staff: User,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    onDoctorAgendaClick: (Long) -> Unit = {},
    onDoctorPatientsClick: (Long) -> Unit = {},
    onNavigateToAvailability: (Long) -> Unit = {},
    staffControlViewModel: StaffControlViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    isClockedIn: Boolean? = false
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val canManageStaff = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN") ||
            sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER") ||
            sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")

    val roles = staff.roles.map { it.uppercase() }
    val isDoctor = roles.any { it.contains("DOCTOR") || it.contains("DENTIST") }
    val showAdvancedActions = isDoctor || roles.any {
        it.contains("ADMIN") || it.contains("SUPERADMIN") || it.contains("OWNER")
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }
    var selectedAppointmentForDetail by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(currentMonth, staff.id) {
        staffControlViewModel.fetchMonthlyAbsences(currentMonth)
        staffControlViewModel.fetchDailyAttendance(selectedDate)

        staff.id?.let { doctorId ->
            userViewModel.fetchDentistAvailability(doctorId)
            if (isDoctor) {
                appointmentViewModel.getDoctorAppointments(doctorId)
            }
        }
        treatmentViewModel.getTreatments()
    }

    val availabilityDto = userViewModel.dentistAvailability
    val allTreatments = treatmentViewModel.treatmentList
    val doctorAppointments = appointmentViewModel.doctorAppointments

    // Filtrar citas del doctor para el día seleccionado
    val appointmentsForSelectedDate = remember(doctorAppointments, selectedDate) {
        doctorAppointments.filter { appt ->
            appt.startTime?.startsWith(selectedDate.toString()) == true
        }
    }

    val absences = remember(staffControlViewModel.uiStateAbsences, staff) {
        val state = staffControlViewModel.uiStateAbsences
        if (state is InterfaceGlobal.Success) {
            val fullName = "${staff.name ?: ""} ${staff.surname ?: ""}".trim()
            state.data.map { dto ->
                AbsenceEvent(
                    id = dto.id,
                    title = dto.title,
                    staffName = dto.staffName,
                    type = try { AbsenceType.valueOf(dto.type.uppercase()) } catch (e: Exception) { AbsenceType.HOLIDAY },
                    startDate = LocalDate.parse(dto.startDate),
                    endDate = LocalDate.parse(dto.endDate)
                )
            }.filter { event ->
                val evtName = event.staffName?.trim() ?: ""
                evtName.contains(staff.name ?: "", ignoreCase = true) ||
                        (staff.surname != null && evtName.contains(staff.surname!!, ignoreCase = true)) ||
                        evtName.equals(fullName, ignoreCase = true)
            }
        } else emptyList()
    }

    val selectedAbsence = absences.find { !selectedDate.isBefore(it.startDate) && !selectedDate.isAfter(it.endDate) }

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
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StaffHeaderCard(
                        staff = staff,
                        selectedAbsence = selectedAbsence,
                        availabilityDto = availabilityDto,
                        canManageStaff = canManageStaff,
                        onEditClick = { staff.id?.let { onEditClick(it) } }
                    )

                    if (showAdvancedActions) {
                        ReadOnlyClinicalInfoCard(
                            availabilityDto = availabilityDto,
                            allTreatments = allTreatments
                        )
                    }

                    StaffActionGridSection(
                        showAdvancedActions = showAdvancedActions,
                        canManageStaff = canManageStaff,
                        roles = roles,
                        onDoctorAgendaClick = { staff.id?.let { onDoctorAgendaClick(it) } },
                        onDoctorPatientsClick = { staff.id?.let { onDoctorPatientsClick(it) } },
                        onNavigateToChat = { staff.id?.let { onNavigateToChat(it) } },
                        onNavigateToAvailability = { staff.id?.let { onNavigateToAvailability(it) } }
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Registre de Fitxatges i Ausències",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    VisualMonthCalendarCard(
                        selectedDate = selectedDate,
                        absences = absences,
                        doctorAppointments = doctorAppointments,
                        onDateSelected = { newDate ->
                            selectedDate = newDate
                            currentMonth = YearMonth.from(newDate)
                        }
                    )

                    StaffAttendanceCardForDate(
                        selectedDate = selectedDate,
                        selectedAbsence = selectedAbsence
                    )

                    // SECCIÓN DE CITAS / PACIENTES DEL DÍA EN SOLO LECTURA
                    if (isDoctor) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cites i Pacients de la Data (${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        if (appointmentsForSelectedDate.isEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sense cites programades per a aquesta data.",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                appointmentsForSelectedDate.forEach { appt ->
                                    DoctorAppointmentProfileCard(
                                        appointment = appt,
                                        onClick = { selectedAppointmentForDetail = appt }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }

    // DIÁLOGO MODAL EN SOLO LECTURA PARA VER DETALLES DE LA CITA
    selectedAppointmentForDetail?.let { appointment ->
        AlertDialog(
            onDismissRequest = { selectedAppointmentForDetail = null },
            confirmButton = {},
            dismissButton = null,
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp)) {
                    ResumeDateScreen(
                        appointment = appointment,
                        onBackClick = { selectedAppointmentForDetail = null },
                        onPatientClick = { }
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
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
    selectedAbsence: AbsenceEvent?,
    availabilityDto: DentistAvailabilityDTO?,
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

    val scheduleType = availabilityDto?.let { detectScheduleTypeLocal(it) } ?: 2
    val scheduleText = when (scheduleType) {
        1 -> "Jornada Completa"
        2 -> "Jornada Parcial"
        3 -> "Jornada Específica"
        else -> "Jornada Parcial"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (canManageStaff) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
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
                UserAvatar(
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

                val (statusText, statusBg, statusTextColor) = when (selectedAbsence?.type) {
                    AbsenceType.VACATION -> Triple("Vacances", Color(0xFFFFF3E0), Color(0xFFF57C00))
                    AbsenceType.HOLIDAY -> Triple("Dia Festiu", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                    AbsenceType.SICK_LEAVE -> Triple("Baixa Mèdica", Color(0xFFE3F2FD), Color(0xFF1976D2))
                    null -> Triple(scheduleText, Color(0xFFE8F5E9), Color(0xFF2E7D32))
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
fun ReadOnlyClinicalInfoCard(
    availabilityDto: DentistAvailabilityDTO?,
    allTreatments: List<Treatment>
) {
    val assignedTreatments = allTreatments.filter { availabilityDto?.treatmentIds?.contains(it.id) == true }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tractaments Assignats:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))

            if (assignedTreatments.isEmpty()) {
                Text("Cap tractament assignat actualment.", fontSize = 13.sp, color = Color.Gray)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assignedTreatments) { treatment ->
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ButtonPrimary)
                        ) {
                            Text(
                                text = treatment.name ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ButtonPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun detectScheduleTypeLocal(dto: DentistAvailabilityDTO): Int {
    val isFull = dto.mondayMorningActive && dto.mondayAfternoonActive &&
            dto.tuesdayMorningActive && dto.tuesdayAfternoonActive &&
            dto.wednesdayMorningActive && dto.wednesdayAfternoonActive &&
            dto.thursdayMorningActive && dto.thursdayAfternoonActive &&
            dto.fridayMorningActive && dto.fridayAfternoonActive &&
            dto.mondayMorningStart == "09:00" && dto.mondayMorningEnd == "14:00" &&
            dto.mondayAfternoonStart == "16:00" && dto.mondayAfternoonEnd == "20:00" &&
            dto.tuesdayMorningStart == "09:00" && dto.tuesdayMorningEnd == "14:00" &&
            dto.tuesdayAfternoonStart == "16:00" && dto.tuesdayAfternoonEnd == "20:00" &&
            dto.wednesdayMorningStart == "09:00" && dto.wednesdayMorningEnd == "14:00" &&
            dto.wednesdayAfternoonStart == "16:00" && dto.wednesdayAfternoonEnd == "20:00" &&
            dto.thursdayMorningStart == "09:00" && dto.thursdayMorningEnd == "14:00" &&
            dto.thursdayAfternoonStart == "16:00" && dto.thursdayAfternoonEnd == "20:00" &&
            dto.fridayMorningStart == "09:00" && dto.fridayMorningEnd == "14:00" &&
            dto.fridayAfternoonStart == "16:00" && dto.fridayAfternoonEnd == "20:00"
    if (isFull) return 1

    if (dto.mondayEveningActive || dto.tuesdayEveningActive || dto.wednesdayEveningActive || dto.thursdayEveningActive || dto.fridayEveningActive) return 3

    val checkCustom = { active: Boolean, start: String?, end: String?, stdStart: String, stdEnd: String ->
        active && (start != stdStart || end != stdEnd)
    }

    val isCustom = checkCustom(dto.mondayMorningActive, dto.mondayMorningStart, dto.mondayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.mondayAfternoonActive, dto.mondayAfternoonStart, dto.mondayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.tuesdayMorningActive, dto.tuesdayMorningStart, dto.tuesdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.tuesdayAfternoonActive, dto.tuesdayAfternoonStart, dto.tuesdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.wednesdayMorningActive, dto.wednesdayMorningStart, dto.wednesdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.wednesdayAfternoonActive, dto.wednesdayAfternoonStart, dto.wednesdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.thursdayMorningActive, dto.thursdayMorningStart, dto.thursdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.thursdayAfternoonActive, dto.thursdayAfternoonStart, dto.thursdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.fridayMorningActive, dto.fridayMorningStart, dto.fridayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.fridayAfternoonActive, dto.fridayAfternoonStart, dto.fridayAfternoonEnd, "15:00", "20:00")

    if (isCustom) return 3

    return 2
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
    showAdvancedActions: Boolean,
    canManageStaff: Boolean,
    roles: List<String>,
    onDoctorAgendaClick: () -> Unit,
    onDoctorPatientsClick: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAvailability: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showAdvancedActions) {
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

        if (canManageStaff && (roles.contains("DOCTOR") || roles.contains("DENTIST") || roles.contains("ROLE_DOCTOR") || roles.contains("ROLE_DENTIST"))) {
            StaffActionCard(
                title = "Disponibilitat i Tractaments",
                icon = Icons.Default.Settings,
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToAvailability
            )
        }

        StaffActionCard(
            title = "Xat Intern",
            icon = Icons.AutoMirrored.Filled.Chat,
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToChat
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
    absences: List<AbsenceEvent>,
    doctorAppointments: List<Appointment> = emptyList(),
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = LocalDate.now()

    val locale = Locale.forLanguageTag("ca")
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    val monthYearText = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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

            val offset = currentMonth.atDay(1).dayOfWeek.value - 1
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
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
                                val dayAbsence = absences.find { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }
                                val isToday = date == today

                                val hasAppointments = doctorAppointments.any { appt ->
                                    appt.startTime?.startsWith(date.toString()) == true
                                }

                                val bgColor = when {
                                    isSelected -> Color(0xFF0D47A1)
                                    dayAbsence?.type == AbsenceType.VACATION -> Color(0xFFFFB300)
                                    dayAbsence?.type == AbsenceType.HOLIDAY -> Color(0xFFE53935)
                                    dayAbsence?.type == AbsenceType.SICK_LEAVE -> Color(0xFF039BE5)
                                    isToday -> Color(0xFFEFF6FF)
                                    else -> Color.Transparent
                                }

                                val textColor = when {
                                    isSelected -> Color.White
                                    dayAbsence != null -> Color.White
                                    isToday -> Color(0xFF0D47A1)
                                    else -> Color(0xFF1E293B)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
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
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 11.sp,
                                            color = textColor,
                                            fontWeight = if (isSelected || dayAbsence != null) FontWeight.Bold else FontWeight.Normal
                                        )

                                        // Punto indicador para días con citas
                                        if (hasAppointments && !isSelected && dayAbsence == null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0D47A1))
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
    }
}

@Composable
private fun StaffAttendanceCardForDate(
    selectedDate: LocalDate,
    selectedAbsence: AbsenceEvent?
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val (icon, color, bgColor, titleText, subtitleText) = when (selectedAbsence?.type) {
        AbsenceType.VACATION -> Quintuple(
            Icons.Default.FlightTakeoff,
            Color(0xFFF57C00),
            Color(0xFFFFF3E0),
            "Vacances",
            "Dia de vacances registrat"
        )
        AbsenceType.HOLIDAY -> Quintuple(
            Icons.Default.Celebration,
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            "Dia Festiu",
            "Festiu oficial"
        )
        AbsenceType.SICK_LEAVE -> Quintuple(
            Icons.Default.LocalHospital,
            Color(0xFF1976D2),
            Color(0xFFE3F2FD),
            "Baixa Mèdica",
            "Absència per motius de salut"
        )
        null -> Quintuple(
            Icons.Default.Schedule,
            Color(0xFF64748B),
            Color(0xFFF1F5F9),
            "Jornada Laboral",
            "Fitxatge / Jornada habitual"
        )
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
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Data: ${selectedDate.format(dateFormatter)}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun DoctorAppointmentProfileCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val startLabel = formatTimeLocal(appointment.startTime)
    val endLabel = run {
        val startMin = parseTimeToMinutesLocal(appointment.startTime)
        val duration = appointment.treatment?.durationMinutes
        if (startMin != null && duration != null) {
            val endMin = startMin + duration
            "%02d:%02d".format(endMin / 60, endMin % 60)
        } else {
            formatTimeLocal(appointment.endTime)
        }
    }
    val patientName = "${appointment.patient?.name ?: ""} ${appointment.patient?.lastName ?: ""}".trim()
    val treatmentName = appointment.treatment?.name ?: "Tractament"
    val boxInfo = appointment.box?.number?.let { "Box $it" } ?: ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEBF4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = patientName.ifBlank { "Pacient desconegut" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (boxInfo.isNotEmpty()) "$startLabel - $endLabel | $boxInfo" else "$startLabel - $endLabel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ButtonPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = treatmentName,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Veure resum",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun extractTimeOnlyLocal(dateTimeString: String?): String? {
    if (dateTimeString == null) return null
    return if (dateTimeString.contains("T")) {
        dateTimeString.split("T").lastOrNull()
    } else {
        dateTimeString.split(" ").lastOrNull()
    }
}

private fun parseTimeToMinutesLocal(time: String?): Int? {
    val cleanTime = extractTimeOnlyLocal(time) ?: return null
    return try {
        val parts = cleanTime.split(":")
        parts[0].toInt() * 60 + parts[1].toInt()
    } catch (e: Exception) { null }
}

private fun formatTimeLocal(time: String?): String {
    val cleanTime = extractTimeOnlyLocal(time) ?: return ""
    return try {
        val parts = cleanTime.split(":")
        "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) { cleanTime }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)