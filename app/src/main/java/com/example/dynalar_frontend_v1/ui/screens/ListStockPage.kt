package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Material
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ButtonPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Afegir Material")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            CustomTopBar(
                title = "Estoc de Materials",
                onNavigateBack = onBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is InterfaceGlobal.Idle -> { }
                    is InterfaceGlobal.Loading -> CircularProgressIndicator(color = ButtonPrimary)
                    is InterfaceGlobal.Success -> {
                        val groupedMaterials = uiState.data
                            .filter { it.name.isNotBlank() }
                            .groupBy { it.name.first().uppercaseChar() }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                        ) {
                            groupedMaterials.forEach { (initial, materialList) ->
                                item { CharacterHeaderStock(initial) }
                                items(materialList, key = { it.id}) { material ->
                                    MaterialStockItem(
                                        material = material,
                                        onClick = { onMaterialClick(material) }
                                    )
                                }
                            }
                        }
                    }
                    is InterfaceGlobal.Error -> Text("Error: ${uiState.message}", color = Color.Red)
                    else -> {}
                }
            }
        }
    }

    if (showAddDialog) {
        CreateMaterialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, minStock ->
                viewModel.createMaterial(Material(id = 0, name = name, minimumStock = minStock, availableStock = 0))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CharacterHeaderStock(initial: Char) {
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
fun MaterialStockItem(material: Material, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick() },
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
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = material.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                val isLowStock = material.availableStock <= material.minimumStock
                Text(
                    text = "Estoc: ${material.availableStock}",
                    fontSize = 14.sp,
                    color = if (isLowStock) Color.Red else Color.Gray,
                    fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CreateMaterialDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nou Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Estoc Mínim") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, minStock.toIntOrNull() ?: 0) }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·lar") }
        }
    )
}