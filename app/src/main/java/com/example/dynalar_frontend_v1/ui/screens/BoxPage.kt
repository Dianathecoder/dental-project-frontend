package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Box
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.BoxViewModel

@Composable
fun BoxPage(
    viewModel: BoxViewModel = viewModel(),
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var boxToDelete by remember { mutableStateOf<Box?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getAllBoxes()
    }

    val uiState = viewModel.boxesState

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            CustomTopBar(
                title = "Gestió de Boxes",
                onNavigateBack = onBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Navegate_Button(
                text = "Afegir Box",
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 22.dp),
                height = 40.dp,
                cornerRadius = 24.dp,
                fillMaxWidth = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is InterfaceGlobal.Idle -> {}
                    is InterfaceGlobal.Loading -> {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }

                    is InterfaceGlobal.Success -> {
                        val groupedBoxes = uiState.data
                            .filter { it.number != null }
                            .groupBy { it.number.toString().first() }

                        if (uiState.data.isEmpty()) {
                            EmptyBoxesState(modifier = Modifier.fillMaxSize())
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                groupedBoxes.forEach { (initial, boxList) ->
                                    item { CharacterHeaderBox(initial) }
                                    items(boxList, key = { it.number ?: 0L }) { box ->
                                        SwipeToDeleteContainer(
                                            onDelete = {
                                                boxToDelete = box
                                                showDeleteDialog = true
                                            }
                                        ) {
                                            BoxItem(box = box)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is InterfaceGlobal.Error -> {
                        Text(
                            text = "Error: ${uiState.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

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
            message = "Estàs segur que vols eliminar el box número ${boxToDelete?.number}?",
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
            title = { Text("Avís") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("D'acord")
                }
            }
        )
    }
}

@Composable
fun CharacterHeaderBox(initial: Char) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF4F6F9),
        tonalElevation = 1.dp
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
fun BoxItem(box: Box) {
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
            Box(
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
        }
    }
}

@Composable
fun CreateBoxDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var numberText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nou Box") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Introdueix el número del nou box de la clínica:")
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) numberText = it },
                    label = { Text("Número del Box") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { numberText.toLongOrNull()?.let { onConfirm(it) } },
                enabled = numberText.isNotBlank()
            ) {
                Text("Crear")
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
            tint = ButtonPrimary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "No hi ha boxes configurats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
    }
}