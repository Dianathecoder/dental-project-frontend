package com.example.dynalar_frontend_v1.ui.screens.staff

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.staff.AbsenceEvent
import com.example.dynalar_frontend_v1.model.staff.AbsenceType
import com.example.dynalar_frontend_v1.model.filter.StaffRoleFilter
import com.example.dynalar_frontend_v1.ui.components.AbsencesCalendarView
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.StaffRoleFilterDropdown
import com.example.dynalar_frontend_v1.ui.components.UserAvatar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.StaffControlViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceCalendarScreen(
    viewModel: StaffControlViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val searchState = rememberTextFieldState()
    var selectedRoleFilter by remember { mutableStateOf(StaffRoleFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    var selectedEmployees by remember { mutableStateOf(setOf<Long>()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(currentMonth) {
        viewModel.fetchMonthlyAbsences(currentMonth)
        userViewModel.getAllStaff()
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, _ ->
                currentMonth = YearMonth.of(year, month + 1)
                showDatePicker = false
            },
            currentMonth.year,
            currentMonth.monthValue - 1,
            1
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
                    CustomTopBar(title = stringResource(R.string.menu_absence_calendar_title), onNavigateBack = onNavigateBack)

                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Cambiar mes", tint = ButtonPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchStaffBar(textFieldState = searchState)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val sessionManager = remember { SessionManager(context) }
                val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")

                StaffRoleFilterDropdown(
                    selectedRoleFilter = selectedRoleFilter,
                    sortAscending = sortAscending,
                    isSuperAdmin = isSuperAdmin,
                    onRoleFilterChanged = { selectedRoleFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = viewModel.uiStateAbsences) {
                is InterfaceGlobal.Loading -> {
                    Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }
                is InterfaceGlobal.Success -> {
                    val query = searchState.text.toString().trim()

                    val staffList = userViewModel.staffList.filter { staff ->
                        val fullName = "${staff.name} ${staff.surname}"
                        if (query.isBlank()) true else fullName.contains(query, ignoreCase = true)
                    }.filter { staff ->
                        val roleStr = staff.roles.joinToString(",").uppercase()
                        when (selectedRoleFilter) {
                            StaffRoleFilter.ALL -> true
                            StaffRoleFilter.SUPERADMIN -> roleStr.contains("SUPERADMIN")
                            StaffRoleFilter.OWNER -> roleStr.contains("OWNER")
                            StaffRoleFilter.ADMIN -> roleStr.contains("ADMIN")
                            StaffRoleFilter.DOCTOR -> roleStr.contains("DOCTOR") || roleStr.contains("DENTIST")
                            StaffRoleFilter.AUXILIAR -> roleStr.contains("AUXILIAR")
                        }
                    }.let { list ->
                        if (sortAscending) list.sortedBy { it.name?.uppercase() }
                        else list.sortedByDescending { it.name?.uppercase() }
                    }

                    // 👉 SI NO HAY NADA SELECCIONADO, LA LISTA DE NOMBRES ESTÁ VACÍA Y NO SE PINTA NADA 👈
                    val activeNamesFilter = if (selectedEmployees.isNotEmpty()) {
                        staffList.filter { it.id in selectedEmployees }
                            .map { "${it.name} ${it.surname}".trim() }
                    } else {
                        emptyList()
                    }

                    val calendarEvents = state.data.map { dto ->
                        AbsenceEvent(
                            id = dto.id,
                            title = dto.title,
                            staffName = dto.staffName,
                            type = try {
                                AbsenceType.valueOf(dto.type.uppercase())
                            } catch (e: Exception) {
                                AbsenceType.HOLIDAY
                            },
                            startDate = LocalDate.parse(dto.startDate),
                            endDate = LocalDate.parse(dto.endDate)
                        )
                    }.filter { event ->
                        event.staffName?.trim() in activeNamesFilter
                    }

                    AbsencesCalendarView(
                        currentMonth = currentMonth,
                        events = calendarEvents,
                        onMonthChange = { currentMonth = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.employees_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (staffList.isNotEmpty() && selectedEmployees.size < staffList.size) {
                                TextButton(onClick = {
                                    selectedEmployees = staffList.mapNotNull { it.id }.toSet()
                                }) {
                                    Text(stringResource(R.string.select_all), color = ButtonPrimary)
                                }
                            }

                            if (selectedEmployees.isNotEmpty()) {
                                TextButton(onClick = { selectedEmployees = emptySet() }) {
                                    Text(
                                        stringResource(R.string.clear_selection),
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (staffList.isEmpty()) {
                        Text(
                            stringResource(R.string.no_records_found),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(staffList, key = { it.id ?: 0L }) { staff ->
                                val isSelected = selectedEmployees.contains(staff.id)

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0F7FF) else Color.White),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ButtonPrimary else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedEmployees = if (isSelected) {
                                            selectedEmployees - (staff.id ?: 0L)
                                        } else {
                                            selectedEmployees + (staff.id ?: 0L)
                                        }
                                    }
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

                                        UserAvatar(
                                            avatarUrl = staff.avatarUrl,
                                            userId = staff.id,
                                            sexRaw = staff.sex,
                                            rolesRaw = staff.roles,
                                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${staff.name} ${staff.surname}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Text(staff.roles.firstOrNull()?.uppercase() ?: "", fontSize = 12.sp, color = Color.Gray)
                                        }

                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = ButtonPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Error", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}