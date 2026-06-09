package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel

@Composable
fun StockPage(
    materialId: Long,
    viewModel: MaterialViewModel,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(materialId) {
        viewModel.getMaterialById(materialId)
    }

    val uiState = viewModel.materialDetailState

    Scaffold(
        topBar = {
            CustomTopBar(title = "Detall del Material", onNavigateBack = onBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is InterfaceGlobal.Loading -> CircularProgressIndicator(color = ButtonPrimary)
                is InterfaceGlobal.Success -> {
                    val material = uiState.data

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEFF3F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(50.dp), tint = ButtonPrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = material.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(32.dp))

                        var tempStockString by remember(material.availableStock) {
                            mutableStateOf(material.availableStock.toString())
                        }

                        val tempStock = tempStockString.toIntOrNull() ?: 0
                        val hasChanged = tempStock != material.availableStock

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Gestió d'Estoc", fontWeight = FontWeight.SemiBold, color = ButtonPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Espaciado ajustado
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (tempStock > 0) tempStockString = (tempStock - 1).toString()
                                        },
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Restar", tint = ButtonPrimary)
                                    }

                                    OutlinedTextField(
                                        value = tempStockString,
                                        onValueChange = { newValue ->
                                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                                tempStockString = newValue
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.width(100.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ButtonPrimary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )

                                    // Botón Sumar
                                    IconButton(
                                        onClick = {
                                            tempStockString = (tempStock + 1).toString()
                                        },
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = ButtonPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // El botón de actualizar se mantiene igual, usará el 'tempStock' convertido a Int
                        Button(
                            onClick = {
                                val diff = tempStock - material.availableStock
                                if (diff > 0) {
                                    viewModel.increaseStock(material.id!!, diff)
                                } else if (diff < 0) {
                                    viewModel.decreaseStock(material.id!!, -diff)
                                }
                            },
                            enabled = hasChanged,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasChanged) ButtonPrimary else Color.LightGray
                            )
                        ) {
                            Text("Actualitzar Estoc")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoCard(label = "Estoc Mínim", value = material.minimumStock.toString(), modifier = Modifier.weight(1f))
                        }

                        if (tempStock <= material.minimumStock) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Alerta: Estoc sota el mínim requerit.", color = Color.Red, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar Material")
                        }
                    }
                }
                is InterfaceGlobal.Error -> Text("Error: ${uiState.message}", Modifier.align(Alignment.Center), color = Color.Red)
                else -> {}
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            message = "Estàs segur que vols eliminar aquest material?",
            onConfirm = {
                viewModel.deleteMaterial(materialId)
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ButtonPrimary)
        }
    }
}