package com.example.dynalar_frontend_v1.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                CustomTopBar(title = stringResource(R.string.stock_detail_title), onNavigateBack = onBack)
            }
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
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFEBF4FF)), // Azul súper suave
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(45.dp), tint = ButtonPrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = material.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), textAlign = TextAlign.Center)

                        if (!material.category.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = material.category, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        var tempStockString by remember(material.availableStock) {
                            mutableStateOf(material.availableStock.toString())
                        }

                        val tempStock = tempStockString.toIntOrNull() ?: 0
                        val hasChanged = tempStock != material.availableStock

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp), // AÑADIDO: fillMaxWidth para que el centro sea real
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // AÑADIDO: textAlign Center y fillMaxWidth al texto
                                Text(
                                    text = stringResource(R.string.stock_management),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (tempStock > 0) tempStockString = (tempStock - 1).toString() },
                                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).size(48.dp)
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
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = Color(0xFF1E293B)
                                        ),
                                        modifier = Modifier.width(100.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ButtonPrimary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        )
                                    )

                                    IconButton(
                                        onClick = { tempStockString = (tempStock + 1).toString() },
                                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = ButtonPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // BOTÓN GUARDAR
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
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary,
                                disabledContainerColor = Color(0xFFE2E8F0)
                            )
                        ) {
                            Text(stringResource(R.string.btn_save), color = if (hasChanged) Color.White else Color(0xFF94A3B8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        InfoCard(
                            label = stringResource(R.string.stock_min_label),
                            value = material.minimumStock.toString(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // TARJETA DE ALERTA DE STOCK
                        if (tempStock <= material.minimumStock) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.stock_alert_low), color = Color(0xFFDC2626), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // BOTÓN DE BORRAR
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_delete_material), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is InterfaceGlobal.Error -> Text(stringResource(R.string.error_msg_format, uiState.message ?: ""), Modifier.align(Alignment.Center), color = Color.Red)
                else -> {}
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            message = stringResource(R.string.stock_delete_confirm),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}