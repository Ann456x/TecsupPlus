package com.tecsup.tecsupplus.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CocinaTecsupView() {
    val fechaHoraActual = remember {
        SimpleDateFormat("EEEE dd/MM/yyyy • hh:mm a", Locale("es", "PE")).format(Date())
    }

    val menusDelDia = listOf(
        Triple("Menú Estudiantil (Comedor Sector 8.1)", "S/ 8.50 • Disponible 12:00 pm - 02:30 pm", "Entrada: Causa Rellena de Pollo | Fondo: Lomo Saltado con Arroz y Papas | Refresco: Chicha Morada"),
        Triple("Menú Ejecutivo / Saludable (Comedor 8.1)", "S/ 11.00 • Disponible 12:15 pm - 02:30 pm", "Entrada: Ensalada Fresca con Palta | Fondo: Filete de Pollo a la Plancha con Quinua | Infusión"),
        Triple("Combo Rápido Cafetería (Sector 8.2)", "S/ 6.50 • Disponible 07:30 am - 08:30 pm", "Triple de Pollo/Palta o Hamburguesa Artesanal + Jugo Natural de Papaya o Café Pasado"),
        Triple("Menú Cena Nocturna (Turno Noche 8.2)", "S/ 7.50 • Disponible 06:00 pm - 08:45 pm", "Arroz Chaufa de Pollo / Tallarines Verdes con Bistec + Mate caliente")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Cocina Tecsup — Comedor (8.1) y Cafetería (8.2)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text("Fecha y hora actual: $fechaHoraActual", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
        }

        items(menusDelDia.size) { idx ->
            val (titulo, horarioPrecio, detalle) = menusDelDia[idx]
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF002B49), modifier = Modifier.weight(1f))
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                            Text("Activo", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    Text(horarioPrecio, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                    Text(detalle, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}