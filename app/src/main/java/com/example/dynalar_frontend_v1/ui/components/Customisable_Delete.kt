package com.example.dynalar_frontend_v1.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.TextoPrincipal
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

//Pop-Up Eliminar
@Composable
fun DeleteConfirmationDialog(
    title: String = "Confirmar eliminació",
    message: String = "¿Estàs segur que vols eliminar aquest element? Aquesta acció no es pot desfer.",
    confirmText: String = "Sí, eliminar",
    cancelText: String = "Cancel·lar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Cierra el diálogo si tocas fuera de él
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
        },
        text = {
            Text(
                text = message,
                color = Color.DarkGray
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {

                Text(text = confirmText, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {

                Text(text = cancelText, color = ButtonPrimary)
            }
        }
    )
}
//Eliminar los objetos de las listas


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    backgroundColor: Color = Color.Red,
    enableHintAnimation: Boolean = false,
    hintAlreadyShown: Boolean = false,
    onHintShown: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else {
                true
            }
        }
    )

    val hintOffset = remember { Animatable(0f) }

    LaunchedEffect(enableHintAnimation, hintAlreadyShown) {
        if (enableHintAnimation && !hintAlreadyShown) {

            delay(400)

            hintOffset.animateTo(
                targetValue = -180f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = EaseInOutCubic
                )
            )


            delay(400)


            hintOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = EaseInOutQuart
                )
            )

            onHintShown()
        }
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(28.dp)
                )
            }
        }
    ) {
        Box(modifier = Modifier.offset { IntOffset(hintOffset.value.roundToInt(), 0) }) {
            content()
        }
    }
}