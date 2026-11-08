package com.example.dynalar_frontend_v1.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.Material
import com.example.dynalar_frontend_v1.model.management.MaterialGroupFilter
import com.example.dynalar_frontend_v1.model.management.TreatmentMaterial
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
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
    var showAssignDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var materialToAssign by remember { mutableStateOf<Material?>(null) }
    var materialToDelete by remember { mutableStateOf<TreatmentMaterial?>(null) }
    var materialToUpdate by remember { mutableStateOf<TreatmentMaterial?>(null) }

    LaunchedEffect(treatmentId) {
        viewModel.getTreatmentById(treatmentId)
    }

    val uiState = viewModel.uiStateTreatmentDetail

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
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomTopBar(
                            title = stringResource(id = R.string.protocol_detail_title),
                            onNavigateBack = onBack
                        )
                    }
                    // Botón para añadir material ahora está aquí arriba
                    IconButton(
                        onClick = {
                            materialViewModel.getAllMaterials()
                            showMaterialSelector = true
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = "Añadir Material",
                            tint = ButtonPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is InterfaceGlobal.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ButtonPrimary)
                }
                is InterfaceGlobal.Success -> {
                    val treatment = uiState.data
                    val materials = treatment.materials ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Rediseño del título del tratamiento
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = treatment.name ?: stringResource(id = R.string.protocols_title),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ButtonPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Protocols Assignats",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (materials.isEmpty()) {
                            EmptyMaterialsState(modifier = Modifier.fillMaxSize())
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(materials, key = { it.material.id!! }) { item ->
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
                    Text(
                        stringResource(id = R.string.error_msg_format, uiState.message ?: ""),
                        Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
                else -> {}
            }
        }
    }

    // DIÁLOGO 1: SELECCIONAR MATERIAL
    if (showMaterialSelector) {
        MaterialSelectorDialogWithGroups(
            viewModel = materialViewModel,
            onDismiss = { showMaterialSelector = false },
            onMaterialSelected = { material ->
                materialToAssign = material
                showMaterialSelector = false
                showAssignDialog = true
            }
        )
    }

    // DIÁLOGO 2: ASIGNAR CANTIDAD INICIAL (Se abre justo después de seleccionar)
    if (showAssignDialog && materialToAssign != null) {
        AssignQuantityDialog(
            material = materialToAssign!!,
            onDismiss = {
                showAssignDialog = false
                materialToAssign = null
            },
            onConfirm = { quantity ->
                viewModel.addMaterialToTreatment(treatmentId, materialToAssign!!.id!!, quantity)
                showAssignDialog = false
                materialToAssign = null
            }
        )
    }

    // DIÁLOGO: EDITAR CANTIDAD EXISTENTE
    if (showUpdateDialog && materialToUpdate != null) {
        AssignQuantityDialog(
            material = materialToUpdate!!.material,
            initialQuantity = materialToUpdate!!.quantityRequired.toString(),
            onDismiss = { showUpdateDialog = false },
            onConfirm = { newQuantity ->
                viewModel.updateMaterialToTreatment(treatmentId, materialToUpdate!!.material.id!!, newQuantity)
                showUpdateDialog = false
                materialToUpdate = null
            }
        )
    }

    // DIÁLOGO: CONFIRMAR ELIMINACIÓN
    if (showDeleteDialog && materialToDelete != null) {
        DeleteConfirmationDialog(
            message = stringResource(id = R.string.protocol_delete_confirm, materialToDelete?.material?.name ?: ""),
            onConfirm = {
                viewModel.deleteMaterialToTreatment(treatmentId, materialToDelete!!.material.id!!)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSelectorDialogWithGroups(
    viewModel: MaterialViewModel,
    onDismiss: () -> Unit,
    onMaterialSelected: (Material) -> Unit
) {
    val materialsState = viewModel.materialsState

    // Categorías disponibles incluyendo TODAS
    val categories = MaterialGroupFilter.values().map { it.displayName }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.protocol_select_material_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 450.dp).fillMaxWidth()) {
                // Selector de Grupo
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar per Grup") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Materiales Filtrados
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (materialsState) {
                        is InterfaceGlobal.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ButtonPrimary)
                        }
                        is InterfaceGlobal.Success -> {
                            // Aplicamos el filtro por la categoría seleccionada
                            val filteredMaterials = if (selectedCategory == MaterialGroupFilter.ALL.displayName) {
                                materialsState.data
                            } else {
                                materialsState.data.filter { it.category == selectedCategory }
                            }

                            if(filteredMaterials.isEmpty()) {
                                Text("No hi ha materials en aquesta categoria.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                            } else {
                                LazyColumn {
                                    items(filteredMaterials) { material ->
                                        ListItem(
                                            headlineContent = { Text(material.name, fontWeight = FontWeight.SemiBold) },
                                            supportingContent = { Text(stringResource(id = R.string.stock_available, material.availableStock), color = Color.Gray) },
                                            modifier = Modifier.clickable { onMaterialSelected(material) }
                                        )
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                        is InterfaceGlobal.Error -> {
                            Text(stringResource(id = R.string.error_msg_format, materialsState.message ?: ""), color = Color.Red, textAlign = TextAlign.Center)
                        }
                        else -> {}
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.protocol_btn_close), color = Color.Gray) }
        },
        containerColor = Color.White
    )
}

@Composable
fun AssignQuantityDialog(
    material: Material,
    initialQuantity: String = "1",
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf(initialQuantity) }
    val q = quantityText.toIntOrNull() ?: 0
    val isExceeding = q > material.availableStock

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.protocol_edit_qty_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Material: ${material.name}", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                Text("Estoc disponible: ${material.availableStock}", color = Color.Gray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(id = R.string.protocol_qty_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = isExceeding // Pone el borde rojo si te pasas
                )

                if (isExceeding) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No hi ha suficient estoc per aquesta quantitat.", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(q) },
                enabled = q > 0 && !isExceeding, // Deshabilita el botón si no hay stock suficiente
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) {
                Text(stringResource(id = R.string.btn_save), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.btn_cancel), color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun TreatmentMaterialCard(
    treatmentMaterial: TreatmentMaterial,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEFF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = treatmentMaterial.material.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val isLowStock = treatmentMaterial.material.availableStock < treatmentMaterial.quantityRequired
                val stockColor = if (isLowStock) Color(0xFFD32F2F) else Color.Gray

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Necessaris: ${treatmentMaterial.quantityRequired}",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = " • ", color = Color.LightGray)
                    Text(
                        text = "Estoc: ${treatmentMaterial.material.availableStock}",
                        fontSize = 12.sp,
                        color = stockColor,
                        fontWeight = if(isLowStock) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMaterialsState(modifier: Modifier = Modifier) {
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
            text = "No hi ha materials",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Prem a la icona de dalt a la dreta per començar a assignar material a aquest tractament.",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}