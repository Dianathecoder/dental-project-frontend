package com.example.dynalar_frontend_v1.ui.screens.management

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.Material
import com.example.dynalar_frontend_v1.model.management.MaterialGroupFilter
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.SharedFilterDropdown
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStockPage(
    viewModel: MaterialViewModel = viewModel(),
    onMaterialClick: (Material) -> Unit,
    onBack: () -> Unit
) {
    val searchState = rememberTextFieldState()
    val listState = rememberLazyListState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<Material?>(null) }

    // Estados del Filtro Reutilizable
    var selectedGroupFilter by remember { mutableStateOf(MaterialGroupFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.getAllMaterials()
    }

    val uiState = viewModel.materialsState
    val query = searchState.text.toString().trim()

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. TOP BAR UNIFICADO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(end = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    CustomTopBar(title = stringResource(R.string.stock_title), onNavigateBack = onBack)
                }
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(44.dp).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Afegir Material",
                        tint = ButtonPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. BUSCADOR
            SearchMaterialBar(textFieldState = searchState)

            Spacer(modifier = Modifier.height(8.dp))

            // 3. FILTRO REUTILIZABLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                val filterLabel = if (selectedGroupFilter == MaterialGroupFilter.ALL) "Filtrar" else selectedGroupFilter.displayName

                val filterOptions = MaterialGroupFilter.values().map { it to it.displayName }

                SharedFilterDropdown(
                    selectedFilter = selectedGroupFilter,
                    defaultFilter = MaterialGroupFilter.ALL,
                    sortAscending = sortAscending,
                    filterLabel = filterLabel,
                    filterSectionTitle = "Grup de material",
                    filterOptions = filterOptions,
                    onFilterChanged = { selectedGroupFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CONTENIDO Y LISTA
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is InterfaceGlobal.Loading, InterfaceGlobal.Idle -> {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                    is InterfaceGlobal.Success -> {
                        val validMaterials = uiState.data.filter { it.name.isNotBlank() }

                        // Lógica de Filtrado y Ordenación
                        val filteredMaterials = validMaterials
                            .filter { material ->
                                if (query.isBlank()) true else material.name.contains(query, ignoreCase = true)
                            }
                            .filter { material ->
                                if (selectedGroupFilter == MaterialGroupFilter.ALL) {
                                    true
                                } else {
                                    material.category == selectedGroupFilter.displayName
                                }
                            }
                            .let { list ->
                                if (sortAscending) list.sortedBy { it.name.uppercase() }
                                else list.sortedByDescending { it.name.uppercase() }
                            }

                        if (filteredMaterials.isEmpty()) {
                            EmptyMaterialsState(modifier = Modifier.fillMaxSize())
                        } else {
                            val groupedMaterials = filteredMaterials.groupBy { it.name.first().uppercaseChar() }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                groupedMaterials.forEach { (initial, materialList) ->
                                    item { CharacterHeaderStock(initial) }
                                    items(materialList, key = { it.id ?: it.hashCode() }) { material ->
                                        MaterialStockItem(
                                            material = material,
                                            onClick = { onMaterialClick(material) },
                                            onDelete = {
                                                materialToDelete = material
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is InterfaceGlobal.Error -> Text(text = uiState.message ?: "Error", color = Color.Red)
                    else -> {}
                }
            }
        }
    }

    if (showAddDialog) {
        CreateMaterialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, initStock, minStock, category ->
                val initStockInt = initStock.toIntOrNull() ?: 0
                val minStockInt = minStock.toIntOrNull() ?: 0
                viewModel.createMaterial(
                    Material(
                        id = 0,
                        name = name,
                        minimumStock = minStockInt,
                        availableStock = initStockInt, // Ahora recoge el valor real
                        category = category
                    )
                )
                showAddDialog = false
            }
        )
    }

    if (showDeleteDialog && materialToDelete != null) {
        DeleteConfirmationDialog(
            message = "¿Estàs segur que vols eliminar '${materialToDelete?.name}'? Aquesta acció no es pot desfer.",
            onConfirm = {
                materialToDelete?.id?.let { viewModel.deleteMaterial(it) }
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
fun SearchMaterialBar(textFieldState: TextFieldState) {
    val query = textFieldState.text.toString()

    OutlinedTextField(
        value = query,
        onValueChange = { text -> textFieldState.edit { replace(0, length, text) } },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        placeholder = { Text("Cercar material...", color = Color.Gray.copy(alpha = 0.8f)) },
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
            tint = Color(0xFFA0B2C0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No s'han trobat materials.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CharacterHeaderStock(initial: Char) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
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
fun MaterialStockItem(material: Material, onClick: () -> Unit, onDelete: () -> Unit) {
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
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, null, tint = ButtonPrimary, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = material.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))

                if (!material.category.isNullOrBlank()) {
                    Text(text = material.category, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val isLowStock = material.availableStock <= material.minimumStock
                Text(
                    text = stringResource(R.string.stock_available, material.availableStock),
                    fontSize = 13.sp,
                    color = if (isLowStock) Color(0xFFDC2626) else Color(0xFF64748B),
                    fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaterialDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var initStock by remember { mutableStateOf("") } // <-- NUEVO
    var minStock by remember { mutableStateOf("") }

    val categories = MaterialGroupFilter.values().filter { it != MaterialGroupFilter.ALL }.map { it.displayName }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nou Material", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.material_name_label)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grup / Categoria") },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Campo Estoc Inicial
                    OutlinedTextField(
                        value = initStock,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) initStock = it },
                        label = { Text("Estoc actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Campo Estoc Mínimo
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) minStock = it },
                        label = { Text("Estoc mínim") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, initStock, minStock, selectedCategory) },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                shape = RoundedCornerShape(8.dp)
            ) { Text(stringResource(R.string.btn_create), color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = Color.Gray) }
        },
        containerColor = Color.White
    )
}