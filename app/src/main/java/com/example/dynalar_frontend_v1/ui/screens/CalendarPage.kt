package com.example.dynalar_frontend_v1.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.TreatmentColors
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


private const val SLOT_HEIGHT_DP = 80
private const val DAY_START_HOUR = 8
private const val TOTAL_HOURS = 14
private const val TOP_MARGIN_DP = 16

@Composable
fun CalendarPage(
    viewModel: AppointmentViewModel = viewModel(),
    onAppointmentClick: (Appointment) -> Unit = {},
    onAddAppointmentClick: (LocalDate, Int, Int) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},

) {

    val selectedDate = viewModel.selectedCalendarDate
    val sharedScrollState = rememberScrollState()


    LaunchedEffect(selectedDate) {
        val startOfDay = selectedDate.atStartOfDay()
        val endOfDay = selectedDate.atTime(23, 59, 59)
        viewModel.fetchCalendar(startOfDay, endOfDay)
    }

    val uiState = viewModel.uiStateCalendar
    val appointmentsForDay = remember(uiState, selectedDate) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data.filter { appointment ->
                appointment.startTime?.startsWith(selectedDate.toString()) == true
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // TOP BAR
                    Box(modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart) {
                        CustomTopBar(
                            title = "Calendari",
                            onNavigateBack = onNavigateBack
                        )
                    }

                    // BOTÓN +
                    Box(
                        modifier = Modifier
                            .offset(y = 2.dp)
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF537895))
                            .clickable {
                                onAddAppointmentClick(selectedDate, 9, 0)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Afegir cita",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                // 2. ACTUALIZAMOS usando el ViewModel en lugar de variables locales
                CalendarHeader(
                    selectedDate = selectedDate,
                    onPrevDay = { viewModel.updateSelectedDate(selectedDate.minusDays(1)) },
                    onNextDay = { viewModel.updateSelectedDate(selectedDate.plusDays(1)) },
                    onTodayClick = { viewModel.updateSelectedDate(LocalDate.now()) },
                    onDateSelected = { viewModel.updateSelectedDate(it) }
                )
                // Usamos HorizontalDivider de M3 o simplemente una caja fina
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (uiState is InterfaceGlobal.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF537895)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(sharedScrollState)
            ) {
                HoursColumn()
                AppointmentsColumn(
                    appointments = appointmentsForDay,
                    onAppointmentClick = onAppointmentClick,
                    onSlotClick = { hour, minute ->
                        onAddAppointmentClick(selectedDate, hour, minute)
                    }
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
            android.app.DatePickerDialog(
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

        // AVUI IZQUIERDA
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
                text = "Avui",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // FLECHA IZQUIERDA
        IconButton(
            onClick = onPrevDay,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Anterior",
                tint = Color(0xFF537895)
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        // FLECHA DERECHA
        IconButton(
            onClick = onNextDay,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Següent",
                tint = Color(0xFF537895)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // FECHA DERECHA
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

    // USAMOS BoxWithConstraints PARA SABER EL ANCHO DE LA PANTALLA
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(top = TOP_MARGIN_DP.dp).height(totalHeightDp.dp)
    ) {
        val columnMaxWidth = maxWidth // Guardamos el ancho disponible


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
                            val nativePaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.parseColor("#E8E8E8")
                                strokeWidth = 1.dp.toPx()
                                pathEffect = android.graphics.DashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
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

                // --- LÓGICA MÁGICA DE SOLAPAMIENTO ---
                // Buscamos qué otras citas chocan con esta en el tiempo
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
                text = patientName.ifEmpty { "Pacient Desconegut" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // ALERTAS
            if (hasAllergies) {
                Text(
                    text = "Al·lèrgies: $allergies",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasInfectious) {
                Text(
                    text = "Infeccioses: $infectiousDeceases",
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
                    text = "Veure Resum Cita",
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


//Utils
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