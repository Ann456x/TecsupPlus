package com.tecsup.tecsupplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.remote.GeminiApiService
import com.tecsup.tecsupplus.data.repository.TecsupRepository
import com.tecsup.tecsupplus.ui.viewmodel.TecsupViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar dependencias de Retrofit, Room y Repositorio
        val database = AppDatabase.getDatabase(applicationContext)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val geminiService = retrofit.create(GeminiApiService::class.java)
        val repository = TecsupRepository(database.cursoDao(), database.asistenciaDao(), geminiService)
        val viewModel = TecsupViewModel(repository)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TecsupViewModel) {
    val cursos by viewModel.cursos.collectAsState()
    val scope = rememberCoroutineScope()

    var nombreCurso by remember { mutableStateOf("") }
    var docenteCurso by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "TecsupPlus - Gestión Académica", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombreCurso,
            onValueChange = { nombreCurso = it },
            label = { Text("Nombre del Curso") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = docenteCurso,
            onValueChange = { docenteCurso = it },
            label = { Text("Docente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (nombreCurso.isNotBlank() && docenteCurso.isNotBlank()) {
                    viewModel.agregarCurso(nombreCurso, docenteCurso, 4)
                    nombreCurso = ""
                    docenteCurso = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Curso")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cursos Registrados:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(cursos) { curso ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = curso.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Docente: \", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
