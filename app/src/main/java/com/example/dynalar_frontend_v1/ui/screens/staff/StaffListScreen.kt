package com.example.dynalar_frontend_v1.ui.screens.staff

import StaffItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.filter.StaffRoleFilter
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.AddButton
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.SharedFilterDropdown
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    staffList: List<User>,
    onNavigateBack: () -> Unit,
    onNavigateToAddUser: () -> Unit = {},
    onNavigateToStaffProfile: (Long) -> Unit,
    onDeleteStaff: (Long) -> Unit = {}
) {
    val searchState = rememberTextFieldState()
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var staffToDelete by remember { mutableStateOf<Long?>(null) }

    var selectedRoleFilter by remember { mutableStateOf(StaffRoleFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")
    val isAdmin = sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN") && !isSuperAdmin && !isOwner
    val isAuxiliar = sessionManager.hasRole("AUXILIAR") || sessionManager.hasRole("ROLE_AUXILIAR")

    val canManageStaff = isSuperAdmin || isOwner || isAdmin
    val query = searchState.text.toString().trim()

    val filteredStaff = remember(staffList, query, selectedRoleFilter, sortAscending, isSuperAdmin, isOwner, isAdmin, isAuxiliar) {
        staffList
            .filter { user ->
                val roles = user.roles.map { it.uppercase() }
                when {
                    isSuperAdmin -> true
                    isOwner -> !roles.contains("SUPERADMIN")
                    isAdmin -> !roles.contains("SUPERADMIN") && !roles.contains("OWNER")
                    isAuxiliar -> roles.contains("DOCTOR") || roles.contains("DENTIST") || roles.contains("AUXILIAR")
                    else -> false
                }
            }
            .filter { user ->
                val fullName = "${user.name ?: ""} ${user.surname ?: ""}".trim()
                val emailVal = user.email ?: ""
                if (query.isBlank()) true else fullName.contains(query, ignoreCase = true) || emailVal.contains(query, ignoreCase = true)
            }
            .filter { user ->
                val roles = user.roles.map { it.uppercase() }
                when (selectedRoleFilter) {
                    StaffRoleFilter.ALL -> true
                    StaffRoleFilter.SUPERADMIN -> roles.any { it.contains("SUPERADMIN") }
                    StaffRoleFilter.OWNER -> roles.any { it.contains("OWNER") }
                    StaffRoleFilter.ADMIN -> roles.any { it.contains("ADMIN") && !it.contains("SUPERADMIN") }
                    StaffRoleFilter.DOCTOR -> roles.any { it.contains("DOCTOR") || it.contains("DENTIST") }
                    StaffRoleFilter.AUXILIAR -> roles.any { it.contains("AUXILIAR") }
                }
            }
            .let { list ->
                if (sortAscending) list.sortedBy { (it.name ?: "").uppercase() }
                else list.sortedByDescending { (it.name ?: "").uppercase() }
            }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StaffTopBar(
                onNavigateToAddUser = onNavigateToAddUser,
                onNavigateBack = onNavigateBack,
                showAddButton = canManageStaff
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchStaffBar(textFieldState = searchState)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // DEFINICIÓN DE OPCIONES DE FILTRO BASADAS EN EL ROL DEL USUARIO ACTUAL
                val filterOptions = mutableListOf<Pair<StaffRoleFilter, String>>(
                    StaffRoleFilter.ALL to stringResource(id = R.string.filter_role_all)
                )
                if (isSuperAdmin) {
                    filterOptions.add(StaffRoleFilter.SUPERADMIN to stringResource(id = R.string.role_superadmin))
                }
                if (isSuperAdmin || isOwner) {
                    filterOptions.add(StaffRoleFilter.OWNER to stringResource(id = R.string.filter_owners))
                    filterOptions.add(StaffRoleFilter.ADMIN to stringResource(id = R.string.filter_admins))
                }
                filterOptions.add(StaffRoleFilter.DOCTOR to stringResource(id = R.string.filter_doctors))
                filterOptions.add(StaffRoleFilter.AUXILIAR to stringResource(id = R.string.filter_auxiliars))

                val filterLabel = when (selectedRoleFilter) {
                    StaffRoleFilter.ALL -> stringResource(id = R.string.filter_label)
                    StaffRoleFilter.SUPERADMIN -> stringResource(id = R.string.role_superadmin)
                    StaffRoleFilter.OWNER -> stringResource(id = R.string.filter_owners)
                    StaffRoleFilter.ADMIN -> stringResource(id = R.string.filter_admins)
                    StaffRoleFilter.DOCTOR -> stringResource(id = R.string.filter_doctors)
                    StaffRoleFilter.AUXILIAR -> stringResource(id = R.string.filter_auxiliars)
                }

                SharedFilterDropdown(
                    selectedFilter = selectedRoleFilter,
                    defaultFilter = StaffRoleFilter.ALL,
                    sortAscending = sortAscending,
                    filterLabel = filterLabel,
                    filterSectionTitle = stringResource(id = R.string.filter_role_header),
                    filterOptions = filterOptions,
                    onFilterChanged = { selectedRoleFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredStaff.isEmpty()) {
                EmptyStaffState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                val groupedStaff = remember(filteredStaff) {
                    filteredStaff.groupBy {
                        (it.name ?: it.surname ?: "?").first().uppercaseChar()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedStaff.forEach { (initial, staffGroup) ->
                        item { CharacterHeader(initial) }

                        items(staffGroup, key = { it.id ?: 0L }) { staff ->
                            if (canManageStaff) {
                                SwipeToDeleteContainer(
                                    onDelete = {
                                        staffToDelete = staff.id
                                        showDeleteDialog = true
                                    }
                                ) {
                                    StaffItem(
                                        staff = staff,
                                        onClick = { selected ->
                                            selected.id?.let { onNavigateToStaffProfile(it) }
                                        }
                                    )
                                }
                            } else {
                                StaffItem(
                                    staff = staff,
                                    onClick = { selected ->
                                        selected.id?.let { onNavigateToStaffProfile(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && staffToDelete != null) {
        DeleteConfirmationDialog(
            message = "Estàs segur que vols eliminar aquest empleat?",
            onConfirm = {
                staffToDelete?.let { id -> onDeleteStaff(id) }
                showDeleteDialog = false
                staffToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                staffToDelete = null
            }
        )
    }
}

@Composable
fun StaffTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    showAddButton: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(end = 14.dp, bottom = 8.dp), // Un poco de padding inferior para que respire
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            CustomTopBar(
                title = stringResource(id = R.string.staff_list_title),
                onNavigateBack = onNavigateBack
            )
        }

        if (showAddButton) {
            AddButton(
                onClick = onNavigateToAddUser,
                iconRes = R.drawable.person_add,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
@Composable
fun SearchStaffBar(
    textFieldState: TextFieldState
) {
    val query = textFieldState.text.toString()

    OutlinedTextField(
        value = query,
        onValueChange = { text ->
            textFieldState.edit { replace(0, length, text) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
        placeholder = {
            Text(
                stringResource(id = R.string.search_employee),
                color = Color.Gray.copy(alpha = 0.8f)
            )
        },
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
private fun EmptyStaffState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Badge,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.no_records_found),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}