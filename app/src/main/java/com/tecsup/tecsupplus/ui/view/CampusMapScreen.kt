package com.tecsup.tecsupplus.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.tecsup.tecsupplus.BuildConfig
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

data class SectorCampusInfo(
    val numero: String,
    val titulo: String,
    val ambientes: String,
    val rx: Float,
    val ry: Float
)

val sectoresOficialesTecsup = listOf(
    SectorCampusInfo("1", "Edificio Administrativo", "Promoción y Desarrollo Empresarial, Atención al cliente, Oficina Crédito y Cobranza, Tesorería", 0.77f, 0.87f),
    SectorCampusInfo("2", "Dirección General", "Dirección General, Oficinas Administrativas, Contabilidad", 0.67f, 0.88f),
    SectorCampusInfo("3", "Oficina de Consultoría", "Oficina de Consultoría Empresarial y Proyectos", 0.59f, 0.90f),
    SectorCampusInfo("4", "Aulas D / Tópico", "Aulas D1 Conversa y Psicología, CDP, D2 Aula Lototo, D4 Tópico y Lactario", 0.37f, 0.71f),
    SectorCampusInfo("5", "Logística / Sub Estación", "Oficina de Logística, Sub Estación Eléctrica, Depósito General", 0.16f, 0.56f),
    SectorCampusInfo("6", "Pabellón R", "Aulas R1 - R6 (Dpto. Estudios Generales)", 0.14f, 0.34f),
    SectorCampusInfo("7", "Auditorio Principal", "Auditorio Principal Tecsup Arequipa", 0.13f, 0.24f),
    SectorCampusInfo("8.1", "Comedor Institucional", "Comedor Central (Dpto. Estudios Generales, Minería y Procesos Químico-Metalúrgicos)", 0.44f, 0.76f),
    SectorCampusInfo("8.2", "Cafetería Campus", "Cafetería (Tecnología Digital y Gestión)", 0.32f, 0.25f),
    SectorCampusInfo("9", "Pabellón A", "Biblioteca / A106-A109, Aulas A101-A114, Aulas A202-A214, Aulas A301-A314, Centro de Seguridad 201", 0.70f, 0.79f),
    SectorCampusInfo("10", "Pabellón B", "Aulas B101-B104, Aulas B201-B204, Aulas B301-B304, Auditorio 2 Primer piso, Calidad e Innovación Educativa TI 3er piso", 0.43f, 0.91f),
    SectorCampusInfo("11", "Pabellón C", "Aulas C2-C6, Lab C1 Hidrometalurgia, C3 Geomecánica, OEP Simuladores C4-C5, Prácticas y Pasantías", 0.48f, 0.53f),
    SectorCampusInfo("12", "Pabellón G (Dirección)", "Dirección Académica", 0.60f, 0.50f),
    SectorCampusInfo("13", "Pabellón G (Aulas)", "Aulas G1 - G11", 0.58f, 0.41f),
    SectorCampusInfo("14", "Pabellón E (Talleres)", "Talleres E1 - E8", 0.35f, 0.46f),
    SectorCampusInfo("15", "Pabellón E (Electrónica)", "Electricidad y Electrónica, Sala de Profesores", 0.58f, 0.38f),
    SectorCampusInfo("16", "Pabellón M (Mecánica)", "Departamento de Mecánica, Sala de Profesores, Miller, Laboratorio de Procesos de Minerales", 0.75f, 0.57f),
    SectorCampusInfo("17", "Talleres de Mecánica", "Talleres de Mecánica M1-M6, M12-M13", 0.75f, 0.46f),
    SectorCampusInfo("18", "Fab Lab / Talleres M9-M11", "Talleres de Mecánica Fab Lab M9-M11, E9-E10", 0.62f, 0.24f),
    SectorCampusInfo("19", "Talleres THINK BIG", "Talleres THINK BIG (M7 - M8)", 0.79f, 0.33f),
    SectorCampusInfo("20", "Pabellón F", "Espacios colaborativos, Aulas 101-106, Aulas 201-206", 0.39f, 0.92f)
)

data class ClaseProximaHorario(
    val curso: String,
    val aula: String,
    val sectorNumero: String,
    val horaTexto: String,
    val minutosRestantes: Int
)

