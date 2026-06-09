package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.model.odontogram.OdontogramEntry
import com.example.dynalar_frontend_v1.model.odontogram.OdontogramUiState
import com.example.dynalar_frontend_v1.model.odontogram.DentalProcess
import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.odontogram.ProcessStatus
import com.example.dynalar_frontend_v1.model.odontogram.Tooth
import com.example.dynalar_frontend_v1.model.odontogram.ToothSurface
import com.example.dynalar_frontend_v1.model.odontogram.ToothSymbol
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.viewmodel.OdontogramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToothPage(number: Int, odontogramId: Long, viewModel: OdontogramViewModel = viewModel(), onBack: () -> Unit,) {

    var selectedSurface by remember { mutableStateOf(ToothSurface.NONE) }
    var selectedProcessStatus by remember { mutableStateOf<ProcessStatus?>(ProcessStatus.FET) }
    var selectedDentalProcess by remember { mutableStateOf(null as DentalProcess?) }

    val odontogramUiState by viewModel.odontogramUiState.collectAsState()

    val dentalProcessList by viewModel.dentalProcessesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getAllDentalProcesses()
    }

    val entries = when (val state = odontogramUiState) {
        is OdontogramUiState.Success -> state.odontogram.odontogramEntries.orEmpty()
        else -> emptyList()
    }

    var toothEntries = entries.filter { it.tooth?.number == number }

    var displayEntries by remember(toothEntries) { mutableStateOf(toothEntries) }

    var showEntries by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var surfaceColor by remember { mutableStateOf(Color.hsl(1.37F, 1.0F, 0.92F)) }

    var showReplaceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDentalProcess, selectedProcessStatus) {
        when (selectedDentalProcess?.name) {
            "Absència natural" -> {
                selectedSurface = ToothSurface.COMPLET
                surfaceColor = Color(0xFF2D2D2D) // Negro
                selectedProcessStatus = null
            }
            "Càries radiogràfica" -> {
                surfaceColor = Color(0xFF00B050) // Verde
            }
            "Segellat de foses i fissures" -> {
                surfaceColor = Color(0xFFFFD700) // Amarillo
            }
            "Exodòncia", "Endodòncia", "Corona", "Exodòncia per ortodòncia" -> {
                selectedSurface = ToothSurface.COMPLET
            }
            else -> {
                surfaceColor = when (selectedProcessStatus) {
                    ProcessStatus.FET -> Color(0xFF0070C0)     // Azul
                    ProcessStatus.PER_FER -> Color(0xFFFF0000) // Rojo
                    else -> Color.hsl(1.37F, 1.0F, 0.92F)
                }
            }
        }
    }

    val saveTreatment = {
        if (selectedDentalProcess != null) {
            val newEntry = OdontogramEntry(
                tooth = Tooth(number = number),
                surface = selectedSurface,
                dentalProcess = selectedDentalProcess,
                processStatus = selectedProcessStatus
            )

            val updatedEntries = entries.filterNot { entry ->
                entry.tooth?.number == number && (
                        selectedSurface == ToothSurface.COMPLET ||
                                entry.surface == ToothSurface.COMPLET ||
                                entry.surface == selectedSurface
                        )
            } + newEntry

            val odontogramToUpdate = Odontogram(
                odontogramEntries = updatedEntries
            )

            viewModel.updateOdontogram(odontogramId, odontogramToUpdate)
            toothEntries = updatedEntries.filter { it.tooth?.number == number }
            displayEntries = updatedEntries.filter { it.tooth?.number == number }

            selectedSurface = ToothSurface.NONE
            selectedDentalProcess = null
            selectedProcessStatus = ProcessStatus.FET
            showReplaceDialog = false
        }
    }

    val isBox = number in 11..13 || number in 21..23 ||
            number in 31..33 || number in 41..43 ||
            number in 51..53 || number in 61..63 ||
            number in 71..73 || number in 81..83

    if (showReplaceDialog) {
        ReplaceTreatmentDialog(
            surface = selectedSurface,
            onConfirm = { saveTreatment() },
            onDismiss = { showReplaceDialog = false }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        CustomTopBar(
            title = "Dent numero $number",
            onNavigateBack = { onBack() },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))


        if (showEntries) {
            ModalBottomSheet(
                onDismissRequest = { showEntries = false },
                sheetState = sheetState
            ) {

                OdontogramEntries(
                    entries = toothEntries,
                    viewModel = viewModel,
                    onDeleteEntry = { entryToRemove ->
                        val updatedEntries = entries.filterNot {
                            it.tooth?.number == entryToRemove.tooth?.number &&
                                    it.surface == entryToRemove.surface &&
                                    it.dentalProcess?.name == entryToRemove.dentalProcess?.name
                        }
                        val odontogramToUpdate = Odontogram(odontogramEntries = updatedEntries)
                        viewModel.updateOdontogram(odontogramId, odontogramToUpdate)


                        toothEntries = updatedEntries.filter { it.tooth?.number == number }
                        displayEntries = updatedEntries.filter { it.tooth?.number == number }
                    }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            InteractiveTooth(
                number,
                isTrapezoid = !isBox,
                color = Color.Black,
                selectedSurface = selectedSurface,
                onSurfaceClick = { surface ->
                    selectedSurface = surface
                },
                toothEntries = displayEntries,
                selectedColor = surfaceColor,
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { selectedSurface = ToothSurface.COMPLET }) {
                Text("Escollir Dent Complet (COMPLET)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (selectedSurface != ToothSurface.NONE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Registrar Procés",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Superficie: $selectedSurface",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        Text("Seleccioni un Procés:", fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        DentalProcessList(
                            dentalProcessList = dentalProcessList,
                            selectedDentalProcess = selectedDentalProcess,
                            onPathologySelected = {
                                    pathology ->
                                selectedDentalProcess = pathology
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Status del Procés:", fontWeight = FontWeight.SemiBold)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            FilterChip(
                                selected = selectedProcessStatus == ProcessStatus.PER_FER,
                                onClick = { selectedProcessStatus = ProcessStatus.PER_FER },
                                label = { Text("PER FER", color = Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF0000)
                                )
                            )

                            FilterChip(
                                selected = selectedProcessStatus == ProcessStatus.FET,
                                onClick = { selectedProcessStatus = ProcessStatus.FET },
                                label = { Text(ProcessStatus.FET.name, color = Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0070C0)
                                )
                            )

                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button (
                            onClick = {
                                if (selectedDentalProcess != null) {
                                    val surfaceExists = displayEntries.any { it.surface == selectedSurface }
                                    val selectedCompletWithEntries = selectedSurface == ToothSurface.COMPLET && displayEntries.isNotEmpty()
                                    val hasCompletAlready = displayEntries.any { it.surface == ToothSurface.COMPLET }
                                    if (surfaceExists || selectedCompletWithEntries || hasCompletAlready) {
                                        showReplaceDialog = true
                                    } else {
                                        saveTreatment()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Desar Procés")
                        }
                    }
                }
            }
        }
        Button(
            onClick = { showEntries = true },
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Text("Ver Registres")
        }
    }
}

@Composable
fun ReplaceTreatmentDialog(
    surface: ToothSurface,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirmació de reemplaçament",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Ja existeix un registre per a la superfície $surface d'aquesta dent. N'estàs segur que vols reemplaçar-lo?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Reemplaçar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel·lar")
            }
        }
    )
}



