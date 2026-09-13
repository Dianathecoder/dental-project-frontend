package com.example.dynalar_frontend_v1.ui.screens.staff

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.staff.*
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.StaffControlViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DailyAttendanceScreen(
    viewModel: StaffControlViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val searchState = rememberTextFieldState()
    var selectedRoleFilter by remember { mutableStateOf(StaffRoleFilter.ALL) }
    var selectedClockFilter by remember { mutableStateOf(StaffClockFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    var selectedEmployeeForSchedule by remember { mutableStateOf<AttendanceEntry?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Verificamos los roles del usuario que está viendo la pantalla
    val sessionManager = remember { SessionManager(context) }
    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")
    val isAdmin = sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN")

    LaunchedEffect(selectedDate) {
        viewModel.fetchDailyAttendance(selectedDate)
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePicker = false
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).apply {
            setOnDismissListener { showDatePicker = false }
            show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(27.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    CustomTopBar(title = stringResource(R.string.menu_daily_attendance_title), onNavigateBack = onNavigateBack)
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = ButtonPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            DateHeaderCard(
                selectedDate = selectedDate,
                onPreviousDay = { selectedDate = selectedDate.minusDays(1) },
                onNextDay = { selectedDate = selectedDate.plusDays(1) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            SearchStaffBar(textFieldState = searchState)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                DailyAttendanceFilterDropdown(
                    selectedRoleFilter = selectedRoleFilter,
                    selectedClockFilter = selectedClockFilter,
                    sortAscending = sortAscending,
                    onRoleFilterChanged = { selectedRoleFilter = it },
                    onClockFilterChanged = { selectedClockFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = viewModel.uiStateAttendance) {
                is InterfaceGlobal.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ButtonPrimary) }
                }
                is InterfaceGlobal.Success -> {
                    val query = searchState.text.toString().trim()

                    val attendanceList = state.data.map { dto ->
                        AttendanceEntry(
                            id = dto.id,
                            staffName = dto.staffName,
                            role = dto.role,
                            roles = dto.roles,
                            sex = dto.sex,
                            date = LocalDate.parse(dto.date),
                            checkInTime = dto.checkInTime?.let { LocalTime.parse(it) },
                            checkOutTime = dto.checkOutTime?.let { LocalTime.parse(it) }
                        )
                    }.filter { entry ->
                        // LÓGICA DE JERARQUÍA (Ocultar jefes a los admins)
                        val rolesStr = entry.roles.joinToString(" ").uppercase()
                        when {
                            isSuperAdmin || isOwner -> true // Superadmin y Owner ven a todo el mundo
                            isAdmin -> !rolesStr.contains("SUPERADMIN") && !rolesStr.contains("OWNER") // Admins ven al resto
                            else -> false
                        }
                    }.filter { entry ->
                        if (query.isBlank()) true else entry.staffName.contains(query, ignoreCase = true)
                    }.filter { entry ->
                        val roleUpper = entry.role.uppercase()
                        when (selectedRoleFilter) {
                            StaffRoleFilter.ALL -> true
                            StaffRoleFilter.OWNER -> roleUpper.contains("OWNER")
                            StaffRoleFilter.ADMIN -> roleUpper.contains("ADMIN")
                            StaffRoleFilter.DOCTOR -> roleUpper.contains("DOCTOR") || roleUpper.contains("DENTIST")
                            StaffRoleFilter.AUXILIAR -> roleUpper.contains("AUXILIAR")
                        }
                    }.filter { entry ->
                        when (selectedClockFilter) {
                            StaffClockFilter.ALL -> true
                            StaffClockFilter.CLOCKED_IN -> entry.status == AttendanceStatusType.ON_TIME || entry.status == AttendanceStatusType.LATE_CLOCKED
                            StaffClockFilter.NOT_CLOCKED -> entry.status == AttendanceStatusType.PENDING || entry.status == AttendanceStatusType.ABSENT_RED
                            StaffClockFilter.LATE_WARNING -> entry.status == AttendanceStatusType.LATE_CLOCKED || entry.status == AttendanceStatusType.ABSENT_RED
                        }
                    }.let { list ->
                        if (sortAscending) list.sortedBy { it.staffName.uppercase() }
                        else list.sortedByDescending { it.staffName.uppercase() }
                    }

                    if (attendanceList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_records_found), color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(attendanceList, key = { it.id }) { entry ->
                                Box(modifier = Modifier.clickable { selectedEmployeeForSchedule = entry }) {
                                    AttendanceEntryCard(entry = entry)
                                }
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Error", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }

    selectedEmployeeForSchedule?.let { employee ->
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm'h'")
        AlertDialog(
            onDismissRequest = { selectedEmployeeForSchedule = null },
            title = {
                Text(text = stringResource(R.string.schedule_of_title, employee.staffName), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(stringResource(R.string.schedule_expected, employee.expectedTime.format(timeFormatter)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rol: ${employee.role}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEmployeeForSchedule = null }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}