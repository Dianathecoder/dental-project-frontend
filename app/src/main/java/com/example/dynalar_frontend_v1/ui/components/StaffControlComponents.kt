package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.staff.*
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import androidx.compose.runtime.Composable
import com.example.dynalar_frontend_v1.ui.components.getStaffImage

@Composable
fun AbsencesCalendarView(currentMonth: YearMonth, events: List<AbsenceEvent>, onMonthChange: (YearMonth) -> Unit) {
    val weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    val firstDay = currentMonth.atDay(1)
    val offset = firstDay.dayOfWeek.value - 1
    val daysInMonth = currentMonth.lengthOfMonth()

    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val headerText = currentMonth.format(formatter).replaceFirstChar { it.uppercase() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectHorizontalDragGestures(onDragEnd = { if (offsetX > 50) onMonthChange(currentMonth.minusMonths(1)) else if (offsetX < -50) onMonthChange(currentMonth.plusMonths(1)); offsetX = 0f }) { change, dragAmount -> change.consume(); offsetX += dragAmount } }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(headerText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Row {
                    IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior", tint = Color.Gray) }
                    IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Següent", tint = Color.Gray) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day -> Text(day.getDisplayName(TextStyle.NARROW, locale), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val day = cellIndex - offset + 1
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (day in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(day)
                                val dayEvents = events.filter { !cellDate.isBefore(it.startDate) && !cellDate.isAfter(it.endDate) }
                                val bgColor = when {
                                    dayEvents.any { it.type == AbsenceType.HOLIDAY } -> Color(0xFFFFEBEE)
                                    dayEvents.any { it.type == AbsenceType.VACATION } -> Color(0xFFFFF3E0)
                                    dayEvents.any { it.type == AbsenceType.SICK_LEAVE } -> Color(0xFFE3F2FD)
                                    else -> Color.Transparent
                                }
                                val textColor = if (dayEvents.isNotEmpty()) Color.Black else Color(0xFF1E293B)
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
                                    Text(day.toString(), fontSize = 14.sp, color = textColor, fontWeight = if (dayEvents.isNotEmpty()) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LegendItem(Color(0xFFFFF3E0), Color(0xFFF57C00), stringResource(R.string.legend_vacation))
                LegendItem(Color(0xFFFFEBEE), Color(0xFFD32F2F), stringResource(R.string.legend_holiday))
                LegendItem(Color(0xFFE3F2FD), Color(0xFF1976D2), stringResource(R.string.legend_sick_leave))
            }
        }
    }
}

@Composable
fun LegendItem(bgColor: Color, dotColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun AbsenceEventCard(event: AbsenceEvent) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("dd MMM", locale) }
    val dateStr = if (event.startDate == event.endDate) event.startDate.format(formatter) else "${event.startDate.format(formatter)} - ${event.endDate.format(formatter)}"
    val (icon, color) = when (event.type) {
        AbsenceType.VACATION -> Icons.Default.FlightTakeoff to Color(0xFFF57C00)
        AbsenceType.HOLIDAY -> Icons.Default.Celebration to Color(0xFFD32F2F)
        AbsenceType.SICK_LEAVE -> Icons.Default.LocalHospital to Color(0xFF1976D2)
    }
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                if (event.staffName != null) { Text(text = event.staffName, color = Color.Gray, fontSize = 13.sp) }
            }
            Text(text = dateStr, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
        }
    }
}

@Composable
fun DateHeaderCard(selectedDate: LocalDate, onPreviousDay: () -> Unit, onNextDay: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", locale) }
    val formattedDate = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDay) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior", tint = Color(0xFF0D47A1)) }
            Text(text = formattedDate, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            IconButton(onClick = onNextDay) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Següent", tint = Color(0xFF0D47A1)) }
        }
    }
}

