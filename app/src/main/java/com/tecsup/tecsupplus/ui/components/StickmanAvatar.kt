package com.tecsup.tecsupplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val stickmanColores = listOf(
    "Azul Tecsup" to Color(0xFF0277BD),
    "Rojo Carmesí" to Color(0xFFC62828),
    "Verde Esmeralda" to Color(0xFF2E7D32),
    "Dorado Solar" to Color(0xFFF9A825),
    "Morado Neón" to Color(0xFF6A1B9A),
    "Naranja Fuego" to Color(0xFFEF6C00),
    "Cian Eléctrico" to Color(0xFF00838F),
    "Rosa Coral" to Color(0xFFAD1457),
    "Negro Ninja" to Color(0xFF212121),
    "Gris Titanio" to Color(0xFF455A64)
)

val stickmanVestimentas = listOf(
    "Uniforme Tecsup",
    "Hoodie Tech",
    "Overol Minero",
    "Armadura Gamer",
    "Bata Laboratorio",
    "Casaca Cuero",
    "Traje Ejecutivo",
    "Deportivo Pro",
    "Poncho Andino",
    "Traje Espacial"
)

val stickmanCabezasAnimales = listOf(
    "Zorro 🦊",
    "Lobo 🐺",
    "Búho 🦉",
    "Puma 🐆",
    "Cóndor 🦅",
    "Gato 🐱",
    "Oso 🐻",
    "Dragón 🐲",
    "Conejo 🐰",
    "León 🦁"
)

