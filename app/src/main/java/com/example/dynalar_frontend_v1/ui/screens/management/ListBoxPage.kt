package com.example.dynalar_frontend_v1.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.Box
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.SharedFilterDropdown
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.BoxViewModel
import com.example.dynalar_frontend_v1.R

// Enumerador simple para la página de boxes
enum class BoxFilter { ALL }

@Composable
fun BoxPage(
    viewModel: BoxViewModel = viewModel(),
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var boxToDelete by remember { mutableStateOf<Box?>(null) }

    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.getAllBoxes()
    }

    val uiState = viewModel.boxesState

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. TOP BAR UNIFICADO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(end = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f, fill = false)) {
                    CustomTopBar(title = stringResource(id = R.string.box_title), onNavigateBack = onBack)
                }
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(44.dp).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Afegir Box",
                        tint = ButtonPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FILTRO REUTILIZABLE (Solo para ordenar en este caso)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                SharedFilterDropdown(
                    selectedFilter = BoxFilter.ALL,
                    defaultFilter = BoxFilter.ALL,
                    sortAscending = sortAscending,
                    filterLabel = "Tots els boxes",
                    filterSectionTitle = "Filtres disponibles",
                    filterOptions = listOf(BoxFilter.ALL to "Tots els boxes"),
                    onFilterChanged = { },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CONTENIDO Y LISTA
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is InterfaceGlobal.Idle, is InterfaceGlobal.Loading -> {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                    is InterfaceGlobal.Success -> {
                        val validBoxes = uiState.data.filter { it.number != null }

                        if (validBoxes.isEmpty()) {
                            EmptyBoxesState(modifier = Modifier.fillMaxSize())
                        } else {
                            // Lógica de Ordenación
                            val sortedBoxes = if (sortAscending) validBoxes.sortedBy { it.number } else validBoxes.sortedByDescending { it.number }

                            // Agrupamos por el primer dígito para mantener el diseño
                            val groupedBoxes = sortedBoxes.groupBy { it.number.toString().first() }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                groupedBoxes.forEach { (initial, boxList) ->
                                    item { CharacterHeaderBox(initial) }
                                    items(boxList, key = { it.number ?: 0L }) { box ->
                                        BoxItem(
                                            box = box,
                                            onDelete = {
                                                boxToDelete = box
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is InterfaceGlobal.Error -> Text(text = "Error: ${uiState.message}", color = Color.Red)
                    else -> {}
                }
            }
        }
    }

    if (showAddDialog) {
        CreateBoxDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { number ->
                viewModel.createBox(Box(number = number))
                showAddDialog = false
            }
        )
    }

    if (showDeleteDialog && boxToDelete != null) {
        DeleteConfirmationDialog(
            message = stringResource(id = R.string.box_delete_confirm, boxToDelete?.number ?: 0),
            onConfirm = {
                boxToDelete?.number?.let { viewModel.deleteBox(it) }
                showDeleteDialog = false
                boxToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                boxToDelete = null
            }
        )
    }

    val errorMessage = viewModel.errorMessage
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(stringResource(R.string.dialog_notice)) },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text(stringResource(R.string.dialog_ok)) } }
        )
    }
}

@Composable
fun CharacterHeaderBox(initial: Char) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Text(
            text = initial.toString(),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ButtonPrimary
        )
    }
}

@Composable
fun BoxItem(box: Box, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Box ${box.number}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun CreateBoxDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var numberText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.box_new_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(id = R.string.box_new_dialog_msg), color = Color.Gray, fontSize = 14.sp)
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) numberText = it },
                    label = { Text("Número del Box") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { numberText.toLongOrNull()?.let { onConfirm(it) } },
                enabled = numberText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) { Text(stringResource(id = R.string.btn_create), color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.btn_cancel), color = Color.Gray) }
        },
        containerColor = Color.White
    )
}

@Composable
private fun EmptyBoxesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MeetingRoom,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.box_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}