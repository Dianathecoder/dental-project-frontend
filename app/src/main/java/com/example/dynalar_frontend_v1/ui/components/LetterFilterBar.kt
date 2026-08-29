
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFilterDropdown(
    selectedLetter: Char?,
    sortAscending: Boolean,
    onLetterSelected: (Char?) -> Unit,
    onSortChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilter = selectedLetter != null
    val label = if (hasFilter) selectedLetter.toString() else "Filtrar"

    // Colores basados en tu diseño gris
    val activeColor = ButtonPrimary // Mantenemos tu azul principal para cuando ESTÁ activo
    val inactiveIconTextColor = Color.Gray
    val inactiveBorderColor = Color(0xFFA0B2C0)
    val inactiveBgColor = Color.Transparent // F

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
                .heightIn(max = 400.dp)
                .background(Color.White)
        ) {
            Text(
                text = "Ordenar",
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
                text = "Letra",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            DropdownMenuItem(
                text = { Text("Todas", fontSize = 14.sp, color = if (selectedLetter == null) ButtonPrimary else Color.Black, fontWeight = if (selectedLetter == null) FontWeight.Bold else FontWeight.Normal) },
                onClick = { onLetterSelected(null); expanded = false },
                trailingIcon = { if (selectedLetter == null) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
            )
            ('A'..'Z').forEach { letter ->
                DropdownMenuItem(
                    text = { Text(letter.toString(), fontSize = 14.sp, color = if (selectedLetter == letter) ButtonPrimary else Color.Black, fontWeight = if (selectedLetter == letter) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { onLetterSelected(letter); expanded = false },
                    trailingIcon = { if (selectedLetter == letter) Icon(Icons.Default.Check, null, tint = ButtonPrimary, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}