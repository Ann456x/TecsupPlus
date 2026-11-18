package com.tecsup.tecsupplus.ui.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.PerfilUsuarioExtraEntity
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ModoExamenSensorView(user: UsuarioEntity) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var bocaAbajoDetectado by remember { mutableStateOf(false) }
    var simulacionManual by remember { mutableStateOf(false) }
    var ejeZ by remember { mutableStateOf(9.8f) }
    var segundosConcentracion by remember { mutableStateOf(0) }
    var minutosTotalesGuardados by remember { mutableStateOf(0) }

    LaunchedEffect(user.username) {
        val perfil = db.extraDao().obtenerPerfilExtra(user.username)
        minutosTotalesGuardados = perfil?.minutosConcentracion ?: 25
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val z = event?.values?.getOrNull(2) ?: return
                ejeZ = z
                bocaAbajoDetectado = z < -8.2f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val modoActivo = bocaAbajoDetectado || simulacionManual

    LaunchedEffect(modoActivo) {
        while (modoActivo) {
            delay(1000L)
            segundosConcentracion += 1
            if (segundosConcentracion % 60 == 0) {
                minutosTotalesGuardados += 1
                val actual = db.extraDao().obtenerPerfilExtra(user.username) ?: PerfilUsuarioExtraEntity(username = user.username)
                db.extraDao().guardarPerfilExtra(actual.copy(minutosConcentracion = minutosTotalesGuardados))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Modo Examen / Concentración por Sensor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
        Text("Coloca el celular boca abajo sobre la carpeta (Acelerómetro Z < -8.2 m/s²) para activar el bloqueo de distracciones.", fontSize = 11.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (modoActivo) Color(0xFF1B5E20) else Color(0xFF002B49))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (modoActivo) "MODO EXAMEN ACTIVO (SILENCIO TOTAL)" else "MODO EN ESPERA (CELULAR BOCA ARRIBA)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (modoActivo) Color(0xFFA5D6A7) else Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lectura Sensor Acelerómetro Eje Z: ${String.format("%.2f", ejeZ)} m/s²", fontSize = 11.sp, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = String.format("%02d:%02d", segundosConcentracion / 60, segundosConcentracion % 60),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Tiempo acumulado en perfil: $minutosTotalesGuardados min de Concentración de Estudio", fontSize = 11.sp, color = Color.LightGray)
            }
        }

        Button(
            onClick = {
                simulacionManual = !simulacionManual
                if (!simulacionManual && segundosConcentracion > 0) {
                    scope.launch {
                        val extra = (segundosConcentracion / 60).coerceAtLeast(1)
                        minutosTotalesGuardados += extra
                        val actual = db.extraDao().obtenerPerfilExtra(user.username) ?: PerfilUsuarioExtraEntity(username = user.username)
                        db.extraDao().guardarPerfilExtra(actual.copy(minutosConcentracion = minutosTotalesGuardados))
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (simulacionManual) Color(0xFFC62828) else Color(0xFF0277BD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (simulacionManual) "Finalizar Sesión y Guardar en Perfil" else "Simular Celular Boca Abajo (Prueba en Emulador)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}