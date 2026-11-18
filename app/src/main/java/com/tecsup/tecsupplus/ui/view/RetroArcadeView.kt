package com.tecsup.tecsupplus.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.PerfilUsuarioExtraEntity
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun RetroArcadeView(user: UsuarioEntity, onCerrar: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var naveX by remember { mutableStateOf(0.5f) }
    var puntaje by remember { mutableStateOf(0) }
    var recordPersonal by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val enemigos = remember { mutableStateListOf<Offset>() }
    val balas = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(user.username) {
        recordPersonal = db.extraDao().obtenerPerfilExtra(user.username)?.arcadeHighScore ?: 0
    }

    LaunchedEffect(gameOver) {
        var tick = 0
        while (!gameOver) {
            delay(45L)
            tick += 1
            if (tick % 18 == 0) {
                enemigos.add(Offset(Random.nextFloat().coerceIn(0.1f, 0.9f), 0.05f))
            }
            if (tick % 8 == 0) {
                balas.add(Offset(naveX, 0.84f))
            }

            for (i in balas.indices.reversed()) {
                val b = balas[i]
                if (b.y - 0.04f < 0f) balas.removeAt(i)
                else balas[i] = Offset(b.x, b.y - 0.04f)
            }

            for (i in enemigos.indices.reversed()) {
                val e = enemigos[i]
                val nuevoY = e.y + 0.014f
                val golpeado = balas.indexOfFirst { b -> abs(b.x - e.x) < 0.07f && abs(b.y - nuevoY) < 0.06f }
                if (golpeado >= 0) {
                    balas.removeAt(golpeado)
                    enemigos.removeAt(i)
                    puntaje += 50
                    if (puntaje > recordPersonal) recordPersonal = puntaje
                } else if (nuevoY > 0.88f) {
                    gameOver = true
                    scope.launch {
                        val actual = db.extraDao().obtenerPerfilExtra(user.username) ?: PerfilUsuarioExtraEntity(username = user.username)
                        if (recordPersonal > actual.arcadeHighScore) {
                            db.extraDao().guardarPerfilExtra(actual.copy(arcadeHighScore = recordPersonal))
                        }
                    }
                    break
                } else {
                    enemigos[i] = Offset(e.x, nuevoY)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("EASTER EGG: TECSUP RETRO-ARCADE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6A1B9A))
                Text("Minijuego recreativo (no altera notas oficiales)", fontSize = 10.sp, color = Color.Gray)
            }
            OutlinedButton(onClick = onCerrar, modifier = Modifier.height(32.dp)) {
                Text("Salir", fontSize = 10.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Puntaje: $puntaje", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0277BD))
            Text("Récord ${user.nombreCompleto.split(" ").first()}: $recordPersonal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E17))) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E17), RoundedCornerShape(10.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            naveX = (change.position.x / size.width).coerceIn(0.08f, 0.92f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                balas.forEach { b ->
                    drawRect(Color(0xFF00E5FF), Offset(b.x * w - 4f, b.y * h - 14f), Size(8f, 22f))
                }

                enemigos.forEach { e ->
                    drawRect(Color(0xFFFF1744), Offset(e.x * w - 22f, e.y * h - 18f), Size(44f, 32f))
                    drawRect(Color(0xFFFFD54F), Offset(e.x * w - 12f, e.y * h - 8f), Size(8f, 8f))
                    drawRect(Color(0xFFFFD54F), Offset(e.x * w + 4f, e.y * h - 8f), Size(8f, 8f))
                }

                drawRect(Color(0xFF76FF03), Offset(naveX * w - 28f, h * 0.87f), Size(56f, 24f))
                drawRect(Color.White, Offset(naveX * w - 8f, h * 0.84f), Size(16f, 14f))
            }
        }

        if (gameOver) {
            Button(
                onClick = {
                    enemigos.clear()
                    balas.clear()
                    puntaje = 0
                    gameOver = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jugar de Nuevo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Desliza el dedo horizontalmente sobre la pantalla negra para mover tu nave.", fontSize = 10.sp, color = Color.Gray)
        }
    }
}