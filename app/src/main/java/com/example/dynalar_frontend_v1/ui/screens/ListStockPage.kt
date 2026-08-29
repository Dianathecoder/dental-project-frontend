package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel

@Composable
fun ListStockPage(
    viewModel: MaterialViewModel = viewModel(),
    onMaterialClick: (Material) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getAllMaterials()
    }

    val uiState = viewModel.materialsState

    Scaffold(
        // Añadimos el fondo gris claro para igualar el estilo de BoxPage
        containerColor = Color(0xFFF8F9FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            CustomTopBar(
                title = stringResource(R.string.stock_title),
                onNavigateBack = onBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Botón superior derecho (Add Material)
            Navegate_Button(
                text = stringResource(id = R.string.stock_add_btn),
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
                    is InterfaceGlobal.Idle -> { }
                    is InterfaceGlobal.Loading -> CircularProgressIndicator(color = ButtonPrimary)
                    is InterfaceGlobal.Success -> {
                        val validMaterials = uiState.data.filter { it.name.isNotBlank() }

                        // Condición de diseño vacío
                        if (validMaterials.isEmpty()) {
                            EmptyMaterialsState(modifier = Modifier.fillMaxSize())
                        } else {
                            val groupedMaterials = validMaterials.groupBy { it.name.first().uppercaseChar() }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                groupedMaterials.forEach { (initial, materialList) ->
                                    item { CharacterHeaderStock(initial) }
                                    items(materialList, key = { it.id ?: it.hashCode() }) { material ->
                                        MaterialStockItem(
                                            material = material,
                                            onClick = { onMaterialClick(material) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is InterfaceGlobal.Error -> Text(
                        text = stringResource(R.string.error_msg_format, uiState.message ?: ""),
                        color = Color.Red
                    )
                    else -> {} // Evita el error 'when expression must be exhaustive'
                }
            }
        }
    }

    if (showAddDialog) {
        CreateMaterialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, minStock ->
                val minStockInt = minStock.toIntOrNull() ?: 0
                viewModel.createMaterial(Material(id = 0, name = name, minimumStock = minStockInt, availableStock = 0))
                showAddDialog = false
            }
        )
    }
}

// Componente para el estado sin materiales (Diseño de la figura)
@Composable
private fun EmptyMaterialsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0) // Color azul-grisáceo suave para coincidir con el de BoxPage
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No configured materials",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
fun CharacterHeaderStock(initial: Char) {
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
fun MaterialStockItem(material: Material, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
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
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = material.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3E50))
                val isLowStock = material.availableStock <= material.minimumStock
                Text(
                    text = stringResource(R.string.stock_available, material.availableStock),
                    fontSize = 13.sp,
                    color = if (isLowStock) Color.Red else Color.Gray,
                    fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CreateMaterialDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nou Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.material_name_label)) },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            minStock = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.material_min_stock_label)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, minStock) },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) {
                Text(stringResource(R.string.btn_create), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}