fun obtenerProximaClaseDesdeHorario(user: UsuarioEntity): ClaseProximaHorario {
    val cal = Calendar.getInstance()
    val minActual = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val bloques = listOf(
        8 * 60 to "08:00 am",
        10 * 60 + 30 to "10:30 am",
        14 * 60 to "02:00 pm",
        16 * 60 + 30 to "04:30 pm",
        18 * 60 + 50 to "06:50 pm",
        20 * 60 + 30 to "08:30 pm"
    )
    val siguienteBloque = bloques.firstOrNull { it.first > minActual } ?: (8 * 60 + 24 * 60 to "08:00 am (Mañana)")
    val minsFaltan = (siguienteBloque.first - minActual).coerceAtLeast(1)

    val u = user.username.lowercase()
    return when {
        u == "prof_videojuegos_8" -> ClaseProximaHorario("Interfaz de Usuario (UI/UX) en Videojuegos", "Lab. A304 (Pab. A)", "9", siguienteBloque.second, minsFaltan)
        u == "prof_videojuegos_2" -> ClaseProximaHorario("Programación de Motores (Unity/Unreal)", "Lab. A302 (Pab. A)", "9", siguienteBloque.second, minsFaltan)
        u == "prof_minas_4" -> ClaseProximaHorario("Mecánica de Rocas", "Lab. C3 (Pab. C)", "11", siguienteBloque.second, minsFaltan)
        u == "prof_software_2" -> ClaseProximaHorario("Desarrollo de Aplicaciones Móviles", "Lab. A205 (Pab. A)", "9", siguienteBloque.second, minsFaltan)
        user.carrera.equals("MINAS", true) -> ClaseProximaHorario("Geomecánica y Procesos Mineros", "Lab. C3 (Pab. C)", "11", siguienteBloque.second, minsFaltan)
        user.carrera.equals("VIDEOJUEGOS", true) -> ClaseProximaHorario("Interfaz de Usuario (UI/UX) en Videojuegos", "Lab. A304 (Pab. A)", "9", siguienteBloque.second, minsFaltan)
        else -> ClaseProximaHorario("Desarrollo de Aplicaciones Móviles", "Lab. A205 (Pab. A)", "9", siguienteBloque.second, minsFaltan)
    }
}

fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private object TileCacheArequipa {
    val cache = ConcurrentHashMap<String, Bitmap>()
}

private fun lonToTileX(lon: Double, zoom: Int): Double =
    (lon + 180.0) / 360.0 * (1 shl zoom)

private fun latToTileY(lat: Double, zoom: Int): Double {
    val rad = Math.toRadians(lat)
    return (1.0 - ln(tan(rad) + 1.0 / cos(rad)) / PI) / 2.0 * (1 shl zoom)
}

private suspend fun descargarTileBitmap(urlStr: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("User-Agent", "TecsupPlusArequipa/1.0 (Android)")
        }
        if (conn.responseCode == 200) {
            conn.inputStream.use { BitmapFactory.decodeStream(it) }
        } else null
    } catch (_: Exception) {
        null
    }
}

