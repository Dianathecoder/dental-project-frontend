package com.example.dynalar_frontend_v1.ui.screens.staff

import android.app.DatePickerDialog
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.TreatmentColors
import com.example.dynalar_frontend_v1.ui.screens.appointment.ResumeDateScreen
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val SLOT_HEIGHT_DP = 80
private const val DAY_START_HOUR = 8
private const val TOTAL_HOURS = 14
private const val TOP_MARGIN_DP = 16

@Composable
fun DoctorAgendaScreen(
    doctorId: Long,
    appointmentViewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedAppointmentForResume by remember { mutableStateOf<Appointment?>(null) }
    val sharedScrollState = rememberScrollState()

    LaunchedEffect(doctorId, selectedDate) {
        appointmentViewModel.getDoctorAppointments(doctorId)
    }

    val appointmentsList = appointmentViewModel.doctorAppointments

    val appointmentsForDay = remember(appointmentsList, selectedDate) {
        appointmentsList.filter { appointment ->
            appointment.startTime?.startsWith(selectedDate.toString()) == true
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        CustomTopBar(
                            title = "Agenda del Doctor",
                            onNavigateBack = onNavigateBack
                        )
                    }
                }

                CalendarHeaderReadOnly(
                    selectedDate = selectedDate,
                    onPrevDay = { selectedDate = selectedDate.minusDays(1) },
                    onNextDay = { selectedDate = selectedDate.plusDays(1) },
                    onTodayClick = { selectedDate = LocalDate.now() },
                    onDateSelected = { selectedDate = it }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(sharedScrollState)
            ) {
                HoursColumnReadOnly()
                AppointmentsColumnReadOnly(
                    appointments = appointmentsForDay,
                    onAppointmentClick = { appointment ->
                        selectedAppointmentForResume = appointment
                    }
                )
            }
        }
    }

    // Modal / Diálogo de solo lectura con el resumen de la cita
    selectedAppointmentForResume?.let { appointment ->
        AlertDialog(
            onDismissRequest = { selectedAppointmentForResume = null },
            confirmButton = {},
            dismissButton = null,
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp)) {
                    ResumeDateScreen(
                        appointment = appointment,
                        onBackClick = { selectedAppointmentForResume = null },
                        onPatientClick = { }
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CalendarHeaderReadOnly(
    selectedDate: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val dayName = selectedDate.dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale("es"))
        .replaceFirstChar { it.uppercase() }

    val dayNum = selectedDate.dayOfMonth

    val monthName = selectedDate.month
        .getDisplayName(TextStyle.SHORT, Locale("es"))
        .replaceFirstChar { it.uppercase() }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDatePicker) {
        val dialog = remember(selectedDate) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    onDateSelected(LocalDate.of(year, month + 1, day))
                    showDatePicker = false
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).also {
                it.setOnDismissListener { showDatePicker = false }
            }
        }

        LaunchedEffect(Unit) {
            dialog.show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onTodayClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF537895)
            ),
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_today),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onPrevDay,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF537895)
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        IconButton(
            onClick = onNextDay,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF537895)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(
            onClick = { showDatePicker = true },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color(0xFF537895),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$dayName $dayNum de $monthName",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF537895)
            )
        }
    }
}

@Composable
private fun HoursColumnReadOnly() {
    Column(modifier = Modifier.width(58.dp)) {
        Spacer(modifier = Modifier.height(TOP_MARGIN_DP.dp))
        (DAY_START_HOUR until DAY_START_HOUR + TOTAL_HOURS).forEach { hour ->
            Box(
                modifier = Modifier
                    .height(SLOT_HEIGHT_DP.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "%02d:00".format(hour),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp, top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun AppointmentsColumnReadOnly(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit
) {
    val totalHeightDp = TOTAL_HOURS * SLOT_HEIGHT_DP

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TOP_MARGIN_DP.dp)
            .height(totalHeightDp.dp)
    ) {
        val columnMaxWidth = maxWidth

        Column(
            modifier = Modifier
                .height(totalHeightDp.dp)
                .fillMaxWidth()
        ) {
            (0 until TOTAL_HOURS).forEach { _ ->
                Box(
                    modifier = Modifier
                        .height(SLOT_HEIGHT_DP.dp)
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFDDDDDD),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                            val midY = size.height / 2f
                            val nativePaint = Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.parseColor("#E8E8E8")
                                strokeWidth = 1.dp.toPx()
                                pathEffect = DashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                                    0f
                                )
                            }
                            drawContext.canvas.nativeCanvas.drawLine(
                                0f,
                                midY,
                                size.width,
                                midY,
                                nativePaint
                            )
                        }
                )
            }
        }

        appointments.forEach { appointment ->
            val startMinutes = parseTimeToMinutesRO(appointment.startTime)
            val duration = appointment.treatment?.durationMinutes ?: 30

            if (startMinutes != null) {
                val endMinutes = startMinutes + duration

                val overlappingApps = appointments.filter { other ->
                    val oStart = parseTimeToMinutesRO(other.startTime) ?: 0
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
                    val heightDp = appointmentHeightDpRO(appointment)

                    AppointmentCardReadOnly(
                        appointment = appointment,
                        color = colorForTreatmentRO(appointment.treatment?.id),
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
private fun AppointmentCardReadOnly(
    appointment: Appointment,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val startLabel = formatTimeRO(appointment.startTime)
    val endLabel = run {
        val startMin = parseTimeToMinutesRO(appointment.startTime)
        val duration = appointment.treatment?.durationMinutes
        if (startMin != null && duration != null) {
            val endMin = startMin + duration
            "%02d:%02d".format(endMin / 60, endMin % 60)
        } else {
            formatTimeRO(appointment.endTime)
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
            .border(
                width = 3.dp,
                color = color,
                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
            )
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

private fun colorForTreatmentRO(treatmentId: Long?): Color {
    if (treatmentId == null) return Color(0xFF4DB6AC)
    return TreatmentColors[(treatmentId % TreatmentColors.size).toInt()]
}

private fun appointmentHeightDpRO(appointment: Appointment): Float {
    val durationMinutes = appointment.treatment?.durationMinutes ?: 30
    return (durationMinutes * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f)
}

private fun extractTimeOnlyRO(dateTimeString: String?): String? {
    if (dateTimeString == null) return null
    return if (dateTimeString.contains("T")) {
        dateTimeString.split("T").lastOrNull()
    } else {
        dateTimeString.split(" ").lastOrNull()
    }
}

private fun parseTimeToMinutesRO(time: String?): Int? {
    val cleanTime = extractTimeOnlyRO(time) ?: return null
    return try {
        val parts = cleanTime.split(":")
        parts[0].toInt() * 60 + parts[1].toInt()
    } catch (e: Exception) { null }
}

private fun formatTimeRO(time: String?): String {
    val cleanTime = extractTimeOnlyRO(time) ?: return ""
    return try {
        val parts = cleanTime.split(":")
        "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) { cleanTime }
}