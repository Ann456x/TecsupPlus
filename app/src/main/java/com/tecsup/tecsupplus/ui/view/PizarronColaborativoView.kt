package com.tecsup.tecsupplus.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.data.local.UsuarioEntity

data class TrazoPizarra(
    val puntos: List<Offset>,
    val color: Color,
    val grosor: Float
)

@Composable
fun PizarronColaborativoView(user: UsuarioEntity) {
    var salaActiva by remember { mutableStateOf("Grupo Cálculo") }
    var colorSeleccionado by remember { mutableStateOf(Color(0xFF0277BD)) }
    val grosorTrazo = 6f

    val trazos = remember { mutableStateListOf<TrazoPizarra>() }
    var puntosActuales by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val coloresPaleta = listOf(
        Color(0xFF0277BD),
        Color(0xFFC62828),
        Color(0xFF2E7D32),
        Color(0xFFEF6C00),
        Color(0xFF212121)
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Pizarrón Colaborativo en Tiempo Real", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
        Text("Sala: $salaActiva • Conectado: ${user.nombreCompleto}", fontSize = 11.sp, color = Color(0xFF2E7D32))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Grupo Cálculo", "Grupo Móviles", "Estudio Libre").forEach { sala ->
                Button(
                    onClick = { salaActiva = sala },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (salaActiva == sala) Color(0xFF0277BD) else Color(0xFFE1F5FE),
                        contentColor = if (salaActiva == sala) Color.White else Color(0xFF0277BD)
                    ),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(sala, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                coloresPaleta.forEach { col ->
                    Button(
                        onClick = { colorSeleccionado = col },
                        colors = ButtonDefaults.buttonColors(containerColor = col),
                        modifier = Modifier.size(30.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        trazos.add(
                            TrazoPizarra(
                                puntos = listOf(Offset(60f, 80f), Offset(180f, 80f), Offset(180f, 160f), Offset(60f, 160f), Offset(60f, 80f)),
                                color = Color(0xFF6A1B9A),
                                grosor = 5f
                            )
                        )
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("+ Esquema Grupo", fontSize = 10.sp)
                }
                Button(
                    onClick = {
                        trazos.clear()
                        puntosActuales = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Limpiar", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .pointerInput(colorSeleccionado) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                puntosActuales = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                puntosActuales = puntosActuales + change.position
                            },
                            onDragEnd = {
                                if (puntosActuales.size > 1) {
                                    trazos.add(TrazoPizarra(puntosActuales, colorSeleccionado, grosorTrazo))
                                }
                                puntosActuales = emptyList()
                            }
                        )
                    }
            ) {
                val step = 45f
                var x = step
                while (x < size.width) {
                    drawLine(Color(0xFFF1F5F9), Offset(x, 0f), Offset(x, size.height), 1f)
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(Color(0xFFF1F5F9), Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }

                trazos.forEach { trazo ->
                    if (trazo.puntos.size > 1) {
                        val path = Path().apply {
                            moveTo(trazo.puntos.first().x, trazo.puntos.first().y)
                            trazo.puntos.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, trazo.color, style = Stroke(width = trazo.grosor, cap = StrokeCap.Round))
                    }
                }

                if (puntosActuales.size > 1) {
                    val pathActual = Path().apply {
                        moveTo(puntosActuales.first().x, puntosActuales.first().y)
                        puntosActuales.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(pathActual, colorSeleccionado, style = Stroke(width = grosorTrazo, cap = StrokeCap.Round))
                }
            }
        }
    }
}