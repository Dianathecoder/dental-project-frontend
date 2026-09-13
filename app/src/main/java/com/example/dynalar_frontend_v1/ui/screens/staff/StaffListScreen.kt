package com.example.dynalar_frontend_v1.ui.screens.staff

import StaffItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.filter.StaffRoleFilter
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.AddButton
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import androidx.compose.ui.text.style.TextAlign
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

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

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
                StaffFilterDropdown(
                    selectedRoleFilter = selectedRoleFilter,
                    sortAscending = sortAscending,
                    isSuperAdmin = isSuperAdmin,
                    isOwner = isOwner,
                    onRoleFilterChanged = { filter -> selectedRoleFilter = filter },
                    onSortChanged = { sort -> sortAscending = sort }
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
    Box(modifier = Modifier.fillMaxWidth()) {
        CustomTopBar(
            title = stringResource(id = R.string.staff_list_title),
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        if (showAddButton) {
            AddButton(
                onClick = onNavigateToAddUser,
                iconRes = R.drawable.person_add,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 50.dp, top = 25.dp)
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
fun StaffFilterDropdown(
    selectedRoleFilter: StaffRoleFilter,
    sortAscending: Boolean,
    isSuperAdmin: Boolean,
    isOwner: Boolean,
    onRoleFilterChanged: (StaffRoleFilter) -> Unit,
    onSortChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilter = selectedRoleFilter != StaffRoleFilter.ALL

    val label = when (selectedRoleFilter) {
        StaffRoleFilter.SUPERADMIN -> stringResource(id = R.string.role_superadmin)
        StaffRoleFilter.OWNER -> stringResource(id = R.string.filter_owners)
        StaffRoleFilter.ADMIN -> stringResource(id = R.string.filter_admins)
        StaffRoleFilter.DOCTOR -> stringResource(id = R.string.filter_doctors)
        StaffRoleFilter.AUXILIAR -> stringResource(id = R.string.filter_auxiliars)
        else -> stringResource(id = R.string.filter_label)
    }

    val activeColor = ButtonPrimary
    val inactiveIconTextColor = Color.Gray
    val inactiveBorderColor = Color(0xFFA0B2C0)

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = if (hasFilter) activeColor else Color.Transparent,
            border = BorderStroke(1.5.dp, if (hasFilter) activeColor else inactiveBorderColor),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (hasFilter) Color.White else inactiveIconTextColor
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasFilter) Color.White else inactiveIconTextColor
                )
                Icon(
                    imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (hasFilter) Color.White else inactiveIconTextColor
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(220.dp)
                .background(Color.White)
        ) {
            Text(
                text = stringResource(id = R.string.filter_order_header),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(14.dp), tint = if (sortAscending) ButtonPrimary else Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.filter_order_asc), fontSize = 14.sp, color = if (sortAscending) ButtonPrimary else Color.Black, fontWeight = if (sortAscending) FontWeight.Bold else FontWeight.Normal)
                    }
                },
                onClick = { onSortChanged(true); expanded = false },
                trailingIcon = { if (sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(14.dp), tint = if (!sortAscending) ButtonPrimary else Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.filter_order_desc), fontSize = 14.sp, color = if (!sortAscending) ButtonPrimary else Color.Black, fontWeight = if (!sortAscending) FontWeight.Bold else FontWeight.Normal)
                    }
                },
                onClick = { onSortChanged(false); expanded = false },
                trailingIcon = { if (!sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(id = R.string.filter_role_header),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // SE FUERZA EL TIPO DE LA LISTA AQUÍ PARA EVITAR EL ERROR DEL COMPILADOR
            val roleOptions = mutableListOf<Pair<StaffRoleFilter, String>>(
                StaffRoleFilter.ALL to stringResource(id = R.string.filter_role_all)
            )

            if (isSuperAdmin) {
                roleOptions.add(StaffRoleFilter.SUPERADMIN to stringResource(id = R.string.role_superadmin))
            }
            if (isSuperAdmin || isOwner) {
                roleOptions.add(StaffRoleFilter.OWNER to stringResource(id = R.string.filter_owners))
                roleOptions.add(StaffRoleFilter.ADMIN to stringResource(id = R.string.filter_admins))
            }
            roleOptions.add(StaffRoleFilter.DOCTOR to stringResource(id = R.string.filter_doctors))
            roleOptions.add(StaffRoleFilter.AUXILIAR to stringResource(id = R.string.filter_auxiliars))

            roleOptions.forEach { (filterOption, optionLabel) ->
                val isSelected = selectedRoleFilter == filterOption
                DropdownMenuItem(
                    text = {
                        Text(
                            optionLabel,
                            fontSize = 14.sp,
                            color = if (isSelected) ButtonPrimary else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onRoleFilterChanged(filterOption)
                        expanded = false
                    },
                    trailingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp))
                    }
                )
            }
        }
    }
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
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
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