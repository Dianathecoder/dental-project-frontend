package com.example.dynalar_frontend_v1.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

@Composable
fun ValidationAndSignatureDialog(
    title: String = "Validació i Consentiment",
    consentTitle: String = "Consentiment Anestèsia:",
    consentText: String? = null,
    infectiousDeceases: String?,
    allergies: String?,
    isOptional: Boolean = true,
    onConfirm: (signatureBase64: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var resetKey by remember { mutableStateOf(0) }

    val hasSigned = strokes.isNotEmpty()


    val defaultConsentText = "Es confirma la revisió d'al·lèrgies (${allergies ?: "cap"}) i el consentiment per a l'ús d'anestèsics si fos necessari."
    val finalConsentText = consentText ?: defaultConsentText

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
        },
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Alerta Mèdica i Infecciosa:", fontWeight = FontWeight.Bold, color = Color.Red)
                Text(
                    text = if (infectiousDeceases.isNullOrBlank()) "Cap malaltia infecciosa registrada."
                    else "Malalties: $infectiousDeceases",
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(consentTitle, fontWeight = FontWeight.Bold)
                Text(
                    text = finalConsentText,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Signatura del Pacient/Tutor:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                SignaturePadControlled(
                    resetKey = resetKey,
                    strokes = strokes,
                    currentStroke = currentStroke,
                    onSizeDetected = { canvasSize = it }
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasSigned) "Signatura registrada" else "Dibuixeu la signatura al requadre",
                        fontSize = 12.sp,
                        color = if (hasSigned) Color(0xFF537895) else Color.LightGray
                    )
                    TextButton(onClick = {
                        strokes.clear()
                        currentStroke.clear()
                        resetKey++
                    }) {
                        Text("Esborrar", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOptional) {
                    TextButton(onClick = { onConfirm("") }) {
                        Text("Ometre firma", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    enabled = hasSigned,
                    onClick = {
                        val base64 = exportStrokesToBase64(strokes, canvasSize)
                        onConfirm(base64)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF537895))
                ) {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tornar")
            }
        }
    )
}
@Composable
fun SignaturePadControlled(
    resetKey: Int,
    strokes: MutableList<List<Offset>>,
    currentStroke: MutableList<Offset>,
    onSizeDetected: (IntSize) -> Unit
) {
    key(resetKey) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .border(1.dp, Color(0xFF537895).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                .pointerInput(resetKey) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke.clear()
                            currentStroke.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke.add(change.position)
                        },
                        onDragEnd = {
                            if (currentStroke.isNotEmpty()) {
                                strokes.add(currentStroke.toList())
                                currentStroke.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                onSizeDetected(IntSize(size.width.toInt(), size.height.toInt()))

                strokes.forEach { stroke ->
                    if (stroke.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.first().x, stroke.first().y)
                            stroke.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path = path, color = Color.Black, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                if (currentStroke.size > 1) {
                    val path = Path().apply {
                        moveTo(currentStroke.first().x, currentStroke.first().y)
                        currentStroke.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path = path, color = Color.Black, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            if (strokes.isEmpty() && currentStroke.isEmpty()) {
                Text(
                    "Signeu aqui",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun exportStrokesToBase64(strokes: List<List<Offset>>, size: IntSize): String {
    val w = if (size.width > 0) size.width else 600
    val h = if (size.height > 0) size.height else 200

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 4f
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }

    strokes.forEach { stroke ->
        if (stroke.size > 1) {
            val path = android.graphics.Path()
            path.moveTo(stroke.first().x, stroke.first().y)
            stroke.drop(1).forEach { path.lineTo(it.x, it.y) }
            canvas.drawPath(path, paint)
        }
    }

    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}
@Composable
fun SignatureView(signatureBase64: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Firma del Pacient",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
            shadowElevation = 2.dp
        ) {
            if (!signatureBase64.isNullOrBlank()) {

                val imageBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Firma del pacient",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text("No hi ha firma disponible", color = Color.LightGray)
                }
            }
        }
    }
}