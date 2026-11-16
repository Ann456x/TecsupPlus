package tecsup.tecsupplus.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapaDualTecsupScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Ruta Exterior (Socabaya)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Mapa del Campus Arequipa") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> VistaRutaExteriorSocabaya()
            1 -> VistaMapaCampusTecsup()
        }
    }
}

@Composable
fun VistaRutaExteriorSocabaya() {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📍 Ubicación Actual: Socabaya, Arequipa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Destino: Sede Tecsup Sur", color = Color(0xFF0277BD), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("⏱️ Tiempo estimado de llegada: ~12 minutos")
            Text("📏 Distancia aproximada: 4.2 km")
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color(0xFF0277BD),
                        start = Offset(100f, 150f),
                        end = Offset(size.width - 100f, size.height - 150f),
                        strokeWidth = 8f
                    )
                    drawCircle(color = Color.Red, radius = 16f, center = Offset(100f, 150f))
                    drawCircle(color = Color(0xFF2E7D32), radius = 20f, center = Offset(size.width - 100f, size.height - 150f))
                }
                Text("Simulación de Ruta GPS Activa", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun VistaMapaCampusTecsup() {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🏢 Plano del Campus - Tecsup Arequipa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Próxima clase: Programación en Móviles (Lab Mecánica de Rocas)", fontSize = 13.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFECEFF1)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFF90CAF9), topLeft = Offset(50f, 50f), size = androidx.compose.ui.geometry.Size(300f, 180f))
                    drawRect(color = Color(0xFFA5D6A7), topLeft = Offset(400f, 50f), size = androidx.compose.ui.geometry.Size(280f, 400f))
                    drawCircle(color = Color(0xFFFFCC80), radius = 60f, center = Offset(250f, 350f))
                }
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏢 Bloque Académico", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(60.dp))
                    Text("💻 Laboratorios / Talleres", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
