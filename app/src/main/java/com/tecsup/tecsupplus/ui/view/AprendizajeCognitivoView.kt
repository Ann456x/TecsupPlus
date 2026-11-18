package com.tecsup.tecsupplus.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.data.local.NearpodQuizEntity
import com.tecsup.tecsupplus.data.local.NearpodResultadoEntity
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import com.tecsup.tecsupplus.data.repository.PreguntaNearpod
import com.tecsup.tecsupplus.data.repository.TecsupRepository
import com.tecsup.tecsupplus.ui.components.StickmanAvatar
import com.tecsup.tecsupplus.ui.components.stickmanCabezasAnimales
import com.tecsup.tecsupplus.ui.components.stickmanColores
import com.tecsup.tecsupplus.ui.components.stickmanVestimentas
import com.tecsup.tecsupplus.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun preguntasDemoPorDefecto(): List<PreguntaNearpod> = listOf(
    PreguntaNearpod("1. ¿Cuál es la arquitectura recomendada en Android moderno con Jetpack Compose?", listOf("MVC clásico", "MVVM con patrón Repository", "Solo Activities sin estado", "JSP Servlets"), 1),
    PreguntaNearpod("2. ¿Qué librería de Android permite persistencia local SQLite con anotaciones?", listOf("Retrofit", "Coil", "Room Database", "Glide"), 2),
    PreguntaNearpod("3. ¿Qué componente permite ejecutar tareas programadas en segundo plano?", listOf("WorkManager", "AlertDialog", "LazyRow", "Divider"), 0),
    PreguntaNearpod("4. En programación orientada a objetos, ¿qué principio oculta los detalles internos?", listOf("Herencia múltiple", "Encapsulamiento", "Sobrecarga", "Recursividad"), 1),
    PreguntaNearpod("5. ¿Qué estructura de datos sigue el principio LIFO (último en entrar, primero en salir)?", listOf("Cola (Queue)", "Pila (Stack)", "Árbol Binario", "Grafo"), 1),
    PreguntaNearpod("6. ¿Cuál es la nota mínima aprobatoria estándar en Tecsup?", listOf("10.5", "11.0", "13.0", "14.0"), 2),
    PreguntaNearpod("7. ¿Qué protocolo se utiliza para consumir APIs REST de forma segura?", listOf("FTP", "HTTPS", "SMTP", "TELNET"), 1),
    PreguntaNearpod("8. ¿Qué sensor detecta cuando colocas el teléfono boca abajo sobre la carpeta?", listOf("Barómetro", "Acelerómetro / Gravedad", "Higrómetro", "Sensor de huella"), 1),
    PreguntaNearpod("9. ¿Qué función de Kotlin permite ejecutar operaciones asíncronas sin bloquear la UI?", listOf("Coroutines (suspend)", "Thread.sleep", "System.exit", "println"), 0),
    PreguntaNearpod("10. ¿Qué ventaja ofrece el patrón Repository en un proyecto móvil?", listOf("Mezcla SQL con botones", "Separa la lógica de datos de la interfaz", "Elimina la base de datos", "Solo sirve para juegos 3D"), 1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprendizajeCognitivoView(user: UsuarioEntity) {
    val context = LocalContext.current
    val repo = remember { TecsupRepository(context) }
    val scope = rememberCoroutineScope()
    val esDocenteOAdmin = esProfesorAdmin(user) || esProfesorNormal(user)

    var subTab by remember { mutableStateOf(0) } // 0: Jugar / Lobby, 1: Crear Quiz (Docente/IA), 2: Historial Semanal
    var quizzesDb by remember { mutableStateOf<List<NearpodQuizEntity>>(emptyList()) }
    var quizSeleccionado by remember { mutableStateOf<NearpodQuizEntity?>(null) }

    // Personalización Stickman del alumno
    var colorIdx by remember { mutableStateOf(0) }
    var ropaIdx by remember { mutableStateOf(0) }
    var cabezaIdx by remember { mutableStateOf(0) }

    // Estado del juego en vivo
    var faseJuego by remember { mutableStateOf("LOBBY") } // LOBBY, JUGANDO, PODIO, TABLA_COMPLETA
    var preguntasActivas by remember { mutableStateOf<List<PreguntaNearpod>>(emptyList()) }
    var indicePregunta by remember { mutableStateOf(0) }
    var tiempoRestante by remember { mutableStateOf(15) }
    var puntajeAcumulado by remember { mutableStateOf(0) }
    var aciertosTotales by remember { mutableStateOf(0) }
    var opcionElegidaIdx by remember { mutableStateOf<Int?>(null) }
    var rankingFinal by remember { mutableStateOf<List<NearpodResultadoEntity>>(emptyList()) }

    // Estado creador de Quiz (Manual o con IA Groq)
    var nuevoTitulo by remember { mutableStateOf("Reto Cognitivo - Teoría Semana 3") }
    var nuevaSemana by remember { mutableStateOf("Semana 3") }
    var nuevoTiempoSeg by remember { mutableStateOf("15") }
    var textoApuntesIA by remember { mutableStateOf("") }
    var generandoConIA by remember { mutableStateOf(false) }
    var mensajeCreador by remember { mutableStateOf<String?>(null) }
    var preguntasBorrador by remember {
        mutableStateOf(
            List(10) { i ->
                PreguntaNearpod(
                    enunciado = "Pregunta ${i + 1}: Escribe aquí el enunciado...",
                    opciones = listOf("Alternativa A", "Alternativa B", "Alternativa C", "Alternativa D"),
                    indiceCorrecto = 0
                )
            }
        )
    }

    // Historial completo para docente
    var todosResultados by remember { mutableStateOf<List<NearpodResultadoEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val existentes = repo.obtenerQuizzesDisponibles()
        if (existentes.isEmpty()) {
            repo.publicarQuiz(
                titulo = "Reto Cognitivo: Fundamentos Tecsup",
                cursoCodigo = "GENERAL",
                semana = "Semana 3",
                creadorUsername = "docente_demo",
                tiempoSeg = 15,
                preguntas = preguntasDemoPorDefecto()
            )
        }
        quizzesDb = repo.obtenerQuizzesDisponibles()
        quizSeleccionado = quizzesDb.firstOrNull()
        todosResultados = repo.obtenerTodosLosResultados()
    }

    // Temporizador por pregunta
    LaunchedEffect(faseJuego, indicePregunta, opcionElegidaIdx) {
        if (faseJuego == "JUGANDO" && opcionElegidaIdx == null) {
            while (tiempoRestante > 0) {
                delay(1000L)
                tiempoRestante -= 1
            }
            if (tiempoRestante == 0 && opcionElegidaIdx == null) {
                opcionElegidaIdx = -1 // Tiempo agotado
                delay(900L)
                if (indicePregunta + 1 < preguntasActivas.size) {
                    indicePregunta += 1
                    tiempoRestante = quizSeleccionado?.tiempoPorPreguntaSeg ?: 15
                    opcionElegidaIdx = null
                } else {
                    // Finalizar juego y calcular podio
                    val quizActual = quizSeleccionado!!
                    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    // Sembrar competidores base si es la primera vez para ver el podio completo
                    val previos = repo.obtenerRankingPorQuiz(quizActual.id)
                    if (previos.isEmpty()) {
                        val bots = listOf(
                            Triple("Ariana Nicole Sotomayor", 820, "2|4|2"),
                            Triple("Matías Alejandro Benavides", 760, "0|1|0"),
                            Triple("Jhosep Sebastian Mamani", 690, "4|3|7"),
                            Triple("Luciana Belén Valdivia", 610, "7|8|5")
                        )
                        bots.forEach { (nom, pts, cfg) ->
                            repo.guardarResultadoYCalcularPodio(
                                NearpodResultadoEntity(
                                    quizId = quizActual.id,
                                    quizTitulo = quizActual.titulo,
                                    semana = quizActual.semana,
                                    estudianteUsername = nom.lowercase().replace(" ", "_"),
                                    estudianteNombre = nom,
                                    puntaje = pts,
                                    aciertos = (pts / 100).coerceIn(5, 9),
                                    totalPreguntas = preguntasActivas.size,
                                    puntosExtra = 0.0,
                                    stickmanConfig = cfg,
                                    fechaHora = fecha
                                )
                            )
                        }
                    }
                    rankingFinal = repo.guardarResultadoYCalcularPodio(
                        NearpodResultadoEntity(
                            quizId = quizActual.id,
                            quizTitulo = quizActual.titulo,
                            semana = quizActual.semana,
                            estudianteUsername = user.username,
                            estudianteNombre = user.nombreCompleto,
                            puntaje = puntajeAcumulado,
                            aciertos = aciertosTotales,
                            totalPreguntas = preguntasActivas.size,
                            puntosExtra = 0.0,
                            stickmanConfig = "$colorIdx|$ropaIdx|$cabezaIdx",
                            fechaHora = fecha
                        )
                    )
                    todosResultados = repo.obtenerTodosLosResultados()
                    faseJuego = "PODIO"
                }
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Aprendizaje Cognitivo Interactivo", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0277BD))
            Text("Evaluación gamificada en tiempo real con Stickman personalizable, IA y puntos extra.", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val tabs = if (esDocenteOAdmin) listOf("Sala en Vivo", "Crear / IA", "Resultados Semanales") else listOf("Sala en Vivo", "Historial")
                tabs.forEachIndexed { idx, tituloTab ->
                    val realIdx = if (!esDocenteOAdmin && idx == 1) 2 else idx
                    Button(
                        onClick = {
                            subTab = realIdx
                            faseJuego = "LOBBY"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == realIdx) Color(0xFF0277BD) else Color(0xFFE1F5FE),
                            contentColor = if (subTab == realIdx) Color.White else Color(0xFF0277BD)
                        ),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(tituloTab, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==================== SUBTAB 0: SALA EN VIVO + STICKMAN + PODIO + RANKING ====================
        if (subTab == 0) {
            when (faseJuego) {
                "LOBBY" -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Personaliza tu Avatar Stickman", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF002B49))
                                Text("Elige entre 10 colores, 10 vestimentas unisex y 10 cabezas de animales", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))

                                StickmanAvatar(colorIndex = colorIdx, outfitIndex = ropaIdx, headIndex = cabezaIdx)
                                Text(
                                    "${stickmanColores[colorIdx].first} • ${stickmanVestimentas[ropaIdx]} • ${stickmanCabezasAnimales[cabezaIdx]}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0277BD)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // 10 Colores
                                Text("1. Color del Stickman (10 opciones):", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    stickmanColores.forEachIndexed { idx, (nombreCol, col) ->
                                        FilterChip(
                                            selected = colorIdx == idx,
                                            onClick = { colorIdx = idx },
                                            label = { Text(nombreCol, fontSize = 10.sp) },
                                            leadingIcon = { Box(modifier = Modifier.size(10.dp).background(col, RoundedCornerShape(50))) }
                                        )
                                    }
                                }

                                // 10 Vestimentas Unisex Torso y Piernas
                                Text("2. Vestimenta Unisex Torso y Piernas (10 opciones):", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    stickmanVestimentas.forEachIndexed { idx, nombreRopa ->
                                        FilterChip(
                                            selected = ropaIdx == idx,
                                            onClick = { ropaIdx = idx },
                                            label = { Text(nombreRopa, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                // 10 Cabezas de Animales
                                Text("3. Cabeza de Animal (10 opciones):", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    stickmanCabezasAnimales.forEachIndexed { idx, nombreCab ->
                                        FilterChip(
                                            selected = cabezaIdx == idx,
                                            onClick = { cabezaIdx = idx },
                                            label = { Text(nombreCab, fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Selecciona Evaluación Activa:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF002B49))
                                Spacer(modifier = Modifier.height(6.dp))
                                quizzesDb.forEach { q ->
                                    val sel = quizSeleccionado?.id == q.id
                                    Surface(
                                        color = if (sel) Color(0xFFE1F5FE) else Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { quizSeleccionado = q }
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(q.titulo, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0277BD))
                                                Text("${q.semana} • Tiempo: ${q.tiempoPorPreguntaSeg}s por pregunta", fontSize = 10.sp, color = Color.DarkGray)
                                            }
                                            if (sel) Text("Seleccionado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val q = quizSeleccionado ?: return@Button
                                        val lista = repo.deserializarPreguntas(q.preguntasJson)
                                        if (lista.isNotEmpty()) {
                                            preguntasActivas = lista
                                            indicePregunta = 0
                                            tiempoRestante = q.tiempoPorPreguntaSeg
                                            puntajeAcumulado = 0
                                            aciertosTotales = 0
                                            opcionElegidaIdx = null
                                            faseJuego = "JUGANDO"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text("Iniciar Reto Cognitivo (10 Preguntas)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                "JUGANDO" -> {
                    item {
                        val preg = preguntasActivas[indicePregunta]
                        val tiempoMax = (quizSeleccionado?.tiempoPorPreguntaSeg ?: 15).coerceAtLeast(1)
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Pregunta ${indicePregunta + 1} de ${preguntasActivas.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0277BD))
                                    Surface(color = if (tiempoRestante <= 5) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "Tiempo: ${tiempoRestante}s | Pts: $puntajeAcumulado",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (tiempoRestante <= 5) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = tiempoRestante.toFloat() / tiempoMax.toFloat(),
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (tiempoRestante <= 5) Color(0xFFD32F2F) else Color(0xFF0277BD)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(preg.enunciado, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF002B49))
                                Spacer(modifier = Modifier.height(14.dp))

                                val coloresOpciones = listOf(Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFEF6C00), Color(0xFF6A1B9A))
                                preg.opciones.forEachIndexed { idx, textoOpcion ->
                                    val bgColor = when {
                                        opcionElegidaIdx == null -> coloresOpciones[idx % 4]
                                        idx == preg.indiceCorrecto -> Color(0xFF2E7D32)
                                        idx == opcionElegidaIdx -> Color(0xFFC62828)
                                        else -> Color(0xFFB0BEC5)
                                    }
                                    Button(
                                        onClick = {
                                            if (opcionElegidaIdx == null) {
                                                opcionElegidaIdx = idx
                                                if (idx == preg.indiceCorrecto) {
                                                    aciertosTotales += 1
                                                    puntajeAcumulado += (100 + tiempoRestante * 10)
                                                }
                                                scope.launch {
                                                    delay(750L)
                                                    if (indicePregunta + 1 < preguntasActivas.size) {
                                                        indicePregunta += 1
                                                        tiempoRestante = tiempoMax
                                                        opcionElegidaIdx = null
                                                    } else {
                                                        val quizActual = quizSeleccionado!!
                                                        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                                        val previos = repo.obtenerRankingPorQuiz(quizActual.id)
                                                        if (previos.isEmpty()) {
                                                            listOf(
                                                                Triple("Ariana Nicole Sotomayor", 820, "2|4|2"),
                                                                Triple("Matías Alejandro Benavides", 760, "0|1|0"),
                                                                Triple("Jhosep Sebastian Mamani", 690, "4|3|7"),
                                                                Triple("Luciana Belén Valdivia", 610, "7|8|5")
                                                            ).forEach { (nom, pts, cfg) ->
                                                                repo.guardarResultadoYCalcularPodio(
                                                                    NearpodResultadoEntity(
                                                                        quizId = quizActual.id,
                                                                        quizTitulo = quizActual.titulo,
                                                                        semana = quizActual.semana,
                                                                        estudianteUsername = nom.lowercase().replace(" ", "_"),
                                                                        estudianteNombre = nom,
                                                                        puntaje = pts,
                                                                        aciertos = (pts / 100).coerceIn(5, 9),
                                                                        totalPreguntas = preguntasActivas.size,
                                                                        puntosExtra = 0.0,
                                                                        stickmanConfig = cfg,
                                                                        fechaHora = fecha
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        rankingFinal = repo.guardarResultadoYCalcularPodio(
                                                            NearpodResultadoEntity(
                                                                quizId = quizActual.id,
                                                                quizTitulo = quizActual.titulo,
                                                                semana = quizActual.semana,
                                                                estudianteUsername = user.username,
                                                                estudianteNombre = user.nombreCompleto,
                                                                puntaje = puntajeAcumulado,
                                                                aciertos = aciertosTotales,
                                                                totalPreguntas = preguntasActivas.size,
                                                                puntosExtra = 0.0,
                                                                stickmanConfig = "$colorIdx|$ropaIdx|$cabezaIdx",
                                                                fechaHora = fecha
                                                            )
                                                        )
                                                        todosResultados = repo.obtenerTodosLosResultados()
                                                        faseJuego = "PODIO"
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(46.dp)
                                    ) {
                                        Text(textoOpcion, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }

                "PODIO" -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF002B49))) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PODIO DE GANADORES - TOP 3", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFFD54F))
                                Text("Puntos adicionales otorgados automáticamente a los primeros puestos", fontSize = 10.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(14.dp))

                                val top3 = rankingFinal.take(3)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                    top3.forEachIndexed { idx, ganador ->
                                        val partes = ganador.stickmanConfig.split("|").mapNotNull { it.toIntOrNull() }
                                        val c = partes.getOrElse(0) { 0 }
                                        val r = partes.getOrElse(1) { 0 }
                                        val h = partes.getOrElse(2) { 0 }
                                        val medalla = when (idx) { 0 -> "1° ORO"; 1 -> "2° PLATA"; else -> "3° BRONCE" }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(medalla, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFD54F))
                                            StickmanAvatar(colorIndex = c, outfitIndex = r, headIndex = h, modifier = Modifier.size(if (idx == 0) 90.dp else 72.dp))
                                            Text(ganador.estudianteNombre.split(" ").take(2).joinToString(" "), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                            Text("${ganador.puntaje} pts", fontSize = 10.sp, color = Color(0xFF81C784))
                                            Text("+${ganador.puntosExtra} pts extra", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { faseJuego = "TABLA_COMPLETA" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver Tabla Completa (Mayor a Menor Nota)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                "TABLA_COMPLETA" -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Clasificación General (De Mayor a Menor Puntaje)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                                Text("Todos los participantes quedan registrados para revisión del docente.", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    itemsIndexed(rankingFinal) { idx, fila ->
                        val notaVigesimal = (fila.aciertos.toDouble() / fila.totalPreguntas.coerceAtLeast(1)) * 20.0
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD), modifier = Modifier.width(32.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fila.estudianteNombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF002B49))
                                    Text("Aciertos: ${fila.aciertos}/${fila.totalPreguntas} • Nota: ${String.format("%.1f", notaVigesimal)}/20", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${fila.puntaje} pts", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    if (fila.puntosExtra > 0) {
                                        Text("+${fila.puntosExtra} extra", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Button(onClick = { faseJuego = "LOBBY" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Volver a la Sala")
                        }
                    }
                }
            }
        }

        // ==================== SUBTAB 1: CREADOR DOCENTE (MANUAL O CON IA GROQ) ====================
        if (subTab == 1 && esDocenteOAdmin) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Crear Evaluación Cognitiva (10 Preguntas)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                        OutlinedTextField(value = nuevoTitulo, onValueChange = { nuevoTitulo = it }, label = { Text("Título de la sesión", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = nuevaSemana, onValueChange = { nuevaSemana = it }, label = { Text("Semana (ej. Semana 3)", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = nuevoTiempoSeg, onValueChange = { nuevoTiempoSeg = it }, label = { Text("Segundos/preg", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                        }

                        Divider()
                        Text("Asistente IA Groq: Genera las 10 preguntas desde tus apuntes o documento", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF6A1B9A))
                        OutlinedTextField(
                            value = textoApuntesIA,
                            onValueChange = { textoApuntesIA = it },
                            placeholder = { Text("Pega aquí el texto de tu clase, guía o resumen...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )
                        Button(
                            onClick = {
                                if (textoApuntesIA.isNotBlank()) {
                                    generandoConIA = true
                                    mensajeCreador = null
                                    scope.launch {
                                        val res = repo.generarPreguntasConIA(textoApuntesIA)
                                        generandoConIA = false
                                        res.onSuccess { generadas ->
                                            preguntasBorrador = generadas
                                            mensajeCreador = "IA Groq generó ${generadas.size} preguntas listas para publicar."
                                        }.onFailure { err ->
                                            mensajeCreador = "Error IA: ${err.message}"
                                        }
                                    }
                                }
                            },
                            enabled = !generandoConIA && textoApuntesIA.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (generandoConIA) "Generando 10 preguntas con Groq IA..." else "Generar 10 Preguntas Automáticas con IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        mensajeCreador?.let {
                            Text(it, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            itemsIndexed(preguntasBorrador) { idx, preg ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Pregunta ${idx + 1} (Correcta: Opción ${preg.indiceCorrecto + 1})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0277BD))
                        OutlinedTextField(
                            value = preg.enunciado,
                            onValueChange = { nuevoEnunc ->
                                val copia = preguntasBorrador.toMutableList()
                                copia[idx] = preg.copy(enunciado = nuevoEnunc)
                                preguntasBorrador = copia
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        preg.opciones.forEachIndexed { opIdx, opTxt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = preg.indiceCorrecto == opIdx,
                                    onClick = {
                                        val copia = preguntasBorrador.toMutableList()
                                        copia[idx] = preg.copy(indiceCorrecto = opIdx)
                                        preguntasBorrador = copia
                                    }
                                )
                                OutlinedTextField(
                                    value = opTxt,
                                    onValueChange = { nuevoOp ->
                                        val opsMutable = preg.opciones.toMutableList()
                                        opsMutable[opIdx] = nuevoOp
                                        val copia = preguntasBorrador.toMutableList()
                                        copia[idx] = preg.copy(opciones = opsMutable)
                                        preguntasBorrador = copia
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            val tiempo = nuevoTiempoSeg.toIntOrNull()?.coerceIn(5, 120) ?: 15
                            repo.publicarQuiz(
                                titulo = nuevoTitulo.ifBlank { "Evaluación Cognitiva" },
                                cursoCodigo = user.carrera ?: "GENERAL",
                                semana = nuevaSemana.ifBlank { "Semana 3" },
                                creadorUsername = user.username,
                                tiempoSeg = tiempo,
                                preguntas = preguntasBorrador
                            )
                            quizzesDb = repo.obtenerQuizzesDisponibles()
                            quizSeleccionado = quizzesDb.firstOrNull()
                            NotificationHelper.enviarNotificacionPush(
                                context,
                                "Nueva Sala Cognitiva: $nuevoTitulo",
                                "Disponible para $nuevaSemana ($tiempo seg/pregunta). ¡Ingresa y personaliza tu Stickman!"
                            )
                            subTab = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Publicar Evaluación y Enviar Notificación Push", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // ==================== SUBTAB 2: HISTORIAL DE RESULTADOS POR SEMANA ====================
        if (subTab == 2) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Historial de Evaluaciones Cognitivas por Semana", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                        Text("Registro guardado en base de datos local con puntos adicionales asignados.", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            if (todosResultados.isEmpty()) {
                item {
                    Text("Aún no hay partidas registradas. ¡Juega una sesión en 'Sala en Vivo'!", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                }
            } else {
                itemsIndexed(todosResultados.sortedByDescending { it.puntaje }) { idx, res ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("#${idx + 1} • ${res.estudianteNombre}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF002B49))
                                Text("${res.quizTitulo} (${res.semana}) • ${res.fechaHora}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${res.puntaje} pts (${res.aciertos}/${res.totalPreguntas})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                                if (res.puntosExtra > 0) {
                                    Text("+${res.puntosExtra} pts extra", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}