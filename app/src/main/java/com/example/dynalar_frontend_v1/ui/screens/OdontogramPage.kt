package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.model.odontogram.Odontogram
import com.example.dynalar_frontend_v1.model.odontogram.OdontogramEntry
import com.example.dynalar_frontend_v1.model.odontogram.OdontogramUiState
import com.example.dynalar_frontend_v1.model.odontogram.ProcessStatus
import com.example.dynalar_frontend_v1.model.odontogram.ToothSurface
import com.example.dynalar_frontend_v1.model.odontogram.ToothSymbol
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.viewmodel.OdontogramViewModel
import kotlinx.coroutines.launch

enum class OdontogramState {
    FULL, VERTICAL, QUADRANT
}

fun getTeethForQuadrant(quadrantId: Int): List<Int> {
    return when (quadrantId) {
        1 -> listOf(18, 17, 16, 15, 14, 13, 12, 11)
        2 -> listOf(21, 22, 23, 24, 25, 26, 27, 28)
        3 -> listOf(31, 32, 33, 34, 35, 36, 37, 38)
        4 -> listOf(48, 47, 46, 45, 44, 43, 42, 41)
        else -> emptyList()
    }
}

fun getTemporalTeethForQuadrant(quadrantId: Int): List<Int> {
    return when (quadrantId) {
        1 -> listOf(55, 54, 53, 52, 51)
        2 -> listOf(61, 62, 63, 64, 65)
        3 -> listOf(71, 72, 73, 74, 75)
        4 -> listOf(85, 84, 83, 82, 81)
        else -> emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdontogramPage(
    onToothSelected: (Int) -> Unit,
    odontogramId: Long,
    viewModel: OdontogramViewModel = viewModel(),
    onBack: () -> Unit
) {
    var currentState by remember { mutableStateOf(OdontogramState.FULL) }
    var selectedQuadrant by remember { mutableStateOf<Int?>(null) }
    var quadrantName by remember { mutableStateOf("") }
    val odontogramUiState by viewModel.odontogramUiState.collectAsState()

    var showEntries by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(odontogramId) {
        viewModel.getOdontogramById(odontogramId)
    }

    val entries = when (val state = odontogramUiState) {
        is OdontogramUiState.Success -> state.odontogram.odontogramEntries.orEmpty()
        else -> emptyList()
    }

    val filteredEntries = entries.sortedBy { it.tooth?.number }

    when (selectedQuadrant) {
        1 -> quadrantName = "Superior Dret"
        2 -> quadrantName = "Superior Esquerra"
        3 -> quadrantName = "Inferior Dret"
        4 -> quadrantName = "Inferior Esquerra"
    }

    if (showEntries) {
        ModalBottomSheet(
            onDismissRequest = { showEntries = false },
            sheetState = sheetState
        ) {
            OdontogramEntries(
                entries = filteredEntries,
                viewModel = viewModel,
                onDeleteEntry = { entryToRemove ->

                    val updatedEntries = entries.filterNot {
                        it.tooth?.number == entryToRemove.tooth?.number &&
                                it.surface == entryToRemove.surface &&
                                it.dentalProcess?.name == entryToRemove.dentalProcess?.name
                    }

                    val updatedOdontogram = Odontogram(
                        odontogramEntries = updatedEntries
                    )

                    viewModel.updateOdontogram(odontogramId, updatedOdontogram)
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val dynamicTitle = when (currentState) {
            OdontogramState.FULL     -> "Odontograma"
            OdontogramState.VERTICAL -> "Odontograma"
            OdontogramState.QUADRANT -> "Quadrant $quadrantName"
        }

        CustomTopBar(
            title = dynamicTitle,
            onNavigateBack = {
                if (currentState == OdontogramState.QUADRANT || currentState == OdontogramState.VERTICAL) {
                    currentState = OdontogramState.FULL
                    selectedQuadrant = null
                } else {
                    onBack()
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (odontogramUiState) {
                is OdontogramUiState.Loading,
                is OdontogramUiState.Idle -> {
                    CircularProgressIndicator()
                }
                is OdontogramUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (odontogramUiState as OdontogramUiState.Error).message,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.getOdontogramById(odontogramId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                is OdontogramUiState.Success -> {
                    AnimatedContent(
                        targetState = currentState,
                        transitionSpec = {
                            if (targetState == OdontogramState.QUADRANT) {
                                (scaleIn(initialScale = 0.6f, animationSpec = tween(400)) +
                                        fadeIn(animationSpec = tween(400))) togetherWith
                                        (scaleOut(targetScale = 1.4f, animationSpec = tween(300)) +
                                                fadeOut(animationSpec = tween(300)))
                            } else {
                                (scaleIn(initialScale = 1.4f, animationSpec = tween(400)) +
                                        fadeIn(animationSpec = tween(400))) togetherWith
                                        (scaleOut(targetScale = 0.6f, animationSpec = tween(300)) +
                                                fadeOut(animationSpec = tween(300)))
                            }
                        },
                        label = "odontogramContent"
                    ) { state ->
                        when (state) {
                            OdontogramState.FULL -> OdontogramGeneralView(
                                onQuadrantClick = { quadrant ->
                                    selectedQuadrant = quadrant
                                    currentState = OdontogramState.QUADRANT
                                },
                                entries = entries
                            )
                            OdontogramState.VERTICAL -> OdontogramVerticalView(
                                onQuadrantClick = { quadrant ->
                                    selectedQuadrant = quadrant
                                    currentState = OdontogramState.QUADRANT
                                },
                                entries = entries
                            )
                            OdontogramState.QUADRANT -> QuadrantDetailView(
                                quadrantId = selectedQuadrant ?: 1,
                                onToothClick = { tooth -> onToothSelected(tooth) },
                                entries = entries
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = currentState == OdontogramState.FULL,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { currentState = OdontogramState.VERTICAL },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Ver Verticalment")
                }
                Button(
                    onClick = { showEntries = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Ver Registres")
                }
            }
        }

        AnimatedVisibility(
            visible = currentState != OdontogramState.FULL,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { currentState = OdontogramState.FULL },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Ver complet")
                }
                Button(
                    onClick = { showEntries = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Ver Registres")
                }
            }
        }
    }
}
@Composable
fun OdontogramEntries(
    entries: List<OdontogramEntry>,
    onDeleteEntry: (OdontogramEntry) -> Unit,
    viewModel: OdontogramViewModel
) {
    var entryToDelete by remember { mutableStateOf<OdontogramEntry?>(null) }

    if (entryToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                entryToDelete?.let { onDeleteEntry(it) }
                entryToDelete = null
            },
            onDismiss = {
                entryToDelete = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Registres",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (entries.isEmpty()) {
            Text(
                text = "No hi ha registres guardats",
                color = Color.Gray,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            val firstEntry = entries.firstOrNull()

            entries.forEach { entry ->
                val isFirst = (entry == firstEntry)
                SwipeToDeleteContainer(
                    onDelete = { entryToDelete = entry },
                    enableHintAnimation = isFirst,
                    hintAlreadyShown = viewModel.isDeleteHintShown,
                    onHintShown = { viewModel.isDeleteHintShown = true }

                ) {
                    Box(modifier = Modifier.background(Color.White)) {
                        EntryItem(entry = entry)
                    }
                }
                HorizontalDivider(Modifier, thickness = 1.dp, color = Color(0xFFEEEEEE))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Confirmació d'esborrat", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Segur que vols esborrar aquest registre?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Esborrar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}
@Composable
fun EntryItem(entry: OdontogramEntry) {
    val processColor = when (entry.dentalProcess?.name) {
        "Absència natural" -> Color(0xFF2D2D2D)             // Negro
        "Càries radiogràfica" -> Color(0xFF00B050)          // Verde
        "Segellat de foses i fissures" -> Color(0xFFFFD700) // Amarillo
        else -> when (entry.processStatus) {
            ProcessStatus.FET     -> Color(0xFF0070C0) // Azul
            ProcessStatus.PER_FER -> Color(0xFFFF0000) // Rojo
            else -> Color.Gray
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = processColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${entry.tooth?.number}",
                color = processColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = entry.dentalProcess?.name ?: "Prùces: ${entry.dentalProcess?.name}}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = "Superficie: ${entry.surface}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        if (entry.processStatus == ProcessStatus.FET || entry.processStatus == ProcessStatus.PER_FER) {
            Box(
                modifier = Modifier
                    .background(color = processColor, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (entry.processStatus) {
                        ProcessStatus.FET -> "Fet"
                        ProcessStatus.PER_FER -> "Per fer"
                        else -> ""
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OdontogramVerticalView(
    onQuadrantClick: (Int) -> Unit,
    entries: List<OdontogramEntry>? = emptyList()) {
    val quadrantBackgroundColor = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Superior Dret", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            QuadrantTeethBlock(
                permanentTeeth = getTeethForQuadrant(1),
                temporalTeeth = getTemporalTeethForQuadrant(1),
                isUpper = true,
                toothSize = 32,
                temporalToothSize = 28,
                numberSize = 12,
                toothSpacing = 8,
                entries = entries,
                onClick = { onQuadrantClick(1) }
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Superior Esquerra", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            QuadrantTeethBlock(
                permanentTeeth = getTeethForQuadrant(2),
                temporalTeeth = getTemporalTeethForQuadrant(2),
                toothSize = 32,
                temporalToothSize = 28,
                numberSize = 12,
                isUpper = true,
                toothSpacing = 8,
                entries = entries,
                onClick = { onQuadrantClick(2) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inferior Dret", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            QuadrantTeethBlock(
                permanentTeeth = getTeethForQuadrant(4),
                temporalTeeth = getTemporalTeethForQuadrant(4),
                isUpper = false,
                toothSize = 32,
                temporalToothSize = 28,
                numberSize = 12,
                toothSpacing = 8,
                entries = entries,
                onClick = { onQuadrantClick(4) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inferior Esquerra", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            QuadrantTeethBlock(
                permanentTeeth = getTeethForQuadrant(3),
                temporalTeeth = getTemporalTeethForQuadrant(3),
                isUpper = false,
                toothSize = 32,
                temporalToothSize = 28,
                numberSize = 12,
                toothSpacing = 8,
                entries = entries,
                onClick = { onQuadrantClick(3) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }


@Composable
fun OdontogramGeneralView(
    onQuadrantClick: (Int) -> Unit,
    entries: List<OdontogramEntry>? = emptyList()) {
    val quadrantBackgroundColor = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Superior Dret", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                QuadrantTeethBlock(
                    permanentTeeth = getTeethForQuadrant(1),
                    temporalTeeth = getTemporalTeethForQuadrant(1),
                    isUpper = true,
                    entries = entries,
                    onClick = { onQuadrantClick(1) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Superior Esquerra", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                QuadrantTeethBlock(
                    permanentTeeth = getTeethForQuadrant(2),
                    temporalTeeth = getTemporalTeethForQuadrant(2),
                    isUpper = true,
                    entries = entries,
                    onClick = { onQuadrantClick(2) }
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QuadrantTeethBlock(
                    permanentTeeth = getTeethForQuadrant(4),
                    temporalTeeth = getTemporalTeethForQuadrant(4),
                    isUpper = false,
                    entries = entries,
                    onClick = { onQuadrantClick(4) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inferior Dret", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = quadrantBackgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QuadrantTeethBlock(
                    permanentTeeth = getTeethForQuadrant(3),
                    temporalTeeth = getTemporalTeethForQuadrant(3),
                    isUpper = false,
                    entries = entries,
                    onClick = { onQuadrantClick(3) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inferior Esquerra", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun QuadrantTeethBlock(
    permanentTeeth: List<Int>,
    temporalTeeth: List<Int>,
    isUpper: Boolean,
    toothSize: Int = 18,
    temporalToothSize: Int = 16,
    numberSize: Int = 10,
    toothSpacing: Int = 0,
    entries: List<OdontogramEntry>? = emptyList(),
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUpper) {
            Row (horizontalArrangement = Arrangement.spacedBy(toothSpacing.dp)){
                permanentTeeth.forEach { n ->
                    MiniToothItem(number = n, toothSize = toothSize, numberSize, entries.orEmpty())
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row (horizontalArrangement = Arrangement.spacedBy(toothSpacing.dp)){
                temporalTeeth.forEach { n ->
                    MiniToothItem(number = n, toothSize = temporalToothSize, numberSize, entries.orEmpty())
                }
            }
        } else {
            Row (horizontalArrangement = Arrangement.spacedBy(toothSpacing.dp)){
                temporalTeeth.forEach { n ->
                    MiniToothItem(number = n, toothSize = temporalToothSize, numberSize, entries.orEmpty())
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row (horizontalArrangement = Arrangement.spacedBy(toothSpacing.dp)){
                permanentTeeth.forEach { n ->
                    MiniToothItem(number = n, toothSize = toothSize, numberSize, entries.orEmpty())
                }
            }
        }
    }
}

@Composable
fun MiniToothItem(number: Int, toothSize: Int, numberSize: Int = 10, entries: List<OdontogramEntry> = emptyList()) {
    val isBox = number in 11..13 || number in 21..23 ||
            number in 31..33 || number in 41..43 ||
            number in 51..53 || number in 61..63 ||
            number in 71..73 || number in 81..83

    val toothEntries = entries.filter { it.tooth?.number == number }

    val isUpperPart = number in 11..18 || number in 21..28 ||
            number in 51..58 || number in 61..68
    val isLeftPart = number in 21..28 || number in 31..38 ||
            number in 61..68 || number in 71..78


    fun getColorForEntry(entry: OdontogramEntry): Color? {
        when (entry.dentalProcess?.name) {
            "Absència natural" -> return Color(0xFF2D2D2D)             // Negro
            "Càries radiogràfica" -> return Color(0xFF00B050)          // Verde
            "Segellat de foses i fissures" -> return Color(0xFFFFD700)  // Amarillo
        }
        return when (entry.processStatus) {
            ProcessStatus.FET     -> Color(0xFF0070C0) // Azul
            ProcessStatus.PER_FER -> Color(0xFFFF0000) // Rojo
            else -> null
        }
    }

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 1.dp)
    ) {
        Canvas(modifier = Modifier.size(toothSize.dp)) {
            val s = size.width
            val stroke = s * 0.08f
            val black = Color.Black

            if (!isBox) {
                val innerScale = 0.5f
                val innerSize = s * innerScale
                val offset = (s - innerSize) / 2f

                val oTL = Offset(0f, 0f)
                val oTR = Offset(s, 0f)
                val oBL = Offset(0f, s)
                val oBR = Offset(s, s)
                val iTL = Offset(offset, offset)
                val iTR = Offset(offset + innerSize, offset)
                val iBL = Offset(offset, offset + innerSize)
                val iBR = Offset(offset + innerSize, offset + innerSize)

                val pathTop = Path().apply { moveTo(oTL.x, oTL.y); lineTo(oTR.x, oTR.y); lineTo(iTR.x, iTR.y); lineTo(iTL.x, iTL.y); close() }
                val pathBottom = Path().apply { moveTo(oBL.x, oBL.y); lineTo(oBR.x, oBR.y); lineTo(iBR.x, iBR.y); lineTo(iBL.x, iBL.y); close() }
                val pathLeft = Path().apply { moveTo(oTL.x, oTL.y); lineTo(oBL.x, oBL.y); lineTo(iBL.x, iBL.y); lineTo(iTL.x, iTL.y); close() }
                val pathRight = Path().apply { moveTo(oTR.x, oTR.y); lineTo(oBR.x, oBR.y); lineTo(iBR.x, iBR.y); lineTo(iTR.x, iTR.y); close() }
                val pathCenter = Path().apply { moveTo(iTL.x, iTL.y); lineTo(iTR.x, iTR.y); lineTo(iBR.x, iBR.y); lineTo(iBL.x, iBL.y); close() }

                fun paintSurface(path: Path, surface: ToothSurface) {
                    val entryColor = getEntryColorForSurface(surface)
                    if (entryColor != null) {
                        drawPath(path = path, color = entryColor.copy(alpha = 0.7f), style = Fill)
                    }
                    drawPath(path = path, color = black, style = Stroke(width = stroke / 2f))
                }

                paintSurface(pathTop,    if (isUpperPart) ToothSurface.VESTIBULAR else ToothSurface.LINGUAL)
                paintSurface(pathBottom, if (isUpperPart) ToothSurface.LINGUAL    else ToothSurface.VESTIBULAR)
                paintSurface(pathLeft,   if (isLeftPart)  ToothSurface.MESIAL     else ToothSurface.DISTAL)
                paintSurface(pathRight,  if (isLeftPart)  ToothSurface.DISTAL     else ToothSurface.MESIAL)
                paintSurface(pathCenter, ToothSurface.OCLUSAL)

                drawRect(color = black, style = Stroke(width = stroke))
                drawRect(
                    color = black,
                    topLeft = Offset(offset, offset),
                    size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
                    style = Stroke(width = stroke)
                )
                drawLine(black, Offset(0f, 0f), Offset(offset, offset), stroke)
                drawLine(black, Offset(s, 0f), Offset(offset + innerSize, offset), stroke)
                drawLine(black, Offset(0f, s), Offset(offset, offset + innerSize), stroke)
                drawLine(black, Offset(s, s), Offset(offset + innerSize, offset + innerSize), stroke)

            } else {
                val center = Offset(s / 2f, s / 2f)

                val pathTop = Path().apply { moveTo(0f, 0f); lineTo(s, 0f); lineTo(center.x, center.y); close() }
                val pathBottom = Path().apply { moveTo(0f, s); lineTo(s, s); lineTo(center.x, center.y); close() }
                val pathLeft = Path().apply { moveTo(0f, 0f); lineTo(0f, s); lineTo(center.x, center.y); close() }
                val pathRight = Path().apply { moveTo(s, 0f); lineTo(s, s); lineTo(center.x, center.y); close() }

                fun paintSurface(path: Path, surface: ToothSurface) {
                    val entryColor = getEntryColorForSurface(surface)
                    if (entryColor != null) {
                        drawPath(path = path, color = entryColor.copy(alpha = 0.7f), style = Fill)
                    }
                    drawPath(path = path, color = black, style = Stroke(width = stroke / 2f))
                }

                paintSurface(pathTop, if (isUpperPart) ToothSurface.VESTIBULAR else ToothSurface.LINGUAL)
                paintSurface(pathBottom, if (isUpperPart) ToothSurface.LINGUAL else ToothSurface.VESTIBULAR)
                paintSurface(pathLeft, if (isLeftPart) ToothSurface.MESIAL else ToothSurface.DISTAL)
                paintSurface(pathRight, if (isLeftPart) ToothSurface.DISTAL else ToothSurface.MESIAL)

                drawRect(color = black, style = Stroke(width = stroke))
                drawLine(black, Offset(0f, 0f), center, stroke)
                drawLine(black, Offset(s, 0f), center, stroke)
                drawLine(black, Offset(0f, s), center, stroke)
                drawLine(black, Offset(s, s), center, stroke)
            }

            val completEntry = toothEntries.find { it.surface == ToothSurface.COMPLET }
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
                                    textSize = s * 0.25f
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
                            isFakeBoldText = true
                            typeface = android.graphics.Typeface.SERIF
                        }
                        val centerY =
                            (s / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)

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
        Spacer(modifier = Modifier.height(2.dp))
        Text("$number", fontSize = numberSize.sp, color = Color.Red)
    }
}

@Composable
fun QuadrantDetailView(
    quadrantId: Int,
    onToothClick: (Int) -> Unit,
    entries: List<OdontogramEntry>? = emptyList()
) {
    val permanentTeeth = getTeethForQuadrant(quadrantId)
    val temporalTeeth = getTemporalTeethForQuadrant(quadrantId)
    val isUpper = quadrantId == 1 || quadrantId == 2

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Selecciona un dent",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isUpper) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                permanentTeeth.forEach { n ->
                    ToothDetailItem(number = n, toothSize = 38, onToothClick = onToothClick, entries.orEmpty())
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                temporalTeeth.forEach { n ->
                    ToothDetailItem(number = n, toothSize = 34, onToothClick = onToothClick, entries.orEmpty())
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                temporalTeeth.forEach { n ->
                    ToothDetailItem(number = n, toothSize = 34, onToothClick = onToothClick, entries.orEmpty())
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                permanentTeeth.forEach { n ->
                    ToothDetailItem(number = n, toothSize = 38, onToothClick = onToothClick, entries.orEmpty())
                }
            }
        }
    }
}

@Composable
fun ToothDetailItem(number: Int, toothSize: Int, onToothClick: (Int) -> Unit, entries: List<OdontogramEntry> = emptyList()) {
    var visible by remember { mutableStateOf(false) }
    val toothEntries = entries.filter { it.tooth?.number == number }

    LaunchedEffect(number) {
        kotlinx.coroutines.delay((number % 10) * 40L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.3f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )) + fadeIn()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onToothClick(number) }
                .padding(4.5.dp)
        ) {
            Text("$number", fontSize = 12.sp    , color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            InteractiveTooth(
                number = number,
                isTrapezoid = !(number in 11..13 || number in 21..23 ||
                        number in 31..33 || number in 41..43 ||
                        number in 51..53 || number in 61..63 ||
                        number in 71..73 || number in 81..83),
                color = Color.Black,
                selectedSurface = ToothSurface.NONE,
                onSurfaceClick = { onToothClick(number) },
                toothEntries = toothEntries,
                modifier = Modifier.size(toothSize.dp)
            )
        }
    }
}