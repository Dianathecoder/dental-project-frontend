package com.example.dynalar_frontend_v1.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.theme.TextoPrincipal
import com.example.dynalar_frontend_v1.ui.theme.TextoSecundario



//Boton de navegacion global
@Composable
fun Navegate_Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF537895),
    contentColor: Color = Color.White,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconSize: Dp = 20.dp,
    height: Dp = 56.dp,
    cornerRadius: Dp = 12.dp,
    fillMaxWidth: Boolean = true,

) {
    Button(
        onClick = onClick,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f)
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
@Composable
fun AddButton(
    onClick: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 35.dp
) {
    Surface(
        modifier = modifier
            .size(iconSize)
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Añadir",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardMenuButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFF1A5BB2),
    textColor: Color = Color(0xFF2C3E50)
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomisableButtonMaterials(
    iconRes: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD)), // Azul clarito
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
//Desplejable en un futuro se tiene que enlazar con la base de datos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomisableDynamicDropdownMenu(
    selectedItem: T?,
    options: List<T>,
    label: String,
    displayText: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }


    var searchText by remember {
        mutableStateOf(selectedItem?.let { displayText(it) } ?: "")
    }


    LaunchedEffect(selectedItem) {
        searchText = selectedItem?.let { displayText(it) } ?: ""
    }


    val filteredOptions = options.filter {
        displayText(it).contains(searchText, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {

        OutlinedTextField(
            readOnly = false,
            value = searchText,
            onValueChange = {
                searchText = it
                expanded = true
            },
            label = { Text(label, fontWeight = FontWeight.Bold) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )


        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false

                    searchText = selectedItem?.let { displayText(it) } ?: ""
                }
            ) {
                filteredOptions.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(displayText(item)) },
                        onClick = {
                            searchText = displayText(item)
                            expanded = false
                            onItemSelected(item) // Llama al callback original
                        }
                    )
                }
            }
        }
    }
}

//Forma de arriba de los perfiles
@Composable
fun BannerGenericProfile(
    userName: String,
    userRole: String,
    profileImage: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    onNavigateBack: () -> Unit,
    bannerColor: Color = Color.White,
    textColor: Color = TextoPrincipal
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
                clip = false,
                ambientColor = Color(0xFFBCAF9F),
                spotColor = Color(0xFFBCAF9F)
            )
            .background(
                color = bannerColor,
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
            )
    ) {
        CustomTopBar(
            title = "Perfil del Usuari",
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            ) {
                profileImage()
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userRole,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}
//Boton para ir para atras con texto
@Composable
fun CustomTopBar(
    title: String,
    titleFontSize: TextUnit = 20.sp,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 35.dp, top = 40.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { onNavigateBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(
                onNavigateBack = onNavigateBack,
                iconRes = R.drawable.general_volver
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold
            )
        }

    }
}

@Composable
fun BackButton(
    onNavigateBack: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(25.dp)
            .clip(CircleShape)
            .clickable { onNavigateBack() },

        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Volver",
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Fit
        )
    }
}

