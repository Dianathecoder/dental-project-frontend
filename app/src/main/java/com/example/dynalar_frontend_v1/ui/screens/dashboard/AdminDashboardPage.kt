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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

@Composable
fun AdminDashboardPage(
    viewModel: AppointmentViewModel = viewModel(),
    onNavigateProfileUserProfile: () -> Unit,
    onNavigatePatients: () -> Unit,
    onNavigateBoxCalendar: () -> Unit,
    onNavigateManagement: () -> Unit,
    onNavigateStaff: () -> Unit,
    onNavigateAttendance: () -> Unit,
    onNavigateToAppointmentDetail: (Appointment) -> Unit,
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")

    LaunchedEffect(Unit) {
        viewModel.fetchToday()
    }

    var selectedDateForDialog by remember { mutableStateOf<LocalDate?>(null) }

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

            // 1. Cabecera (Avatar, Rol, Selector de Idioma)
            Header_HomePage(
                onNavigateProfileUserProfile = onNavigateProfileUserProfile,
                onLanguageChange = onLanguageChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            val uiState = viewModel.uiStateToday
            var citasHoyCount = 0
            if (uiState is InterfaceGlobal.Success) {
                citasHoyCount = uiState.data.size
            }

            // 2. Saludo con resumen de citas de hoy
            GreetingSection(citasHoy = citasHoyCount)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Calendario
            CalendarHomepage(
                viewModel = viewModel,
                onDayClick = { date ->
                    viewModel.fetchDayDetails(date)
                    selectedDateForDialog = date
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Parrilla de botones adaptada
            Buttons_AdminDashboard(
                isSuperAdminOrOwner = isSuperAdmin || isOwner,
                onNavigatePatients = onNavigatePatients,
                onNavigateBoxCalendar = onNavigateBoxCalendar,
                onNavigateManagement = onNavigateManagement,
                onNavigateStaff = onNavigateStaff,
                onNavigateAttendance = onNavigateAttendance
            )
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
fun Buttons_AdminDashboard(
    modifier: Modifier = Modifier,
    isSuperAdminOrOwner: Boolean,
    onNavigatePatients: () -> Unit,
    onNavigateBoxCalendar: () -> Unit,
    onNavigateManagement: () -> Unit,
    onNavigateStaff: () -> Unit,
    onNavigateAttendance: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FILA 1: Pacientes y Agenda
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

        // FILA 2: Gestión Clínica y Equip de la Clínica
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardMenuButton(
                icon = Icons.Default.Inventory,
                title = stringResource(id = R.string.home_btn_management),
                onClick = onNavigateManagement,
                modifier = Modifier.weight(1f)
            )
            CardMenuButton(
                icon = Icons.Default.Badge,
                title = stringResource(id = R.string.dashboard_btn_staff),
                onClick = onNavigateStaff,
                modifier = Modifier.weight(1f)
            )
        }

        // FILA 3: Control de Fichajes (SOLO VISIBLE PARA SUPERADMIN Y OWNER)
        if (isSuperAdminOrOwner) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CardMenuButton(
                    icon = Icons.Default.AccessTime,
                    title = stringResource(id = R.string.dashboard_btn_attendance),
                    onClick = onNavigateAttendance,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}