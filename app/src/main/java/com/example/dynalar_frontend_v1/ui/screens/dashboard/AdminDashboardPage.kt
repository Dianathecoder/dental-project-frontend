package com.example.dynalar_frontend_v1.ui.screens.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.CardMenuButton
import com.example.dynalar_frontend_v1.ui.components.DayAppointmentsDialog
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardPage(
    viewModel: AppointmentViewModel = viewModel(),
    onNavigateProfileUserProfile: () -> Unit,
    onNavigatePatients: () -> Unit,
    onNavigateBoxCalendar: () -> Unit,
    onNavigateManagement: () -> Unit, // Para Box y Stock
    onNavigateClinical: () -> Unit,   // Para Tratamientos y Doctores
    onNavigateStaff: () -> Unit,
    onNavigateAttendance: () -> Unit,
    onNavigateToClockInDirect: () -> Unit,
    onNavigateToAppointmentDetail: (Appointment) -> Unit,
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")
    val isAdmin = sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")
    val isDoctor = sessionManager.hasRole("DOCTOR") || sessionManager.hasRole("ROLE_DOCTOR") || sessionManager.hasRole("DENTIST") || sessionManager.hasRole("ROLE_DENTIST")

    val isManagementRole = isSuperAdmin || isOwner || isAdmin
    val showNextAppointment = isSuperAdmin || isOwner || isAdmin || isDoctor

    LaunchedEffect(Unit) {
        viewModel.fetchToday()
    }

    var selectedDateForDialog by remember { mutableStateOf<LocalDate?>(null) }
    var showClinicalMenu by remember { mutableStateOf(false) }
    var showHRMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val uiState = viewModel.uiStateToday
    val nowTime = LocalTime.now()
    var citasHoyCount = 0
    var nextAppointments = emptyList<Appointment>()

    // Cálculo de citas de hoy y la próxima cita (idéntico al HomePage)
    if (uiState is InterfaceGlobal.Success) {
        val todayAppointments = uiState.data
        citasHoyCount = todayAppointments.size

        if (todayAppointments.isNotEmpty() && showNextAppointment) {
            val groupedByTime = todayAppointments.groupBy { appt ->
                appt.startTime?.replace("T", " ")?.split(" ")?.lastOrNull()?.take(5) ?: "23:59"
            }

            val bestGroup = groupedByTime.minByOrNull { (timeStr, _) ->
                try {
                    val parts = timeStr.split(":")
                    val apptMinutes = parts[0].toInt() * 60 + parts[1].toInt()
                    val currentMinutes = nowTime.hour * 60 + nowTime.minute

                    if (apptMinutes >= currentMinutes) {
                        apptMinutes - currentMinutes
                    } else {
                        10000 + (currentMinutes - apptMinutes)
                    }
                } catch (e: Exception) {
                    99999
                }
            }
            nextAppointments = bestGroup?.value ?: emptyList()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPagina)
    ) {
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .heightIn(min = screenHeight)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Header_HomePage(
                onNavigateProfileUserProfile = onNavigateProfileUserProfile,
                onLanguageChange = onLanguageChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            GreetingSection(citasHoy = citasHoyCount)

            Spacer(modifier = Modifier.height(16.dp))

            CalendarHomepage(
                viewModel = viewModel,
                onDayClick = { date ->
                    viewModel.fetchDayDetails(date)
                    selectedDateForDialog = date
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Grid de 4 Botones
            Buttons_AdminDashboard_Simplified(
                onNavigatePatients = onNavigatePatients,
                onNavigateBoxCalendar = onNavigateBoxCalendar,
                onOpenClinicalMenu = { showClinicalMenu = true },
                onOpenHRMenu = { showHRMenu = true }
            )

            // Próxima Cita (Solo visible para los roles permitidos)
            if (showNextAppointment) {
                Spacer(modifier = Modifier.height(32.dp))
                NextAppointmentSection(
                    isLoading = uiState is InterfaceGlobal.Loading,
                    nextAppointments = nextAppointments,
                    onAppointmentClick = { appointment -> onNavigateToAppointmentDetail(appointment) }
                )
            }
        }
    }

    // --- MENÚS DESPLEGABLES (BOTTOM SHEETS) ---

    if (showClinicalMenu) {
        ModalBottomSheet(
            onDismissRequest = { showClinicalMenu = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text("Gestió de Clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                Spacer(modifier = Modifier.height(16.dp))

                CardMenuButton(
                    icon = Icons.Default.Inventory,
                    title = "Logística (Materials)",
                    onClick = { showClinicalMenu = false; onNavigateManagement() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                CardMenuButton(
                    icon = Icons.Default.MedicalServices,
                    title = "Àrea Clínica",
                    onClick = { showClinicalMenu = false; onNavigateClinical() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showHRMenu) {
        ModalBottomSheet(
            onDismissRequest = { showHRMenu = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text("Recursos Humans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                Spacer(modifier = Modifier.height(16.dp))

                CardMenuButton(
                    icon = Icons.Default.Badge,
                    title = stringResource(id = R.string.dashboard_btn_staff),
                    onClick = { showHRMenu = false; onNavigateStaff() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                CardMenuButton(
                    icon = Icons.Default.AccessTime,
                    title = stringResource(id = R.string.dashboard_btn_attendance),
                    onClick = {
                        showHRMenu = false
                        if (isManagementRole) onNavigateAttendance() else onNavigateToClockInDirect()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (selectedDateForDialog != null) {
        val detailUiState = viewModel.uiStateCalendar
        val appointments = if (detailUiState is InterfaceGlobal.Success) detailUiState.data else emptyList()

        DayAppointmentsDialog(
            date = selectedDateForDialog!!,
            appointments = appointments,
            isLoading = detailUiState is InterfaceGlobal.Loading,
            onDismiss = { selectedDateForDialog = null },
            onAppointmentClick = { appointment -> onNavigateToAppointmentDetail(appointment) }
        )
    }
}

@Composable
fun Buttons_AdminDashboard_Simplified(
    modifier: Modifier = Modifier,
    onNavigatePatients: () -> Unit,
    onNavigateBoxCalendar: () -> Unit,
    onOpenClinicalMenu: () -> Unit,
    onOpenHRMenu: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardMenuButton(
                icon = Icons.Default.Person,
                title = stringResource(id = R.string.home_btn_patients),
                onClick = onNavigatePatients,
                modifier = Modifier.weight(1f)
            )
            CardMenuButton(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(id = R.string.home_btn_agenda),
                onClick = onNavigateBoxCalendar,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardMenuButton(
                icon = Icons.Default.LocalHospital,
                title = "Gestió de Clínica",
                onClick = onOpenClinicalMenu,
                modifier = Modifier.weight(1f)
            )
            CardMenuButton(
                icon = Icons.Default.Badge,
                title = "Recursos Humans",
                onClick = onOpenHRMenu,
                modifier = Modifier.weight(1f)
            )
        }
    }
}