@Composable
fun MapaNativoArequipaCanvas(
    tecsupLatLng: LatLng,
    usuarioLatLng: LatLng,
    gpsActivo: Boolean,
    tipoMapa: MapType,
    aulaDestino: String,
    sectorDestino: String
) {
    var zoom by remember { mutableIntStateOf(16) }
    var centerLat by remember { mutableDoubleStateOf(tecsupLatLng.latitude) }
    var centerLon by remember { mutableDoubleStateOf(tecsupLatLng.longitude) }
    var tileVersion by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(gpsActivo, usuarioLatLng) {
        if (gpsActivo) {
            centerLat = (tecsupLatLng.latitude + usuarioLatLng.latitude) / 2.0
            centerLon = (tecsupLatLng.longitude + usuarioLatLng.longitude) / 2.0
            zoom = 15
        } else {
            centerLat = tecsupLatLng.latitude
            centerLon = tecsupLatLng.longitude
            zoom = 16
        }
    }

    val estiloClave = when (tipoMapa) {
        MapType.HYBRID -> "SAT"
        MapType.TERRAIN -> "REL"
        else -> "OSM"
    }

    val centerTileX = lonToTileX(centerLon, zoom)
    val centerTileY = latToTileY(centerLat, zoom)
    val baseTileX = floor(centerTileX).toInt()
    val baseTileY = floor(centerTileY).toInt()

    LaunchedEffect(baseTileX, baseTileY, zoom, estiloClave) {
        val maxTile = (1 shl zoom) - 1
        for (dx in -2..2) {
            for (dy in -2..2) {
                val tx = baseTileX + dx
                val ty = baseTileY + dy
                if (tx in 0..maxTile && ty in 0..maxTile) {
                    val key = "$estiloClave/$zoom/$tx/$ty"
                    if (!TileCacheArequipa.cache.containsKey(key)) {
                        val url = when (estiloClave) {
                            "SAT" -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$zoom/$ty/$tx"
                            "REL" -> "https://a.tile.opentopomap.org/$zoom/$tx/$ty.png"
                            else -> "https://tile.openstreetmap.org/$zoom/$tx/$ty.png"
                        }
                        scope.launch {
                            val bmp = descargarTileBitmap(url)
                            if (bmp != null) {
                                TileCacheArequipa.cache[key] = bmp
                                tileVersion++
                            }
                        }
                    }
                }
            }
        }
    }

    val paintLabel = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 26f
            isAntiAlias = true
            isFakeBoldText = true
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFDCE4EC))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zoom) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val tilePx = 380.0
                        val n = (1 shl zoom).toDouble()
                        centerLon -= (dragAmount.x / tilePx) * (360.0 / n)
                        val curY = latToTileY(centerLat, zoom) - (dragAmount.y / tilePx)
                        val latRad = atan(sinh(PI * (1.0 - 2.0 * curY / n)))
                        centerLat = Math.toDegrees(latRad).coerceIn(-85.0, 85.0)
                    }
                }
        ) {
            @Suppress("UNUSED_VARIABLE")
            val triggerRedraw = tileVersion
            val tilePx = 380f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxTile = (1 shl zoom) - 1

            for (dx in -2..2) {
                for (dy in -2..2) {
                    val tx = baseTileX + dx
                    val ty = baseTileY + dy
                    if (tx in 0..maxTile && ty in 0..maxTile) {
                        val key = "$estiloClave/$zoom/$tx/$ty"
                        val left = cx + ((tx - centerTileX) * tilePx).toFloat()
                        val top = cy + ((ty - centerTileY) * tilePx).toFloat()
                        val bmp = TileCacheArequipa.cache[key]
                        if (bmp != null) {
                            drawImage(
                                image = bmp.asImageBitmap(),
                                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                                dstSize = IntSize(ceil(tilePx).toInt(), ceil(tilePx).toInt())
                            )
                        } else {
                            drawRect(
                                color = Color(0xFFE3EAF0),
                                topLeft = Offset(left, top),
                                size = Size(tilePx, tilePx)
                            )
                            drawRect(
                                color = Color(0xFFCFD8DC),
                                topLeft = Offset(left, top),
                                size = Size(tilePx, tilePx),
                                style = Stroke(1f)
                            )
                        }
                    }
                }
            }

            fun toScreen(lat: Double, lon: Double): Offset {
                val sx = cx + ((lonToTileX(lon, zoom) - centerTileX) * tilePx).toFloat()
                val sy = cy + ((latToTileY(lat, zoom) - centerTileY) * tilePx).toFloat()
                return Offset(sx, sy)
            }

            // Perímetro Campus Tecsup Arequipa
            val p1 = toScreen(-16.4301, -71.5204)
            val p2 = toScreen(-16.4301, -71.5184)
            val p3 = toScreen(-16.4323, -71.5184)
            val p4 = toScreen(-16.4323, -71.5204)
            val polyPath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            drawPath(polyPath, Color(0x380277BD))
            drawPath(polyPath, Color(0xFF0277BD), style = Stroke(4f))

            val tecsupPt = toScreen(tecsupLatLng.latitude, tecsupLatLng.longitude)
            val userPt = toScreen(usuarioLatLng.latitude, usuarioLatLng.longitude)

            if (gpsActivo) {
                drawLine(
                    color = Color(0xFFD50000),
                    start = userPt,
                    end = tecsupPt,
                    strokeWidth = 7f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                )
                drawCircle(Color(0xFFD50000), radius = 15f, center = userPt)
                drawCircle(Color.White, radius = 6f, center = userPt)
                drawContext.canvas.nativeCanvas.drawText("Tu GPS", userPt.x, userPt.y - 22f, paintLabel)
            }

            // Marcador Campus Tecsup Arequipa
            drawCircle(Color(0xFF0277BD), radius = 18f, center = tecsupPt)
            drawCircle(Color.White, radius = 7f, center = tecsupPt)
            drawContext.canvas.nativeCanvas.drawText("Tecsup Arequipa ($aulaDestino • S.$sectorDestino)", tecsupPt.x, tecsupPt.y - 26f, paintLabel)
        }

        // Controles Zoom + / - y centrar en Tecsup
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    centerLat = tecsupLatLng.latitude
                    centerLon = tecsupLatLng.longitude
                },
                containerColor = Color.White,
                contentColor = Color(0xFF0277BD),
                shape = CircleShape
            ) {
                Text("◎", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            SmallFloatingActionButton(
                onClick = { if (zoom < 18) zoom++ },
                containerColor = Color.White,
                contentColor = Color(0xFF002B49),
                shape = CircleShape
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            SmallFloatingActionButton(
                onClick = { if (zoom > 13) zoom-- },
                containerColor = Color.White,
                contentColor = Color(0xFF002B49),
                shape = CircleShape
            ) {
                Text("−", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun CampusMapScreen(user: UsuarioEntity) {
    val context = LocalContext.current
    var vistaExterior by remember { mutableStateOf(false) }
    var gpsActivo by remember { mutableStateOf(false) }
    var ubicacionGps by remember { mutableStateOf<Location?>(null) }
    var tipoMapaGoogle by remember { mutableStateOf(MapType.NORMAL) }

    val proximaClase = remember(user) { obtenerProximaClaseDesdeHorario(user) }
    var sectorSeleccionado by remember {
        mutableStateOf(sectoresOficialesTecsup.first { it.numero == proximaClase.sectorNumero })
    }

    val permisoGpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        gpsActivo = concedido
    }

    DisposableEffect(gpsActivo) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { loc -> ubicacionGps = loc }

        if (gpsActivo) {
            val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (tienePermiso) {
                ubicacionGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                try {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500L, 2f, listener)
                    }
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500L, 2f, listener)
                    }
                } catch (_: Exception) { }
            }
        } else {
            ubicacionGps = null
        }

        onDispose {
            try { locationManager.removeUpdates(listener) } catch (_: Exception) { }
        }
    }

    val tecsupLat = -16.4312
    val tecsupLon = -71.5194
    val tecsupLatLng = LatLng(tecsupLat, tecsupLon)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Ubicación y Plano Oficial Tecsup Arequipa", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text("Navegación integrada con tu horario académico (${user.nombreCompleto})", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vistaExterior = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!vistaExterior) Color(0xFF0277BD) else Color(0xFFE1F5FE),
                        contentColor = if (!vistaExterior) Color.White else Color(0xFF0277BD)
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Plano Oficial (20 Sectores)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { vistaExterior = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vistaExterior) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                        contentColor = if (vistaExterior) Color.White else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Desde Exterior (Arequipa)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (gpsActivo) "Ubicación Actual (GPS Activo)" else "Ubicación GPS Desactivada (Ruta desde Entrada)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (gpsActivo) Color(0xFF2E7D32) else Color(0xFF002B49)
                        )
                        Text(
                            text = if (gpsActivo) {
                                val loc = ubicacionGps
                                if (loc != null) "GPS: ${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}"
                                else "Sincronizando satélite GPS..."
                            } else "Actívalo para ver tu ubicación real en el Plano Oficial y Desde Exterior.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = gpsActivo,
                        onCheckedChange = { activar ->
                            if (activar) {
                                val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (ok) gpsActivo = true
                                else permisoGpsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            } else {
                                gpsActivo = false
                            }
                        }
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF002B49))) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PRÓXIMA CLASE EN HORARIO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA))
                        Text(proximaClase.curso, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD54F))
                        Text("${proximaClase.aula} • Sector [${proximaClase.sectorNumero}] • ${proximaClase.horaTexto}", fontSize = 11.sp, color = Color.White)
                    }
                    Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "Faltan ${proximaClase.minutosRestantes} min",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (!vistaExterior) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (gpsActivo) "Ruta en vivo desde TU UBICACIÓN GPS hasta Sector [${sectorSeleccionado.numero}]"
                            else "Ruta desde ENTRADA (Ingreso 1) hasta Sector [${sectorSeleccionado.numero}] (${proximaClase.aula})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0277BD),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val colorPiso1 = Color(0xFF1565C0)
                        val colorPiso2 = Color(0xFF2E7D32)
                        val colorPiso3 = Color(0xFF455A64)
                        val colorPiso4 = Color(0xFFB0BEC5)
                        val colorVerdeCampus = Color(0xFF5FB47E)
                        val colorAsfalto = Color(0xFF2B3035)
                        val colorVereda = Color(0xFFD0D5DD)

                        val paintTexto = remember {
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = true
                            }
                        }
                        val paintOscuro = remember {
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#102027")
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = true
                            }
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .background(Color(0xFFE9EEF2), RoundedCornerShape(10.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        val rx = tapOffset.x / size.width
                                        val ry = tapOffset.y / size.height
                                        val masCercano = sectoresOficialesTecsup.minByOrNull {
                                            hypot((it.rx - rx).toDouble(), (it.ry - ry).toDouble())
                                        }
                                        if (masCercano != null && hypot((masCercano.rx - rx).toDouble(), (masCercano.ry - ry).toDouble()) < 0.11) {
                                            sectorSeleccionado = masCercano
                                        }
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height

                            val campusPath = Path().apply {
                                moveTo(w * 0.03f, h * 0.22f)
                                lineTo(w * 0.25f, h * 0.17f)
                                lineTo(w * 0.30f, h * 0.22f)
                                lineTo(w * 0.42f, h * 0.19f)
                                lineTo(w * 0.86f, h * 0.19f)
                                lineTo(w * 0.88f, h * 0.58f)
                                lineTo(w * 0.84f, h * 0.61f)
                                lineTo(w * 0.85f, h * 0.86f)
                                lineTo(w * 0.80f, h * 0.92f)
                                lineTo(w * 0.45f, h * 0.96f)
                                lineTo(w * 0.32f, h * 0.96f)
                                lineTo(w * 0.27f, h * 0.88f)
                                lineTo(w * 0.18f, h * 0.87f)
                                lineTo(w * 0.14f, h * 0.56f)
                                close()
                            }
                            drawPath(campusPath, colorVerdeCampus)
                            drawPath(campusPath, Color(0xFF2E4035), style = Stroke(width = 3f))

                            drawRect(colorAsfalto, Offset(w * 0.19f, h * 0.53f), Size(w * 0.13f, h * 0.34f))
                            drawRect(colorAsfalto, Offset(w * 0.33f, h * 0.29f), Size(w * 0.49f, h * 0.03f))
                            drawRect(colorAsfalto, Offset(w * 0.32f, h * 0.57f), Size(w * 0.50f, h * 0.03f))
                            drawRect(colorVereda, Offset(w * 0.33f, h * 0.60f), Size(w * 0.46f, h * 0.28f))

                            for (row in 0..5) {
                                val cy = h * (0.59f + row * 0.043f)
                                drawRoundRect(Color(0xFFFFD54F), Offset(w * 0.28f, cy), Size(w * 0.025f, h * 0.022f), CornerRadius(3f, 3f))
                            }
                            paintTexto.textSize = 18f
                            rotate(-90f, Offset(w * 0.22f, h * 0.72f)) {
                                drawContext.canvas.nativeCanvas.drawText("ESTACIONAMIENTO", w * 0.22f, h * 0.72f, paintTexto)
                            }

                            drawRect(Color(0xFFD88C3A), Offset(w * 0.16f, h * 0.41f), Size(w * 0.11f, h * 0.05f))
                            drawRect(Color.White, Offset(w * 0.16f, h * 0.41f), Size(w * 0.11f, h * 0.05f), style = Stroke(1.5f))
                            drawRect(Color(0xFF3E9B56), Offset(w * 0.16f, h * 0.465f), Size(w * 0.11f, h * 0.055f))
                            drawRect(Color.White, Offset(w * 0.16f, h * 0.465f), Size(w * 0.11f, h * 0.055f), style = Stroke(1.5f))

                            rotate(-22f, Offset(w * 0.16f, h * 0.29f)) {
                                drawRect(colorPiso1, Offset(w * 0.07f, h * 0.22f), Size(w * 0.16f, h * 0.045f))
                                paintTexto.textSize = 18f
                                drawContext.canvas.nativeCanvas.drawText("AUDITORIO", w * 0.15f, h * 0.25f, paintTexto)
                                val aulasR = listOf("R5", "R6", "R4", "R1", "R3", "R2")
                                aulasR.forEachIndexed { idx, cod ->
                                    val col = idx % 2
                                    val fila = idx / 2
                                    val bx = w * (0.07f + col * 0.082f)
                                    val by = h * (0.27f + fila * 0.036f)
                                    drawRect(colorPiso1, Offset(bx, by), Size(w * 0.078f, h * 0.032f))
                                    paintTexto.textSize = 16f
                                    drawContext.canvas.nativeCanvas.drawText(cod, bx + w * 0.039f, by + h * 0.022f, paintTexto)
                                }
                            }

                            drawRect(Color(0xFF1E88E5), Offset(w * 0.29f, h * 0.21f), Size(w * 0.12f, h * 0.055f))
                            paintTexto.textSize = 16f
                            drawContext.canvas.nativeCanvas.drawText("CAFETERÍA", w * 0.35f, h * 0.235f, paintTexto)

                            val bloquesNorte = listOf("E9", "E10", "FAB LAB", "M9", "M10", "M11")
                            bloquesNorte.forEachIndexed { idx, nom ->
                                val bx = w * (0.44f + idx * 0.068f)
                                drawRect(colorPiso1, Offset(bx, h * 0.21f), Size(w * 0.064f, h * 0.06f))
                                paintTexto.textSize = 15f
                                drawContext.canvas.nativeCanvas.drawText(nom, bx + w * 0.032f, h * 0.245f, paintTexto)
                            }

                            drawRect(colorPiso1, Offset(w * 0.70f, h * 0.32f), Size(w * 0.13f, h * 0.09f))
                            paintTexto.textSize = 16f
                            drawContext.canvas.nativeCanvas.drawText("M8 / THINK BIG", w * 0.765f, h * 0.37f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.78f, h * 0.42f), Size(w * 0.07f, h * 0.08f))
                            drawContext.canvas.nativeCanvas.drawText("M4/M3", w * 0.815f, h * 0.465f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.69f, h * 0.50f), Size(w * 0.13f, h * 0.075f))
                            drawContext.canvas.nativeCanvas.drawText("M1-M13 / MEC", w * 0.755f, h * 0.545f, paintTexto)

                            val aulasE = listOf("E8", "E7", "E6", "E5", "E4", "E3B", "E3A", "E2", "E1")
                            aulasE.forEachIndexed { idx, cod ->
                                val bx = if (idx < 2) w * 0.44f else w * 0.35f
                                val by = if (idx < 2) h * (0.33f + idx * 0.032f) else h * (0.37f + (idx - 2) * 0.027f)
                                drawRect(colorPiso1, Offset(bx, by), Size(w * 0.075f, h * 0.024f))
                                paintTexto.textSize = 15f
                                drawContext.canvas.nativeCanvas.drawText(cod, bx + w * 0.037f, by + h * 0.018f, paintTexto)
                            }

                            drawRect(colorPiso1, Offset(w * 0.51f, h * 0.40f), Size(w * 0.11f, h * 0.10f))
                            paintTexto.textSize = 16f
                            drawContext.canvas.nativeCanvas.drawText("G1 - G11", w * 0.565f, h * 0.455f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.42f, h * 0.52f), Size(w * 0.19f, h * 0.045f))
                            drawContext.canvas.nativeCanvas.drawText("C1 - C6 (PAB. C)", w * 0.515f, h * 0.548f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.14f, h * 0.53f), Size(w * 0.045f, h * 0.05f))

                            drawRect(Color.White, Offset(w * 0.35f, h * 0.61f), Size(w * 0.24f, h * 0.055f))
                            drawRect(Color.Black, Offset(w * 0.35f, h * 0.61f), Size(w * 0.24f, h * 0.055f), style = Stroke(1.5f))
                            paintOscuro.textSize = 14f
                            drawContext.canvas.nativeCanvas.drawText("PLAZA CENTRAL", w * 0.47f, h * 0.642f, paintOscuro)

                            drawRect(colorPiso1, Offset(w * 0.38f, h * 0.68f), Size(w * 0.14f, h * 0.035f))
                            paintTexto.textSize = 14f
                            drawContext.canvas.nativeCanvas.drawText("D3 / TÓPICO", w * 0.45f, h * 0.703f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.39f, h * 0.74f), Size(w * 0.15f, h * 0.048f))
                            drawContext.canvas.nativeCanvas.drawText("COMEDOR", w * 0.465f, h * 0.77f, paintTexto)

                            drawCircle(Color(0xFFB0BEC5), radius = w * 0.055f, center = Offset(w * 0.62f, h * 0.75f))
                            drawCircle(Color(0xFF546E7A), radius = w * 0.055f, center = Offset(w * 0.62f, h * 0.75f), style = Stroke(2f))

                            drawRect(colorPiso1, Offset(w * 0.68f, h * 0.61f), Size(w * 0.032f, h * 0.17f))
                            drawRect(colorPiso2, Offset(w * 0.715f, h * 0.61f), Size(w * 0.032f, h * 0.17f))
                            drawRect(colorPiso3, Offset(w * 0.75f, h * 0.61f), Size(w * 0.032f, h * 0.17f))
                            paintTexto.textSize = 15f
                            drawContext.canvas.nativeCanvas.drawText("PAB. A", w * 0.73f, h * 0.70f, paintTexto)

                            drawRect(colorPiso1, Offset(w * 0.34f, h * 0.82f), Size(w * 0.055f, h * 0.11f))
                            drawRect(colorPiso2, Offset(w * 0.42f, h * 0.82f), Size(w * 0.04f, h * 0.11f))
                            drawRect(colorPiso1, Offset(w * 0.465f, h * 0.82f), Size(w * 0.04f, h * 0.11f))
                            drawRect(colorPiso3, Offset(w * 0.51f, h * 0.82f), Size(w * 0.04f, h * 0.11f))
                            drawContext.canvas.nativeCanvas.drawText("PAB. B", w * 0.485f, h * 0.88f, paintTexto)

                            drawRect(colorPiso2, Offset(w * 0.58f, h * 0.87f), Size(w * 0.21f, h * 0.045f))
                            paintTexto.textSize = 14f
                            drawContext.canvas.nativeCanvas.drawText("ADMIN / TESORERÍA", w * 0.685f, h * 0.898f, paintTexto)

                            paintOscuro.textSize = 15f
                            drawContext.canvas.nativeCanvas.drawText("INGRESO 2", w * 0.24f, h * 0.85f, paintOscuro)
                            drawContext.canvas.nativeCanvas.drawText("ENTRADA (INGRESO 1)", w * 0.84f, h * 0.94f, paintOscuro)

                            val leyendaPisos = listOf(
                                "PRIMER PISO" to colorPiso1,
                                "SEGUNDO PISO" to colorPiso2,
                                "TERCER PISO" to colorPiso3,
                                "CUARTO PISO" to colorPiso4
                            )
                            leyendaPisos.forEachIndexed { idx, (labelPiso, colPiso) ->
                                val ly = h * (0.88f + idx * 0.026f)
                                drawRect(colPiso, Offset(w * 0.03f, ly), Size(w * 0.16f, h * 0.022f))
                                paintTexto.textSize = 13f
                                drawContext.canvas.nativeCanvas.drawText(labelPiso, w * 0.11f, ly + h * 0.017f, paintTexto)
                            }

                            val origenUsuario = if (gpsActivo) {
                                val loc = ubicacionGps
                                if (loc != null && abs(loc.latitude - tecsupLat) < 1.0) {
                                    val dx = ((loc.longitude - tecsupLon) * 180.0).toFloat().coerceIn(-0.20f, 0.20f)
                                    val dy = ((tecsupLat - loc.latitude) * 180.0).toFloat().coerceIn(-0.22f, 0.22f)
                                    Offset(w * (0.55f + dx), h * (0.63f + dy))
                                } else {
                                    Offset(w * 0.55f, h * 0.63f)
                                }
                            } else {
                                Offset(w * 0.84f, h * 0.91f)
                            }

                            val destinoSector = Offset(w * sectorSeleccionado.rx, h * sectorSeleccionado.ry)
                            drawLine(
                                color = Color(0xFFD50000),
                                start = origenUsuario,
                                end = destinoSector,
                                strokeWidth = 6f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                            )

                            drawCircle(if (gpsActivo) Color(0xFF00B0FF) else Color(0xFFD50000), radius = 14f, center = origenUsuario)
                            drawCircle(Color.White, radius = 6f, center = origenUsuario)

                            sectoresOficialesTecsup.forEach { sec ->
                                val cx = w * sec.rx
                                val cy = h * sec.ry
                                val esSel = sec.numero == sectorSeleccionado.numero
                                val radio = if (esSel) 22f else 15f
                                drawCircle(
                                    color = if (esSel) Color(0xFFFF6D00) else Color(0xFFFBC02D),
                                    radius = radio,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = if (esSel) Color.White else Color(0xFF212121),
                                    radius = radio,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = if (esSel) 3.5f else 2f)
                                )
                                paintOscuro.textSize = if (esSel) 18f else 15f
                                drawContext.canvas.nativeCanvas.drawText(sec.numero, cx, cy + 5f, paintOscuro)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sectoresOficialesTecsup.forEach { sec ->
                        val sel = sec.numero == sectorSeleccionado.numero
                        Button(
                            onClick = { sectorSeleccionado = sec },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sel) Color(0xFFFF6D00) else Color(0xFFFFF8E1),
                                contentColor = if (sel) Color.White else Color(0xFF3E2723)
                            ),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("${sec.numero}. ${sec.titulo.take(12)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Sector [${sectorSeleccionado.numero}] — ${sectorSeleccionado.titulo}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0277BD)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ambientes y Aulas:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002B49))
                        Text(sectorSeleccionado.ambientes, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        } else {
            item {
                val loc = ubicacionGps
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                val gpsEnArequipa = gpsActivo && loc != null && calcularDistanciaKm(loc.latitude, loc.longitude, tecsupLat, tecsupLon) < 50.0
                val usuarioLatLng = if (gpsEnArequipa && loc != null) {
                    LatLng(loc.latitude, loc.longitude)
                } else {
                    LatLng(-16.422704, -71.530830) // Parque Lambramani / Av. Dolores, Arequipa
                }

                val distanciaKm = calcularDistanciaKm(usuarioLatLng.latitude, usuarioLatLng.longitude, tecsupLat, tecsupLon)
                val minutosViaje = ((distanciaKm * 3.4).roundToInt()).coerceAtLeast(4)
                val tieneClaveGoogleMaps = BuildConfig.MAPS_API_KEY.startsWith("AIza")

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mapa Exterior Real → Campus Tecsup Arequipa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "Normal" to MapType.NORMAL,
                                "Satélite / Híbrido" to MapType.HYBRID,
                                "Relieve" to MapType.TERRAIN
                            ).forEach { (nombreTipo, mType) ->
                                val activo = tipoMapaGoogle == mType
                                Button(
                                    onClick = { tipoMapaGoogle = mType },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activo) Color(0xFF0277BD) else Color(0xFFECEFF1),
                                        contentColor = if (activo) Color.White else Color(0xFF37474F)
                                    ),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(nombreTipo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (tieneClaveGoogleMaps) {
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(tecsupLatLng, 15.5f)
                            }
                            val tecsupMarkerState = rememberMarkerState(position = tecsupLatLng)
                            val userMarkerState = rememberMarkerState(position = usuarioLatLng)

                            LaunchedEffect(usuarioLatLng, gpsActivo) {
                                userMarkerState.position = usuarioLatLng
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        if (gpsActivo) usuarioLatLng else tecsupLatLng,
                                        15.2f
                                    ),
                                    durationMs = 1000
                                )
                            }

                            val campusTecsupPolygon = remember {
                                listOf(
                                    LatLng(-16.4301, -71.5204),
                                    LatLng(-16.4301, -71.5184),
                                    LatLng(-16.4323, -71.5184),
                                    LatLng(-16.4323, -71.5204)
                                )
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(10.dp))) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    properties = MapProperties(
                                        isMyLocationEnabled = gpsEnArequipa && tienePermiso,
                                        mapType = tipoMapaGoogle
                                    ),
                                    uiSettings = MapUiSettings(
                                        zoomControlsEnabled = true,
                                        myLocationButtonEnabled = gpsEnArequipa && tienePermiso,
                                        compassEnabled = true
                                    )
                                ) {
                                    Polygon(
                                        points = campusTecsupPolygon,
                                        strokeColor = Color(0xFF0277BD),
                                        fillColor = Color(0x330277BD),
                                        strokeWidth = 5f
                                    )
                                    Marker(
                                        state = tecsupMarkerState,
                                        title = "Campus Tecsup Arequipa",
                                        snippet = "Destino: ${proximaClase.aula} (Sector [${proximaClase.sectorNumero}])",
                                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                    )
                                    if (gpsActivo) {
                                        Marker(
                                            state = userMarkerState,
                                            title = "Tu Ubicación Actual (GPS)",
                                            snippet = "Distancia a Tecsup: ${String.format("%.1f", distanciaKm)} km (~$minutosViaje min)",
                                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                        )
                                        Polyline(
                                            points = listOf(usuarioLatLng, tecsupLatLng),
                                            color = Color(0xFFD50000),
                                            width = 12f,
                                            pattern = listOf(Dash(20f), Gap(10f)),
                                            startCap = RoundCap(),
                                            endCap = RoundCap(),
                                            jointType = JointType.ROUND
                                        )
                                    }
                                }
                            }
                        } else {
                            MapaNativoArequipaCanvas(
                                tecsupLatLng = tecsupLatLng,
                                usuarioLatLng = usuarioLatLng,
                                gpsActivo = gpsActivo,
                                tipoMapa = tipoMapaGoogle,
                                aulaDestino = proximaClase.aula,
                                sectorDestino = proximaClase.sectorNumero
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (gpsActivo) "Origen: Tu Ubicación Actual (GPS)" else "Origen: Entrada Campus Tecsup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF002B49)
                                )
                                Text("Destino: ${proximaClase.aula} (Sector [${proximaClase.sectorNumero}])", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            if (gpsActivo) {
                                Surface(color = Color(0xFFE1F5FE), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "${String.format("%.1f", distanciaKm)} km (~$minutosViaje min)",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0277BD)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val gmmUri = Uri.parse("google.navigation:q=-16.4312,-71.5194")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri)
                                try { context.startActivity(mapIntent) } catch (_: Exception) { }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar Ruta Turno a Turno en Google Maps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}