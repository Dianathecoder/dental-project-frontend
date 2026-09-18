package com.example.dynalar_frontend_v1.ui.screens.management

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.management.Treatment
import com.example.dynalar_frontend_v1.model.management.TreatmentDurationFilter
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.SharedFilterDropdown
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProtocolsPage(
    viewModel: TreatmentViewModel,
    onBack: () -> Unit,
    onTreatmentClick: (Treatment) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getTreatments()
    }

    val treatmentsList = viewModel.treatmentList

    val searchState = rememberTextFieldState()
    val listState = rememberLazyListState()
    var showAddDialog by remember { mutableStateOf(false) }
    var treatmentToDelete by remember { mutableStateOf<Treatment?>(null) }

    // Estados del Filtro Reutilizable
    var selectedDurationFilter by remember { mutableStateOf(TreatmentDurationFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    val query = searchState.text.toString().trim()

    val filteredTreatments = remember(treatmentsList, query, selectedDurationFilter, sortAscending) {
        treatmentsList
            .filter { treatment ->
                if (query.isBlank()) true
                else treatment.name?.contains(query, ignoreCase = true) == true
            }
            .filter { treatment ->
                val duration = treatment.durationMinutes ?: 0
                when (selectedDurationFilter) {
                    TreatmentDurationFilter.ALL -> true
                    TreatmentDurationFilter.SHORT -> duration < 30
                    TreatmentDurationFilter.MEDIUM -> duration in 30..60
                    TreatmentDurationFilter.LONG -> duration in 61..120
                    TreatmentDurationFilter.EXTRA_LONG -> duration > 120
                }
            }
            .let { list ->
                if (sortAscending) list.sortedBy { (it.name ?: "").uppercase() }
                else list.sortedByDescending { (it.name ?: "").uppercase() }
            }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        CustomTopBar(title = "Gestió de Tractaments", onNavigateBack = onBack)
                    }

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.size(44.dp).offset(x = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Afegir Tractament",
                            tint = ButtonPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchTreatmentBar(textFieldState = searchState)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                val filterLabel = when (selectedDurationFilter) {
                    TreatmentDurationFilter.ALL -> "Filtrar"
                    TreatmentDurationFilter.SHORT -> "< 30 min"
                    TreatmentDurationFilter.MEDIUM -> "30 min - 1h"
                    TreatmentDurationFilter.LONG -> "1h - 2h"
                    TreatmentDurationFilter.EXTRA_LONG -> "+ 2h"
                }

                SharedFilterDropdown(
                    selectedFilter = selectedDurationFilter,
                    defaultFilter = TreatmentDurationFilter.ALL,
                    sortAscending = sortAscending,
                    filterLabel = filterLabel,
                    filterSectionTitle = "Durada del tractament",
                    filterOptions = listOf(
                        TreatmentDurationFilter.ALL to "Tots",
                        TreatmentDurationFilter.SHORT to "Menys de 30 min",
                        TreatmentDurationFilter.MEDIUM to "De 30 min a 1h",
                        TreatmentDurationFilter.LONG to "D'1h a 2h",
                        TreatmentDurationFilter.EXTRA_LONG to "Més de 2h"
                    ),
                    onFilterChanged = { selectedDurationFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTreatments.isEmpty()) {
                EmptyTreatmentsState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                val groupedTreatments = remember(filteredTreatments) {
                    filteredTreatments.groupBy {
                        (it.name ?: "?").first().uppercaseChar()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedTreatments.forEach { (initial, treatmentGroup) ->
                        item { CharacterHeader(initial) }

                        items(treatmentGroup, key = { it.id ?: 0L }) { treatment ->
                            // AQUÍ ESTÁ EL CAMBIO A DESLIZAR PARA ELIMINAR
                            SwipeToDeleteContainer(
                                enableHintAnimation = false, // Puedes poner true si quieres la animación inicial
                                onDelete = {
                                    treatmentToDelete = treatment
                                }
                            ) {
                                TreatmentCard(
                                    treatment = treatment,
                                    onClick = { onTreatmentClick(treatment) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newDuration by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = { Text(text = "Nou Tractament", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Introdueix les dades del nou procediment clínic:", color = Color.Gray, fontSize = 14.sp)

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nom del Tractament") },
                        placeholder = { Text("Ex: Empastament simple") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ButtonPrimary,
                            focusedLabelColor = ButtonPrimary
                        )
                    )

                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it.filter { char -> char.isDigit() } },
                        label = { Text("Durada (minuts)") },
                        placeholder = { Text("Ex: 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ButtonPrimary,
                            focusedLabelColor = ButtonPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newDuration.isNotBlank()) {
                            val durationInt = newDuration.toIntOrNull() ?: 30
                            viewModel.createTreatment(
                                Treatment(name = newName, durationMinutes = durationInt)
                            ) {
                                Toast.makeText(context, "Tractament creat", Toast.LENGTH_SHORT).show()
                                viewModel.getTreatments()
                                showAddDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Omple tots els camps", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Crear", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel·lar", color = Color.Gray) }
            }
        )
    }

    if (treatmentToDelete != null) {
        // En lugar del AlertDialog estático tuyo, usamos nuestro componente DeleteConfirmationDialog
        // Si no tienes este componente importado, déjalo con tu AlertDialog, pero te lo he adaptado aquí
        // para que sea igual en todos los sitios.
        AlertDialog(
            onDismissRequest = { treatmentToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = { Text(text = "Eliminar Tractament", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = {
                Text(
                    text = "Estàs segur que vols eliminar el tractament '${treatmentToDelete?.name}'? Aquesta acció no es pot desfer.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        treatmentToDelete?.id?.let { id ->
                            viewModel.deleteTreatment(id) {
                                Toast.makeText(context, "Tractament eliminat", Toast.LENGTH_SHORT).show()
                                treatmentToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { treatmentToDelete = null }) { Text("Cancel·lar", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun SearchTreatmentBar(textFieldState: TextFieldState) {
    val query = textFieldState.text.toString()

    OutlinedTextField(
        value = query,
        onValueChange = { text ->
            textFieldState.edit { replace(0, length, text) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        placeholder = { Text("Cercar tractament...", color = Color.Gray.copy(alpha = 0.8f)) },
        shape = RoundedCornerShape(30.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedBorderColor = Color(0xFFA0B2C0),
            focusedBorderColor = ButtonPrimary,
            cursorColor = ButtonPrimary
        ),
        singleLine = true
    )
}

@Composable
fun CharacterHeader(initial: Char) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF4F6F9),
        tonalElevation = 1.dp
    ) {
        Text(
            text = initial.toString(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ButtonPrimary
        )
    }
}

@Composable
private fun EmptyTreatmentsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No s'han trobat tractaments.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Revisa la cerca o el filtre.",
            color = Color.LightGray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TreatmentCard(treatment: Treatment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = treatment.name ?: "Sense nom",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${treatment.durationMinutes ?: 30} minuts",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Veure Protocol",
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