@Composable
fun AttendanceEntryCard(entry: AttendanceEntry) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm'h'")
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val avatarRes = getStaffImage(entry.id, entry.roles, entry.sex)
    val bgColor: Color
    val borderColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val iconTint: Color
    val titleText: String
    val isTimeRed: Boolean

    when (entry.status) {
        AttendanceStatusType.ON_TIME -> {
            bgColor = Color.White; borderColor = Color(0xFFE2E8F0); icon = Icons.Default.CheckCircle; iconTint = Color(0xFF2E7D32); titleText = stringResource(R.string.status_clocked_on_time); isTimeRed = false
        }
        AttendanceStatusType.LATE_CLOCKED -> {
            bgColor = Color(0xFFFFF5F5); borderColor = Color(0xFFEF5350); icon = Icons.Default.Warning; iconTint = Color(0xFFD32F2F); titleText = stringResource(R.string.status_clocked_late); isTimeRed = true
        }
        AttendanceStatusType.ABSENT_RED -> {
            bgColor = Color(0xFFFFF5F5); borderColor = Color(0xFFEF5350); icon = Icons.Default.Error; iconTint = Color(0xFFD32F2F); titleText = stringResource(R.string.status_not_clocked); isTimeRed = true
        }
        AttendanceStatusType.PENDING -> {
            bgColor = Color.White; borderColor = Color(0xFFE2E8F0); icon = Icons.Default.Schedule; iconTint = Color(0xFF64748B); titleText = stringResource(R.string.status_pending); isTimeRed = false
        }
    }
    val noTimeText = stringResource(R.string.no_time)
    val checkInStr = entry.checkInTime?.format(timeFormatter) ?: noTimeText
    val checkOutStr = entry.checkOutTime?.format(timeFormatter) ?: noTimeText

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (avatarRes is String) {
                coil.compose.AsyncImage(
                    model = avatarRes,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(55.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (avatarRes is Int) {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(55.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = entry.staffName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Text(text = entry.role, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0D47A1))
                Spacer(modifier = Modifier.height(8.dp))

                // BLOQUE DE HORAS (Entrada y Salida)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.clock_in_label, checkInStr), fontSize = 13.sp, fontWeight = if (isTimeRed) FontWeight.Bold else FontWeight.Medium, color = if (isTimeRed) Color(0xFFD32F2F) else Color(0xFF475569))
                    Text(text = "  |  ", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                    Text(text = stringResource(R.string.clock_out_label, checkOutStr), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                }
            }
        }
    }
}

// Función para traducir los Roles automáticamente
@Composable
fun StaffRoleFilter.getLocalizedName(): String {
    return when (this) {
        StaffRoleFilter.ALL -> stringResource(R.string.filter_all_staff)
        StaffRoleFilter.OWNER -> stringResource(R.string.filter_owners)
        StaffRoleFilter.ADMIN -> stringResource(R.string.filter_admins)
        StaffRoleFilter.DOCTOR -> stringResource(R.string.filter_doctors)
        StaffRoleFilter.AUXILIAR -> stringResource(R.string.filter_auxiliars)
    }
}

// Función para traducir los Estados de Fichaje automáticamente
@Composable
fun StaffClockFilter.getLocalizedName(): String {
    return when (this) {
        StaffClockFilter.ALL -> stringResource(R.string.filter_status_all)
        StaffClockFilter.CLOCKED_IN -> stringResource(R.string.filter_status_clocked_in)
        StaffClockFilter.NOT_CLOCKED -> stringResource(R.string.filter_status_not_clocked)
        StaffClockFilter.LATE_WARNING -> stringResource(R.string.status_clocked_late)
    }
}

// Filtro completo para el Registro Diario
@Composable
fun DailyAttendanceFilterDropdown(
    selectedRoleFilter: StaffRoleFilter,
    selectedClockFilter: StaffClockFilter?,
    sortAscending: Boolean,
    onRoleFilterChanged: (StaffRoleFilter) -> Unit,
    onClockFilterChanged: ((StaffClockFilter) -> Unit)? = null,
    onSortChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilter = selectedRoleFilter != StaffRoleFilter.ALL || (selectedClockFilter != null && selectedClockFilter != StaffClockFilter.ALL)

    val label = if (hasFilter) stringResource(R.string.filter_active) else stringResource(R.string.filter_title)
    val activeColor = ButtonPrimary
    val inactiveIconTextColor = Color.Gray

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = if (hasFilter) activeColor else Color.Transparent,
            border = BorderStroke(1.5.dp, if (hasFilter) activeColor else Color(0xFFA0B2C0)),
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (hasFilter) Color.White else inactiveIconTextColor)
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (hasFilter) Color.White else inactiveIconTextColor)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(230.dp).background(Color.White)) {
            Text(stringResource(R.string.filter_order_header), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_order_asc), fontSize = 14.sp) }, onClick = { onSortChanged(true); expanded = false }, trailingIcon = { if (sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_order_desc), fontSize = 14.sp) }, onClick = { onSortChanged(false); expanded = false }, trailingIcon = { if (!sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))
            Text(stringResource(R.string.filter_role_header), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            StaffRoleFilter.values().forEach { filterOption ->
                DropdownMenuItem(text = { Text(filterOption.getLocalizedName(), fontSize = 14.sp) }, onClick = { onRoleFilterChanged(filterOption); expanded = false }, trailingIcon = { if (selectedRoleFilter == filterOption) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })
            }

            if (onClockFilterChanged != null && selectedClockFilter != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))
                Text(stringResource(R.string.filter_status_header), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                StaffClockFilter.values().forEach { filterOption ->
                    DropdownMenuItem(text = { Text(filterOption.getLocalizedName(), fontSize = 14.sp) }, onClick = { onClockFilterChanged(filterOption); expanded = false }, trailingIcon = { if (selectedClockFilter == filterOption) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })
                }
            }
        }
    }
}

// Filtro reducido (Solo Rol y Ordenación) para el Calendario de Ausencias
@Composable
fun StaffRoleFilterDropdown(
    selectedRoleFilter: StaffRoleFilter,
    sortAscending: Boolean,
    onRoleFilterChanged: (StaffRoleFilter) -> Unit,
    onSortChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilter = selectedRoleFilter != StaffRoleFilter.ALL

    val label = if (hasFilter) stringResource(R.string.filter_active) else stringResource(R.string.filter_title)
    val activeColor = ButtonPrimary
    val inactiveIconTextColor = Color.Gray

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = if (hasFilter) activeColor else Color.Transparent,
            border = BorderStroke(1.5.dp, if (hasFilter) activeColor else Color(0xFFA0B2C0)),
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (hasFilter) Color.White else inactiveIconTextColor)
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (hasFilter) Color.White else inactiveIconTextColor)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(230.dp).background(Color.White)) {
            Text(stringResource(R.string.filter_order_header), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_order_asc), fontSize = 14.sp) }, onClick = { onSortChanged(true); expanded = false }, trailingIcon = { if (sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_order_desc), fontSize = 14.sp) }, onClick = { onSortChanged(false); expanded = false }, trailingIcon = { if (!sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) })

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))
            Text(stringResource(R.string.filter_role_header), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            StaffRoleFilter.values().forEach { filterOption ->
                DropdownMenuItem(
                    text = { Text(filterOption.getLocalizedName(), fontSize = 14.sp) },
                    onClick = { onRoleFilterChanged(filterOption); expanded = false },
                    trailingIcon = { if (selectedRoleFilter == filterOption) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}
