package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
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
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.model.TreatmentMaterial
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel

@Composable
fun ProtocolPage(
    treatmentId: Long,
    materialViewModel: MaterialViewModel,
    viewModel: TreatmentViewModel,
    onBack: () -> Unit,
) {
    var showMaterialSelector by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var materialToDelete by remember { mutableStateOf<TreatmentMaterial?>(null) }
    var materialToUpdate by remember { mutableStateOf<TreatmentMaterial?>(null) }

    LaunchedEffect(treatmentId) {
        viewModel.getTreatmentById(treatmentId)
    }

    val uiState = viewModel.uiStateTreatmentDetail

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Detalls del Protocol",
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is InterfaceGlobal.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is InterfaceGlobal.Success -> {
                    val treatment = uiState.data
                    val materials = treatment.materials ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Navegate_Button(
                            text = "Afegir material",
                            onClick = {
                                materialViewModel.getAllMaterials()
                                showMaterialSelector = true
                            },
                            modifier = Modifier.align(Alignment.End),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = treatment.name ?: "Protocol",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (materials.isEmpty()) {
                            EmptyMaterialsState(modifier = Modifier.fillMaxSize())
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(materials, key = { it.material.id }) { item ->
                                    SwipeToDeleteContainer(
                                        onDelete = {
                                            materialToDelete = item
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        TreatmentMaterialCard(
                                            treatmentMaterial = item,
                                            onClick = {
                                                materialToUpdate = item
                                                showUpdateDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Text("Error: ${uiState.message}", Modifier.align(Alignment.Center), color = Color.Red)
                }
                else -> {}
            }
        }
    }

    if (showMaterialSelector) {
        MaterialSelectorDialog(
            viewModel = materialViewModel,
            onDismiss = { showMaterialSelector = false },
            onMaterialSelected = { material ->
                viewModel.addMaterialToTreatment(treatmentId, material.id, 1)
                showMaterialSelector = false
            }
        )
    }

    if (showUpdateDialog && materialToUpdate != null) {
        UpdateQuantityDialog(
            materialName = materialToUpdate!!.material.name,
            currentQuantity = materialToUpdate!!.quantityRequired,
            onDismiss = { showUpdateDialog = false },
            onConfirm = { newQuantity ->
                viewModel.updateMaterialToTreatment(treatmentId, materialToUpdate!!.material.id, newQuantity)
                showUpdateDialog = false
                materialToUpdate = null
            }
        )
    }

    if (showDeleteDialog && materialToDelete != null) {
        DeleteConfirmationDialog(
            message = "Estàs segur que vols eliminar ${materialToDelete?.material?.name} d'aquest protocol?",
            onConfirm = {
                viewModel.deleteMaterialToTreatment(treatmentId, materialToDelete!!.material.id)
                showDeleteDialog = false
                materialToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                materialToDelete = null
            }
        )
    }
}

@Composable
fun UpdateQuantityDialog(
    materialName: String,
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf(currentQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar quantitat") },
        text = {
            Column {
                Text("Indica la quantitat necessària per a $materialName:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantitat") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityText.toIntOrNull() ?: 1
                    onConfirm(q)
                }
            ) {
                Text("Guardar")
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
fun MaterialSelectorDialog(
    viewModel: MaterialViewModel,
    onDismiss: () -> Unit,
    onMaterialSelected: (Material) -> Unit
) {
    val materialsState = viewModel.materialsState

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Material") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()) {
                when (materialsState) {
                    is InterfaceGlobal.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is InterfaceGlobal.Success -> {
                        LazyColumn {
                            items(materialsState.data) { material ->
                                ListItem(
                                    headlineContent = { Text(material.name) },
                                    supportingContent = { Text("Stock: ${material.availableStock}") },
                                    modifier = Modifier.clickable { onMaterialSelected(material) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    is InterfaceGlobal.Error -> {
                        Text("Error: ${materialsState.message}", color = Color.Red)
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tancar") }
        }
    )
}

@Composable
fun TreatmentMaterialCard(
    treatmentMaterial: TreatmentMaterial,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = treatmentMaterial.material.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Quantitat: ${treatmentMaterial.quantityRequired}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                val stockColor = if (treatmentMaterial.material.availableStock < treatmentMaterial.quantityRequired) Color.Red else Color(0xFF7A7A7A)
                Text(
                    text = "Stock disponible: ${treatmentMaterial.material.availableStock}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = stockColor
                )
            }
        }
    }
}

@Composable
private fun EmptyMaterialsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory, null, Modifier.size(72.dp), tint = ButtonPrimary)
            Spacer(Modifier.height(10.dp))
            Text("No hi ha materials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Afegeix materials per veure'ls aquí.", color = Color.Gray)
        }
    }
}