fun getToothSymbol(entries: List<OdontogramEntry?>): ToothSymbol {
    val completEntry = entries.find { it?.surface == ToothSurface.COMPLET }
    if (completEntry != null) {
        return when (completEntry.dentalProcess?.name) {
            "Absència natural", "Exodòncia" -> ToothSymbol.X
            "Exodòncia per ortodòncia" -> ToothSymbol.X_ORT
            "Endodòncia" -> ToothSymbol.E
            else -> ToothSymbol.NONE
        }
    }
    return ToothSymbol.NONE
}

fun getColorForEntry(entry: OdontogramEntry): Color? {
    when (entry.dentalProcess?.name) {
        "Absència natural" -> return Color(0xFF2D2D2D)             // Negro
        "Càries radiogràfica" -> return Color(0xFF00B050)          // Verde
        "Segellat de foses i fissures" -> return Color(0xFFFFD700) // Amarillo
    }

    return when (entry.processStatus) {
        ProcessStatus.FET     -> Color(0xFF0070C0) // Azul
        ProcessStatus.PER_FER -> Color(0xFFFF0000) // Rojo
        else -> null
    }
}

@Composable
fun InteractiveTooth(
    number: Int,
    isTrapezoid: Boolean,
    color: Color,
    selectedSurface: ToothSurface,
    onSurfaceClick: (ToothSurface) -> Unit,
    modifier: Modifier = Modifier,
    toothEntries: List<OdontogramEntry?> = emptyList(),
    selectedColor: Color = Color.hsl(1.37F, 1.0F, 0.92F)
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isTrapezoid, number) {
                    detectTapGestures { tapOffset ->
                        val x = tapOffset.x
                        val y = tapOffset.y
                        val s = size.width.toFloat()

                        if (isTrapezoid) {
                            val innerScale = 0.5f
                            val innerSize = s * innerScale
                            val innerOffset = (s - innerSize) / 2f

                            if (x in innerOffset..(innerOffset + innerSize) && y in innerOffset..(innerOffset + innerSize)) {
                                onSurfaceClick(ToothSurface.OCLUSAL)
                                return@detectTapGestures
                            }
                        }

                        val isAbove1 = y < x
                        val isAbove2 = y < s - x

                        val isUpper = number in 11..18 || number in 21..28 ||
                                number in 51..58 || number in 61..68

                        val isLeftPart = number in 21..28 || number in 31..38 ||
                                number in 61..68 || number in 71..78

                        val clickedSurface = when {
                            isAbove1 && isAbove2   -> if (isUpper) ToothSurface.VESTIBULAR else ToothSurface.LINGUAL
                            !isAbove1 && !isAbove2 -> if (isUpper) ToothSurface.LINGUAL    else ToothSurface.VESTIBULAR
                            !isAbove1 && isAbove2  -> if (isLeftPart) ToothSurface.MESIAL  else ToothSurface.DISTAL
                            else                   -> if (isLeftPart) ToothSurface.DISTAL  else ToothSurface.MESIAL
                        }
                        onSurfaceClick(clickedSurface)
                    }
                }
        ) {
            val s = size.width
            val borderStroke = s * 0.05f

            val oTL = Offset(0f, 0f)
            val oTR = Offset(s, 0f)
            val oBL = Offset(0f, s)
            val oBR = Offset(s, s)

            val pathTop = Path()
            val pathBottom = Path()
            val pathLeft = Path()
            val pathRight = Path()
            var pathCenter: Path? = null

            if (isTrapezoid) {
                val innerScale = 0.5f
                val innerSize = s * innerScale
                val offset = (s - innerSize) / 2f

                val iTL = Offset(offset, offset)
                val iTR = Offset(offset + innerSize, offset)
                val iBL = Offset(offset, offset + innerSize)
                val iBR = Offset(offset + innerSize, offset + innerSize)

                pathTop.apply { moveTo(oTL.x, oTL.y); lineTo(oTR.x, oTR.y); lineTo(iTR.x, iTR.y); lineTo(iTL.x, iTL.y); close() }
                pathBottom.apply { moveTo(oBL.x, oBL.y); lineTo(oBR.x, oBR.y); lineTo(iBR.x, iBR.y); lineTo(iBL.x, iBL.y); close() }
                pathLeft.apply { moveTo(oTL.x, oTL.y); lineTo(oBL.x, oBL.y); lineTo(iBL.x, iBL.y); lineTo(iTL.x, iTL.y); close() }
                pathRight.apply { moveTo(oTR.x, oTR.y); lineTo(oBR.x, oBR.y); lineTo(iBR.x, iBR.y); lineTo(iTR.x, iTR.y); close() }
                pathCenter = Path().apply { moveTo(iTL.x, iTL.y); lineTo(iTR.x, iTR.y); lineTo(iBR.x, iBR.y); lineTo(iBL.x, iBL.y); close() }
            } else {
                val center = Offset(s / 2f, s / 2f)

                pathTop.apply { moveTo(oTL.x, oTL.y); lineTo(oTR.x, oTR.y); lineTo(center.x, center.y); close() }
                pathBottom.apply { moveTo(oBL.x, oBL.y); lineTo(oBR.x, oBR.y); lineTo(center.x, center.y); close() }
                pathLeft.apply { moveTo(oTL.x, oTL.y); lineTo(oBL.x, oBL.y); lineTo(center.x, center.y); close() }
                pathRight.apply { moveTo(oTR.x, oTR.y); lineTo(oBR.x, oBR.y); lineTo(center.x, center.y); close() }
            }

            val isUpperPart = number in 11..18 || number in 21..28 ||
                    number in 51..58 || number in 61..68

            val isLeftPart = number in 21..28 || number in 31..38 ||
                    number in 61..68 || number in 71..78




            fun getEntryColorForSurface(surface: ToothSurface): Color? {
                val completEntry = toothEntries.find { it?.surface == ToothSurface.COMPLET }

                if (completEntry != null) {
                    val symbol = getToothSymbol(toothEntries)

                    if (symbol != ToothSymbol.NONE) {
                        return null
                    }

                    val completColor = getColorForEntry(completEntry)
                    if (completColor != null) {
                        return completColor
                    }
                }

                val entry = toothEntries.find { it?.surface == surface }
                return entry?.let { getColorForEntry(it) }
            }

            fun drawSurface(path: Path, surface: ToothSurface) {
                val entryColor = getEntryColorForSurface(surface)
                when {
                    selectedSurface == ToothSurface.COMPLET || selectedSurface == surface -> {
                        drawPath(path = path, color = selectedColor, style = Fill)
                    }

                    entryColor != null -> {
                        drawPath(path = path, color = entryColor.copy(alpha = 0.7f), style = Fill)
                    }
                }
                drawPath(path = path, color = color, style = Stroke(width = borderStroke / 2f))
            }

            drawSurface(pathTop,    if (isUpperPart) ToothSurface.VESTIBULAR else ToothSurface.LINGUAL)
            drawSurface(pathBottom, if (isUpperPart) ToothSurface.LINGUAL    else ToothSurface.VESTIBULAR)
            drawSurface(pathLeft,   if (isLeftPart) ToothSurface.MESIAL      else ToothSurface.DISTAL)
            drawSurface(pathRight,  if (isLeftPart) ToothSurface.DISTAL      else ToothSurface.MESIAL)

            if (isTrapezoid && pathCenter != null) {
                drawSurface(pathCenter, ToothSurface.OCLUSAL)
            }

            drawRect(color = color, style = Stroke(width = borderStroke))

            val completEntry = toothEntries.find { it?.surface == ToothSurface.COMPLET }
            val symbol = getToothSymbol(toothEntries)

            if (symbol != ToothSymbol.NONE && completEntry != null) {
                val symbolColor = getColorForEntry(completEntry) ?: Color.Black
                val symbolStroke = s * 0.16f

                when (symbol) {
                    ToothSymbol.X, ToothSymbol.X_ORT -> {
                        drawLine(
                            color = symbolColor,
                            start = Offset(s * 0.15f, s * 0.15f),
                            end = Offset(s * 0.85f, s * 0.85f),
                            strokeWidth = symbolStroke
                        )
                        drawLine(
                            color = symbolColor,
                            start = Offset(s * 0.85f, s * 0.15f),
                            end = Offset(s * 0.15f, s * 0.85f),
                            strokeWidth = symbolStroke
                        )
                        if (symbol == ToothSymbol.X_ORT) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "ORT",
                                s * 0.5f,
                                s * 0.95f,
                                android.graphics.Paint().apply {
                                    this.color = symbolColor.toArgb()
                                    textSize = s * 0.20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }

                    ToothSymbol.E -> {
                        val textPaint = android.graphics.Paint().apply {
                            this.color = symbolColor.toArgb()
                            textSize = s * 1.1f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = false
                            typeface = android.graphics.Typeface.SERIF
                        }
                        val centerY = (s / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)

                        drawContext.canvas.nativeCanvas.drawText(
                            "E",
                            s * 0.5f,
                            centerY,
                            textPaint
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DentalProcessList(
    dentalProcessList: List<DentalProcess>,
    selectedDentalProcess: DentalProcess?,
    onPathologySelected: (DentalProcess) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDentalProcess?.name ?: "Tria una opció",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dentalProcessList.forEach { pathology ->
                DropdownMenuItem(
                    text = { pathology.name?.let { Text(text = it) } },
                    onClick = {
                        onPathologySelected(pathology)
                        expanded = false
                    }
                )
            }
        }
    }
}