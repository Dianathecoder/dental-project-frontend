package com.example.dynalar_frontend_v1.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary

@Composable
fun <T> SharedFilterDropdown(
    selectedFilter: T,
    defaultFilter: T,
    sortAscending: Boolean,
    filterLabel: String,
    filterOptions: List<Pair<T, String>>,
    filterSectionTitle: String,
    onFilterChanged: (T) -> Unit,
    onSortChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilter = selectedFilter != defaultFilter

    val activeColor = ButtonPrimary
    val inactiveIconTextColor = Color.Gray
    val inactiveBorderColor = Color(0xFFA0B2C0)
    val inactiveBgColor = Color.Transparent

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = if (hasFilter) activeColor else inactiveBgColor,
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
                    text = filterLabel,
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
                text = "Ordenació",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(14.dp), tint = if (sortAscending) ButtonPrimary else Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("A → Z", fontSize = 14.sp, color = if (sortAscending) ButtonPrimary else Color.Black, fontWeight = if (sortAscending) FontWeight.Bold else FontWeight.Normal)
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
                        Text("Z → A", fontSize = 14.sp, color = if (!sortAscending) ButtonPrimary else Color.Black, fontWeight = if (!sortAscending) FontWeight.Bold else FontWeight.Normal)
                    }
                },
                onClick = { onSortChanged(false); expanded = false },
                trailingIcon = { if (!sortAscending) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = filterSectionTitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            filterOptions.forEach { (filterOption, optionLabel) ->
                val isSelected = selectedFilter == filterOption
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
                        onFilterChanged(filterOption)
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