@Composable
fun StickmanAvatar(
    colorIndex: Int,
    outfitIndex: Int,
    headIndex: Int,
    modifier: Modifier = Modifier.size(110.dp)
) {
    val cuerpoColor = stickmanColores[colorIndex.coerceIn(0, stickmanColores.lastIndex)].second
    val outfitColor = when (outfitIndex.coerceIn(0, 9)) {
        0 -> Color(0xFF01579B) // Uniforme Tecsup
        1 -> Color(0xFF37474F) // Hoodie Tech
        2 -> Color(0xFFE65100) // Overol Minero
        3 -> Color(0xFF4A148C) // Armadura Gamer
        4 -> Color(0xFFECEFF1) // Bata Laboratorio
        5 -> Color(0xFF3E2723) // Casaca Cuero
        6 -> Color(0xFF1A237E) // Traje Ejecutivo
        7 -> Color(0xFF00695C) // Deportivo Pro
        8 -> Color(0xFFB71C1C) // Poncho Andino
        else -> Color(0xFFCFD8DC) // Traje Espacial
    }
    val pantalonColor = when (outfitIndex.coerceIn(0, 9)) {
        0, 6 -> Color(0xFF263238)
        2 -> Color(0xFFBF360C)
        3 -> Color(0xFF311B92)
        4, 9 -> Color(0xFFB0BEC5)
        8 -> Color(0xFF4E342E)
        else -> cuerpoColor.copy(alpha = 0.85f)
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        val headRadius = w * 0.16f
        val headCenter = Offset(cx, h * 0.24f)
        val neckY = headCenter.y + headRadius
        val hipY = h * 0.64f
        val footY = h * 0.92f

        // Brazos stickman
        drawLine(cuerpoColor, Offset(cx - w * 0.12f, neckY + h * 0.06f), Offset(cx - w * 0.28f, hipY - h * 0.04f), strokeWidth = 6f, cap = StrokeCap.Round)
        drawLine(cuerpoColor, Offset(cx + w * 0.12f, neckY + h * 0.06f), Offset(cx + w * 0.28f, hipY - h * 0.04f), strokeWidth = 6f, cap = StrokeCap.Round)

        // Piernas stickman + vestimenta de piernas
        drawLine(pantalonColor, Offset(cx - w * 0.07f, hipY), Offset(cx - w * 0.16f, footY), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(pantalonColor, Offset(cx + w * 0.07f, hipY), Offset(cx + w * 0.16f, footY), strokeWidth = 10f, cap = StrokeCap.Round)

        // Torso stickman + vestimenta unisex
        drawRoundRect(
            color = outfitColor,
            topLeft = Offset(cx - w * 0.14f, neckY + 2f),
            size = Size(w * 0.28f, hipY - neckY),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = cuerpoColor,
            topLeft = Offset(cx - w * 0.14f, neckY + 2f),
            size = Size(w * 0.28f, hipY - neckY),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 3f)
        )

        // Detalle de vestimenta (cinturón o franja)
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(cx - w * 0.13f, (neckY + hipY) * 0.52f),
            end = Offset(cx + w * 0.13f, (neckY + hipY) * 0.52f),
            strokeWidth = 4f
        )

        // Cabeza de animal (10 estilos)
        val animal = headIndex.coerceIn(0, 9)
        when (animal) {
            0, 1, 3, 5 -> {
                // Orejas puntiagudas (Zorro, Lobo, Puma, Gato)
                val earLeft = Path().apply {
                    moveTo(headCenter.x - headRadius * 0.85f, headCenter.y - headRadius * 0.3f)
                    lineTo(headCenter.x - headRadius * 0.55f, headCenter.y - headRadius * 1.45f)
                    lineTo(headCenter.x - headRadius * 0.1f, headCenter.y - headRadius * 0.75f)
                    close()
                }
                val earRight = Path().apply {
                    moveTo(headCenter.x + headRadius * 0.85f, headCenter.y - headRadius * 0.3f)
                    lineTo(headCenter.x + headRadius * 0.55f, headCenter.y - headRadius * 1.45f)
                    lineTo(headCenter.x + headRadius * 0.1f, headCenter.y - headRadius * 0.75f)
                    close()
                }
                drawPath(earLeft, cuerpoColor)
                drawPath(earRight, cuerpoColor)
            }
            6, 9 -> {
                // Orejas redondas / melena (Oso, León)
                if (animal == 9) {
                    drawCircle(Color(0xFFFF8F00), radius = headRadius * 1.32f, center = headCenter)
                }
                drawCircle(cuerpoColor, radius = headRadius * 0.42f, center = Offset(headCenter.x - headRadius * 0.75f, headCenter.y - headRadius * 0.75f))
                drawCircle(cuerpoColor, radius = headRadius * 0.42f, center = Offset(headCenter.x + headRadius * 0.75f, headCenter.y - headRadius * 0.75f))
            }
            8 -> {
                // Orejas largas de Conejo
                drawRoundRect(cuerpoColor, Offset(headCenter.x - headRadius * 0.65f, headCenter.y - headRadius * 1.9f), Size(headRadius * 0.4f, headRadius * 1.2f), CornerRadius(10f, 10f))
                drawRoundRect(cuerpoColor, Offset(headCenter.x + headRadius * 0.25f, headCenter.y - headRadius * 1.9f), Size(headRadius * 0.4f, headRadius * 1.2f), CornerRadius(10f, 10f))
            }
            2, 4 -> {
                // Plumas / cresta de Búho o Cóndor
                drawCircle(Color(0xFFFFB300), radius = headRadius * 0.35f, center = Offset(headCenter.x, headCenter.y - headRadius * 1.05f))
            }
            7 -> {
                // Cuernos de Dragón
                drawLine(Color(0xFFD50000), Offset(headCenter.x - headRadius * 0.5f, headCenter.y - headRadius * 0.7f), Offset(headCenter.x - headRadius * 0.95f, headCenter.y - headRadius * 1.5f), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(Color(0xFFD50000), Offset(headCenter.x + headRadius * 0.5f, headCenter.y - headRadius * 0.7f), Offset(headCenter.x + headRadius * 0.95f, headCenter.y - headRadius * 1.5f), strokeWidth = 6f, cap = StrokeCap.Round)
            }
        }

        // Rostro principal
        drawCircle(color = cuerpoColor, radius = headRadius, center = headCenter)
        // Ojos blancos y pupilas
        drawCircle(Color.White, radius = headRadius * 0.22f, center = Offset(headCenter.x - headRadius * 0.36f, headCenter.y - headRadius * 0.12f))
        drawCircle(Color.White, radius = headRadius * 0.22f, center = Offset(headCenter.x + headRadius * 0.36f, headCenter.y - headRadius * 0.12f))
        drawCircle(Color.Black, radius = headRadius * 0.10f, center = Offset(headCenter.x - headRadius * 0.36f, headCenter.y - headRadius * 0.12f))
        drawCircle(Color.Black, radius = headRadius * 0.10f, center = Offset(headCenter.x + headRadius * 0.36f, headCenter.y - headRadius * 0.12f))
        // Hocico / pico
        drawCircle(Color(0xFFFFE082), radius = headRadius * 0.24f, center = Offset(headCenter.x, headCenter.y + headRadius * 0.28f))
    }
}