package com.tecsup.tecsupplus.ui.view


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.RegistroAsistenciaPersistenteEntity
import com.tecsup.tecsupplus.data.local.RegistroNotaPersistenteEntity
import com.tecsup.tecsupplus.data.repository.TecsupRepository
import com.tecsup.tecsupplus.util.NotificationHelper
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.BuildConfig
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlumnoAsistenciaState(val nombreAlumno: String, estadoInicial: String) {
    var estado by mutableStateOf(estadoInicial)
}

data class AsistenciaDetalleModel(
    val codigo: String,
    val curso: String,
    val carreraAsociada: String,
    val docenteAsignado: String,
    var porcentaje: String,
    val historialAlumnos: MutableMap<String, SnapshotStateList<AlumnoAsistenciaState>>
)

class AlumnoCalificacionState(
    val nombreAlumno: String,
    n1: Double,
    n2: Double,
    n3: Double,
    n4: Double,
    n5: Double
) {
    var nota1 by mutableStateOf(n1)
    var nota2 by mutableStateOf(n2)
    var nota3 by mutableStateOf(n3)
    var nota4 by mutableStateOf(n4)
    var nota5 by mutableStateOf(n5)

    fun calcularPromedioFinal(): Double {
        return (nota1 * 0.30) + (nota2 * 0.20) + (nota3 * 0.20) + (nota4 * 0.20) + (nota5 * 0.10)
    }
}

data class CalificacionCursoModel(
    val curso: String,
    val codigo: String,
    val carreraAsociada: String,
    val docenteAsignado: String,
    val formula: String,
    val alumnosCalificaciones: SnapshotStateList<AlumnoCalificacionState>
)

data class CursoInscritoModel(val nombre: String, val codigo: String, val seccion: String, val detalle: String, val carreraAsociada: String, val docenteAsignado: String, val wppGroup: String, val horaClase: String, val diaClase: String, val aulaDocente: String)
data class ClaseDiaModel(val hora: String, val curso: String, val aulaDocente: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(user: UsuarioEntity, onLogout: () -> Unit) {
    var selectedTabByName by remember { mutableStateOf("Situación actual del estudiante") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val tecsupPrimary = Color(0xFF0277BD)

    val menuItems = listOf(
        "Situación actual del estudiante" to Icons.Default.Home,
        "Inscripción de Cursos" to Icons.Default.Info,
        "Horario de Clases" to Icons.Default.DateRange,
        "Asistencias (Semana 6)" to Icons.Default.CheckCircle,
        "Calificaciones (Detalladas)" to Icons.Default.Star,
        "Mapa del Campus (Arequipa)" to Icons.Default.LocationOn,
        "Encuestas" to Icons.Default.List,
        "Asistente IA (Gemini)" to Icons.Default.Send,
        "Perfil y Ayuda" to Icons.Default.Person
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(tecsupPrimary)
                        .padding(16.dp)
                ) {
                    Text(text = "Tecsup", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(text = "SEDE SUR - AREQUIPA", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    menuItems.forEach { (title, icon) ->
                        val isSelected = selectedTabByName == title
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null, tint = if (isSelected) tecsupPrimary else Color.White) },
                            label = { 
                                Text(
                                    title, 
                                    color = if (isSelected) tecsupPrimary else Color.White, 
                                    fontSize = 13.sp, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                selectedTabByName = title
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color.White,
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", color = Color.White)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = " T ",
                                    color = tecsupPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "TecsupPlus Arequipa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { selectedTabByName = "Mapa del Campus (Arequipa)" }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Mapa", tint = Color.White)
                        }
                        IconButton(onClick = { selectedTabByName = "Perfil y Ayuda" }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = tecsupPrimary)
                )
            },
            containerColor = Color(0xFFF0F7FF)
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                when (selectedTabByName) {
                    "Situación actual del estudiante" -> SituacionActualView(user)
                    "Inscripción de Cursos" -> InscripcionCursosView(user)
                    "Horario de Clases" -> HorarioClasesView(user)
                    "Asistencias (Semana 6)" -> AsistenciasView(user)
                    "Calificaciones (Detalladas)" -> CalificacionesView(user)
                    "Mapa del Campus (Arequipa)" -> MapaCampusArequipaView(user)
                    "Encuestas" -> EncuestasView()
                    "Asistente IA (Gemini)" -> AsistenteIAView(user)
                    "Perfil y Ayuda" -> PerfilYAwardView(user)
                }
            }
        }
    }
}

fun esAdminGeneral(user: UsuarioEntity): Boolean {
    val rol = user.rol?.uppercase() ?: ""
    return rol == "ADMIN" || rol == "DIRECTOR"
}

fun esProfesorAdmin(user: UsuarioEntity): Boolean {
    val rol = user.rol?.uppercase() ?: ""
    return esAdminGeneral(user) || rol.contains("PROFESOR_ADMIN") || rol.contains("TUTOR")
}

fun esProfesorNormal(user: UsuarioEntity): Boolean {
    val rol = user.rol?.uppercase() ?: ""
    return !esProfesorAdmin(user) && (rol.contains("DOCENTE") || rol.contains("PROFESOR") || rol.contains("PROFE"))
}

fun obtenerTodosLosCursosInscritos(): List<CursoInscritoModel> {
    return listOf(
        // CARRERA: SOFTWARE (9 cursos -> 1 por cada docente regular de Software)
        CursoInscritoModel("Aplicaciones del Cálculo y Estadística", "C92317", "Sección: C21-2-G", "4 Créditos", "Software", "Ing. Ricardo Alonso Pimentel", "https://wa.me/51949263864?text=Grupo_Calculo", "10:20 am", "Martes", "Aula A405"),
        CursoInscritoModel("Construcción y Pruebas de Software", "C66527", "Sección: C24-4-A", "5 Créditos", "Software", "Ing. Valeria Nicole Huertas", "https://wa.me/51949263864?text=Grupo_Construccion", "04:20 pm", "Lunes", "Aula A408 - Pabellón Tecnológico"),
        CursoInscritoModel("Ingeniería de Requerimientos y Diseño de Software", "C66538", "Sección: C24-3-B", "4 Créditos", "Software", "Ing. Gabriel Esteban Navarro", "https://wa.me/51949263864?text=Grupo_Requerimientos", "10:20 am", "Jueves", "Aula B301"),
        CursoInscritoModel("Investigación e Innovación Tecnológica", "C34327", "Sección: C24-4-A", "4 Créditos", "Software", "Ing. Andrea Jimena Salazar", "https://wa.me/51949263864?text=Grupo_Investigacion", "02:00 pm", "Viernes", "Aula A405"),
        CursoInscritoModel("Programación en Móviles", "C66541", "Sección: C24-4-A", "5 Créditos", "Software", "Ing. Marco Antonio Becerra", "https://wa.me/51949263864?text=Grupo_Moviles", "06:50 pm", "Lunes", "Lab Mecánica de Rocas"),
        CursoInscritoModel("Experiencias Formativas en Situaciones Reales de Trabajo 4", "C84475", "Sección: C24-4-A", "3 Créditos", "Software", "Ing. Daniel Alejandro León", "https://wa.me/51949263864?text=Grupo_EFRT4", "12:10 pm", "Miércoles", "Lab Mecánica de Rocas"),
        CursoInscritoModel("Tutoría 4", "C84474", "Sección: C21-4-H", "3 Créditos", "Software", "Ing. Karina Lizeth Ramos", "https://wa.me/51949263864?text=Grupo_Tutoria", "09:00 am", "Sábado", "Aula C102"),
        CursoInscritoModel("Arquitectura de Software en la Nube", "C66550", "Sección: C24-4-B", "4 Créditos", "Software", "Ing. Eduardo José Pastor", "https://wa.me/51949263864?text=Grupo_Nube", "08:00 am", "Jueves", "Aula A406"),
        CursoInscritoModel("Seguridad y Calidad de Software", "C66552", "Sección: C24-4-B", "4 Créditos", "Software", "Ing. Tatiana Marisol Cárdenas", "https://wa.me/51949263864?text=Grupo_Calidad", "03:40 pm", "Viernes", "Aula B204"),

        // CARRERA: MINAS (9 cursos -> 1 por cada docente regular de Minas)
        CursoInscritoModel("Geomecánica Aplicada a Minería", "M24-11021", "Sección: M24-4-A", "5 Créditos", "Minas", "Ing. Luis Alberto Mendoza", "https://wa.me/51949263864?text=Grupo_Geomecanica_Minas", "12:10 pm", "Martes", "Aula B301"),
        CursoInscritoModel("Perforación y Voladura de Rocas", "M24-11022", "Sección: M24-4-A", "4 Créditos", "Minas", "Ing. Mario Fernando Vargas", "https://wa.me/51949263864?text=Grupo_Perforacion_Minas", "04:20 pm", "Lunes", "Aula A408"),
        CursoInscritoModel("Planeamiento de Mina Tajo Abierto", "M24-11023", "Sección: M24-4-A", "4 Créditos", "Minas", "Ing. Sofía Lucía Rojas", "https://wa.me/51949263864?text=Grupo_Planeamiento_Minas", "10:20 am", "Martes", "Aula A405"),
        CursoInscritoModel("Seguridad y Salud Ocupacional Minera", "M24-11024", "Sección: M24-4-A", "3 Créditos", "Minas", "Ing. David Ernesto Cáceres", "https://wa.me/51949263864?text=Grupo_Seguridad_Minas", "08:00 am", "Miércoles", "Aula M101"),
        CursoInscritoModel("Ventilación y Servicios Auxiliares de Mina", "M24-11025", "Sección: M24-4-B", "4 Créditos", "Minas", "Ing. Claudia Milagros Medina", "https://wa.me/51949263864?text=Grupo_Ventilacion_Minas", "02:00 pm", "Jueves", "Aula M102"),
        CursoInscritoModel("Procesamiento de Minerales y Metalurgia", "M24-11026", "Sección: M24-4-B", "5 Créditos", "Minas", "Ing. Jorge Luis Paredes", "https://wa.me/51949263864?text=Grupo_Metalurgia", "10:20 am", "Viernes", "Lab Metalurgia"),
        CursoInscritoModel("Topografía y Geodesia Minera", "M24-11027", "Sección: M24-4-B", "4 Créditos", "Minas", "Ing. Victor Manuel Silva", "https://wa.me/51949263864?text=Grupo_Topografia", "08:00 am", "Martes", "Campo Topografía"),
        CursoInscritoModel("Estimación de Recursos y Reservas", "M24-11028", "Sección: M24-4-B", "4 Créditos", "Minas", "Ing. Rosa Elena Morales", "https://wa.me/51949263864?text=Grupo_Recursos", "04:20 pm", "Miércoles", "Aula M201"),
        CursoInscritoModel("Maquinaria y Transporte Minero", "M24-11029", "Sección: M24-4-B", "3 Créditos", "Minas", "Ing. Carlos Alberto Castro", "https://wa.me/51949263864?text=Grupo_Maquinaria", "09:00 am", "Sábado", "Taller Equipo Pesado"),

        // CARRERA: VIDEOJUEGOS (9 cursos -> 1 por cada docente regular de Videojuegos)
        CursoInscritoModel("Diseño de Niveles y Gameplay 3D", "V24-55011", "Sección: V24-4-A", "5 Créditos", "Videojuegos", "Ing. Bruno Alonso Morales", "https://wa.me/51949263864?text=Grupo_Niveles_3D", "12:10 pm", "Martes", "Aula B301"),
        CursoInscritoModel("Programación de Shaders en Unreal", "V24-55012", "Sección: V24-4-A", "5 Créditos", "Videojuegos", "Ing. Camila Alejandra Vega", "https://wa.me/51949263864?text=Grupo_Shaders_Unreal", "04:20 pm", "Lunes", "Aula A408"),
        CursoInscritoModel("Inteligencia Artificial para Videojuegos", "V24-55013", "Sección: V24-4-A", "4 Créditos", "Videojuegos", "Ing. Diego Alonso Torres", "https://wa.me/51949263864?text=Grupo_IA_Videojuegos", "10:20 am", "Martes", "Aula A405"),
        CursoInscritoModel("Modelado Orgánico ZBrush Avanzado", "V24-55014", "Sección: V24-4-A", "4 Créditos", "Videojuegos", "Ing. Fiorella Nicole Meléndez", "https://wa.me/51949263864?text=Grupo_ZBrush", "02:00 pm", "Jueves", "Lab VFX"),
        CursoInscritoModel("Física y Matemáticas para Motores 3D", "V24-55015", "Sección: V24-4-B", "4 Créditos", "Videojuegos", "Ing. Alonso Javier Paredes", "https://wa.me/51949263864?text=Grupo_Fisica3D", "08:00 am", "Miércoles", "Lab Motores 3D"),
        CursoInscritoModel("Animación Rigging y Motion Capture", "V24-55016", "Sección: V24-4-B", "4 Créditos", "Videojuegos", "Ing. Sebastián Enrique Ramos", "https://wa.me/51949263864?text=Grupo_Mocap", "02:00 pm", "Viernes", "Estudio Mocap"),
        CursoInscritoModel("Diseño Sonoro y Audio Interactivo", "V24-55017", "Sección: V24-4-B", "3 Créditos", "Videojuegos", "Ing. Lucía Fernanda Castillo", "https://wa.me/51949263864?text=Grupo_Audio", "10:20 am", "Jueves", "Lab Multimedia"),
        CursoInscritoModel("Interfaz de Usuario (UI/UX) en Videojuegos", "V24-55018", "Sección: V24-4-B", "5 Créditos", "Videojuegos", "Ing. Rodrigo Andrés Silva", "https://wa.me/51949263864?text=Grupo_Multiplayer", "04:20 pm", "Miércoles", "Lab Redes Gaming"),
        CursoInscritoModel("Arte Conceptual y Texturizado PBR", "V24-55019", "Sección: V24-4-B", "4 Créditos", "Videojuegos", "Ing. Melissa Alejandra Flores", "https://wa.me/51949263864?text=Grupo_ArtePBR", "09:00 am", "Sábado", "Lab Diseño Digital")
    )
}

fun obtenerAlumnosPorCurso(codigoCurso: String, carrera: String): List<String> {
    return when (codigoCurso) {
        // SOFTWARE: cada curso tiene su propio grupo de alumnos matriculados
        "C92317" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        "C66527" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        "C66538" -> listOf("Jhosep Sebastian Mamani", "Gianfranco Vargas", "Daniela Condori")
        "C34327" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        "C66541" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        "C84475" -> listOf("Jhosep Sebastian Mamani", "Andrés Felipe Huamán", "Sofía Ramos")
        "C84474" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        "C66550" -> listOf("Jhosep Sebastian Mamani", "Chrona Ruiz", "Andrés Felipe Huamán")
        "C66552" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")

        // MINAS: cada curso tiene su propio grupo de alumnos matriculados
        "M24-11021" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
        "M24-11022" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
        "M24-11023" -> listOf("Luciana Belén Valdivia", "Diego Armando Condori", "Kevin Ticona")
        "M24-11024" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
        "M24-11025" -> listOf("Luciana Belén Valdivia", "Valeria Choque", "Kevin Ticona")
        "M24-11026" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
        "M24-11027" -> listOf("Luciana Belén Valdivia", "Brayan Mamani", "Paola Quispe")
        "M24-11028" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
        "M24-11029" -> listOf("Luciana Belén Valdivia", "Diego Armando Condori", "Brayan Mamani")

        // VIDEOJUEGOS: cada curso tiene su propio grupo de alumnos matriculados
        "V24-55011" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
        "V24-55012" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
        "V24-55013" -> listOf("Ariana Nicole Sotomayor", "Camila Torres", "Gabriel Mendoza", "Matías Alejandro Benavides")
        "V24-55014" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
        "V24-55015" -> listOf("Ariana Nicole Sotomayor", "Ariana Nicole Sotomayor", "Gabriel Mendoza")
        "V24-55016" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
        "V24-55017" -> listOf("Ariana Nicole Sotomayor", "Renato Paredes", "Gabriel Mendoza")
        "V24-55018" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
        "V24-55019" -> listOf("Ariana Nicole Sotomayor", "Camila Torres", "Renato Paredes")

        else -> when (carrera.uppercase()) {
            "MINAS" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
            "VIDEOJUEGOS" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
            else -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
        }
    }
}

fun obtenerCursosParaUsuario(user: UsuarioEntity): List<CursoInscritoModel> {
    val todos = obtenerTodosLosCursosInscritos()
    return when {
        // Admin general ve TODOS los cursos de TODAS las carreras
        esAdminGeneral(user) -> todos

        // Profesor Admin ve TODOS los cursos de SU carrera
        esProfesorAdmin(user) -> {
            if (user.carrera.isNullOrBlank()) todos
            else todos.filter { it.carreraAsociada.equals(user.carrera, ignoreCase = true) }
        }

        // Profesor Normal ve SOLO su(s) curso(s) asignado(s)
        esProfesorNormal(user) -> {
            val cursosCarrera = if (user.carrera.isNullOrBlank()) todos
            else todos.filter { it.carreraAsociada.equals(user.carrera, ignoreCase = true) }

            val nombreUser = user.nombreCompleto.trim().uppercase()
            val porNombre = cursosCarrera.filter { curso ->
                val docente = curso.docenteAsignado.trim().uppercase()
                docente == nombreUser || docente.contains(nombreUser) || nombreUser.contains(docente)
            }
            if (porNombre.isNotEmpty()) {
                porNombre
            } else if (cursosCarrera.isNotEmpty()) {
                val numDocente = user.username.substringAfterLast("_").toIntOrNull() ?: 1
                val indice = (numDocente - 1).coerceAtLeast(0) % cursosCarrera.size
                listOf(cursosCarrera[indice])
            } else {
                emptyList()
            }
        }

        // Estudiante ve SOLO los cursos de su carrera donde está matriculado
        else -> {
            val cursosCarrera = if (user.carrera.isNullOrBlank()) todos
            else todos.filter { it.carreraAsociada.equals(user.carrera, ignoreCase = true) }

            val matriculados = cursosCarrera.filter { curso ->
                val lista = obtenerAlumnosPorCurso(curso.codigo, curso.carreraAsociada)
                lista.any { alumno ->
                    alumno.equals(user.nombreCompleto, ignoreCase = true) ||
                    user.nombreCompleto.contains(alumno, ignoreCase = true) ||
                    alumno.contains(user.nombreCompleto, ignoreCase = true)
                }
            }
            matriculados.ifEmpty { cursosCarrera }
        }
    }
}

val globalAsistenciasMap = mutableMapOf<String, AsistenciaDetalleModel>()
val globalCalificacionesMap = mutableMapOf<String, CalificacionCursoModel>()

fun obtenerAsistenciasMemoria(user: UsuarioEntity): List<AsistenciaDetalleModel> {
    val cursosFiltrados = obtenerCursosParaUsuario(user)

    return cursosFiltrados.map { curso ->
        globalAsistenciasMap.getOrPut(curso.codigo) {
            val alumnosCurso = obtenerAlumnosPorCurso(curso.codigo, curso.carreraAsociada)
            val mapaHistorial = mutableMapOf<String, SnapshotStateList<AlumnoAsistenciaState>>()
            listOf("Semana 1", "Semana 2", "Semana 3", "Semana 4", "Semana 5", "Semana 6").forEach { sem ->
                val estados = alumnosCurso.map { alumno -> AlumnoAsistenciaState(alumno, "Presente") }.toMutableList()
                mapaHistorial[sem] = SnapshotStateList<AlumnoAsistenciaState>().apply { addAll(estados) }
            }
            AsistenciaDetalleModel(
                codigo = curso.codigo,
                curso = curso.nombre,
                carreraAsociada = curso.carreraAsociada,
                docenteAsignado = curso.docenteAsignado,
                porcentaje = "88%",
                historialAlumnos = mapaHistorial
            )
        }
    }
}

fun obtenerCalificacionesMemoria(user: UsuarioEntity): List<CalificacionCursoModel> {
    val cursosFiltrados = obtenerCursosParaUsuario(user)

    return cursosFiltrados.map { curso ->
        globalCalificacionesMap.getOrPut(curso.codigo) {
            val alumnosCurso = obtenerAlumnosPorCurso(curso.codigo, curso.carreraAsociada)
            val listaAlumnStates = alumnosCurso.map { alumno ->
                AlumnoCalificacionState(
                    nombreAlumno = alumno,
                    n1 = 15.0,
                    n2 = 16.0,
                    n3 = 14.5,
                    n4 = 17.0,
                    n5 = 15.5
                )
            }.let { SnapshotStateList<AlumnoCalificacionState>().apply { addAll(it) } }

            CalificacionCursoModel(
                curso = curso.nombre,
                codigo = curso.codigo,
                carreraAsociada = curso.carreraAsociada,
                docenteAsignado = curso.docenteAsignado,
                formula = "Teoría (30%) + Lab 1 (20%) + Parcial (20%) + Lab 2 (20%) + Examen (10%) = 100%",
                alumnosCalificaciones = listaAlumnStates
            )
        }
    }
}

@Composable
fun AsistenciasView(user: UsuarioEntity) {
    val context = LocalContext.current
    val repo = remember { TecsupRepository(context) }
    val scope = rememberCoroutineScope()

    var cursoExpandido by remember { mutableStateOf<String?>(null) }
    var semanaSeleccionada by remember { mutableStateOf("Semana 6") }
    val listaAsistencias = remember(user) { obtenerAsistenciasMemoria(user) }

    // Cargar asistencias guardadas permanentemente en Room DB al iniciar
    LaunchedEffect(listaAsistencias) {
        val guardadas = repo.obtenerMapaAsistenciasGuardadas()
        listaAsistencias.forEach { curso ->
            curso.historialAlumnos.forEach { (sem, alumnos) ->
                alumnos.forEach { al ->
                    val clave = "${curso.codigo}|$sem|${al.nombreAlumno}"
                    guardadas[clave]?.let { estadoPersistido -> al.estado = estadoPersistido }
                }
            }
        }
    }

    var borradorAsistencias by remember { mutableStateOf(mapOf<String, String>()) }
    var mensajeGuardadoCurso by remember { mutableStateOf<String?>(null) }

    val esDocenteOAdmin = esProfesorAdmin(user) || esProfesorNormal(user)
    val subtituloRol = when {
        esAdminGeneral(user) -> "Vista Administrador General: Acceso total a todas las carreras, cursos y alumnos."
        esProfesorAdmin(user) -> "Vista Profesor Administrador (${user.carrera}): Acceso a todos los cursos y alumnos de tu carrera."
        esProfesorNormal(user) -> "Vista Docente: Gestiona únicamente los alumnos matriculados en tu curso asignado."
        else -> "Modo Estudiante: Consulta tus asistencias registradas en tus cursos matriculados."
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Control de Asistencia Institucional", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text(subtituloRol, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
        }
        items(listaAsistencias) { item ->
            val estaExpandido = cursoExpandido == item.codigo || listaAsistencias.size == 1
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Carrera: ${item.carreraAsociada} | Docente: ${item.docenteAsignado}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = item.curso, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                            Text(text = "Asistencia Global Promedio: ${item.porcentaje}", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        }
                        if (listaAsistencias.size > 1) {
                            IconButton(onClick = { cursoExpandido = if (estaExpandido) null else item.codigo }) {
                                Icon(if (estaExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.Add, contentDescription = null, tint = Color(0xFF0277BD))
                            }
                        }
                    }
                    if (estaExpandido) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val semanas = listOf("Semana 1", "Semana 2", "Semana 3", "Semana 4", "Semana 5", "Semana 6")
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            semanas.forEach { sem ->
                                val sel = semanaSeleccionada == sem
                                Button(
                                    onClick = {
                                        semanaSeleccionada = sem
                                        mensajeGuardadoCurso = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF0277BD) else Color(0xFFE1F5FE), contentColor = if (sel) Color.White else Color(0xFF0277BD)),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(sem, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val listaAlumnos = item.historialAlumnos[semanaSeleccionada] ?: mutableListOf()

                        val alumnosAMostrar = if (!esDocenteOAdmin) {
                            listaAlumnos.filter {
                                it.nombreAlumno.equals(user.nombreCompleto, ignoreCase = true) ||
                                user.nombreCompleto.contains(it.nombreAlumno, ignoreCase = true) ||
                                it.nombreAlumno.contains(user.nombreCompleto, ignoreCase = true)
                            }.ifEmpty { listOf(listaAlumnos.first()) }
                        } else {
                            listaAlumnos
                        }

                        Text(
                            if (esDocenteOAdmin) "Alumnos Matriculados en este Curso (${alumnosAMostrar.size}) - $semanaSeleccionada:"
                            else "Tu Estado de Asistencia en $semanaSeleccionada:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002B49)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        var hayCambiosSinGuardar = false

                        alumnosAMostrar.forEach { alumnoReg ->
                            val claveBorrador = "${item.codigo}|$semanaSeleccionada|${alumnoReg.nombreAlumno}"
                            val estadoMostrado = borradorAsistencias[claveBorrador] ?: alumnoReg.estado
                            val estaModificado = borradorAsistencias.containsKey(claveBorrador) && borradorAsistencias[claveBorrador] != alumnoReg.estado
                            if (estaModificado) hayCambiosSinGuardar = true

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (esDocenteOAdmin) "• ${alumnoReg.nombreAlumno}" else "• ${user.nombreCompleto} (Tú)",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    if (estaModificado) {
                                        Text("  * Pendiente de guardar en BD", fontSize = 9.sp, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (estadoMostrado == "Presente") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = estadoMostrado,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (estadoMostrado == "Presente") Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                        )
                                    }
                                    if (esDocenteOAdmin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                mensajeGuardadoCurso = null
                                                val nuevoEstado = if (estadoMostrado == "Presente") "Ausente" else "Presente"
                                                val mapaActualizado = borradorAsistencias.toMutableMap()
                                                if (nuevoEstado == alumnoReg.estado) {
                                                    mapaActualizado.remove(claveBorrador)
                                                } else {
                                                    mapaActualizado[claveBorrador] = nuevoEstado
                                                }
                                                borradorAsistencias = mapaActualizado
                                            },
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                                        ) {
                                            Text("Cambiar", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (esDocenteOAdmin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val mapaRestante = borradorAsistencias.toMutableMap()
                                        val aPersistir = mutableListOf<RegistroAsistenciaPersistenteEntity>()
                                        alumnosAMostrar.forEach { alumnoReg ->
                                            val clave = "${item.codigo}|$semanaSeleccionada|${alumnoReg.nombreAlumno}"
                                            val nuevoVal = mapaRestante.remove(clave)
                                            if (nuevoVal != null) {
                                                alumnoReg.estado = nuevoVal
                                            }
                                            aPersistir.add(
                                                RegistroAsistenciaPersistenteEntity(
                                                    clave = clave,
                                                    codigoCurso = item.codigo,
                                                    semana = semanaSeleccionada,
                                                    nombreAlumno = alumnoReg.nombreAlumno,
                                                    estado = alumnoReg.estado
                                                )
                                            )
                                        }
                                        borradorAsistencias = mapaRestante
                                        mensajeGuardadoCurso = "${item.codigo}_$semanaSeleccionada"
                                        scope.launch {
                                            repo.guardarAsistencias(aPersistir)
                                            NotificationHelper.enviarNotificacionPush(
                                                context,
                                                "Asistencia Guardada (${item.curso})",
                                                "Se actualizaron permanentemente las asistencias de $semanaSeleccionada."
                                            )
                                        }
                                    },
                                    enabled = hayCambiosSinGuardar,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32),
                                        disabledContainerColor = Color(0xFFBDBDBD)
                                    ),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text(
                                        text = if (hayCambiosSinGuardar) "Guardar Cambios" else "Sin cambios pendientes",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                if (hayCambiosSinGuardar) {
                                    OutlinedButton(
                                        onClick = {
                                            val mapaRestante = borradorAsistencias.toMutableMap()
                                            alumnosAMostrar.forEach { alumnoReg ->
                                                mapaRestante.remove("${item.codigo}|$semanaSeleccionada|${alumnoReg.nombreAlumno}")
                                            }
                                            borradorAsistencias = mapaRestante
                                        },
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Descartar", fontSize = 11.sp, color = Color(0xFFC62828))
                                    }
                                }
                            }

                            if (mensajeGuardadoCurso == "${item.codigo}_$semanaSeleccionada") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Guardado permanentemente en base de datos ($semanaSeleccionada).",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalificacionesView(user: UsuarioEntity) {
    val context = LocalContext.current
    val repo = remember { TecsupRepository(context) }
    val scope = rememberCoroutineScope()

    val calificacionesCursos = remember(user) { obtenerCalificacionesMemoria(user) }
    val cursosInfoMap = remember { obtenerTodosLosCursosInscritos().associateBy { it.codigo } }
    val esDocenteOAdmin = esProfesorAdmin(user) || esProfesorNormal(user)

    var vistaConsolidado by remember { mutableStateOf(false) }

    // Cargar notas guardadas permanentemente en Room DB al abrir
    LaunchedEffect(calificacionesCursos) {
        val guardadas = repo.obtenerMapaNotasGuardadas()
        calificacionesCursos.forEach { curso ->
            curso.alumnosCalificaciones.forEach { al ->
                val clave = "${curso.codigo}|${al.nombreAlumno}"
                guardadas[clave]?.let { saved ->
                    al.nota1 = saved.nota1
                    al.nota2 = saved.nota2
                    al.nota3 = saved.nota3
                    al.nota4 = saved.nota4
                    al.nota5 = saved.nota5
                }
            }
        }
    }

    var borradorNotas by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var alumnoGuardadoMensaje by remember { mutableStateOf<String?>(null) }

    val subtituloNotas = when {
        esAdminGeneral(user) -> "Vista Administrador General: Visualiza y edita calificaciones de todas las carreras."
        esProfesorAdmin(user) -> "Vista Profesor Administrador (${user.carrera}): Visualiza todos los cursos y alumnos de tu carrera."
        esProfesorNormal(user) -> "Vista Docente: Edita las notas de tus alumnos matriculados (0-20) y presiona 'Guardar Notas'."
        else -> "Consulta tus calificaciones por semana o revisa tu Consolidado Ponderado del ciclo."
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Calificaciones y Consolidado Académico", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text(subtituloNotas, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Selector Detalle por Curso vs Consolidado General
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vistaConsolidado = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!vistaConsolidado) Color(0xFF0277BD) else Color(0xFFE1F5FE),
                        contentColor = if (!vistaConsolidado) Color.White else Color(0xFF0277BD)
                    ),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("Detalle por Curso", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { vistaConsolidado = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vistaConsolidado) Color(0xFF0277BD) else Color(0xFFE1F5FE),
                        contentColor = if (vistaConsolidado) Color.White else Color(0xFF0277BD)
                    ),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("Consolidado General", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (vistaConsolidado) {
            if (!esDocenteOAdmin) {
                // CONSOLIDADO BOLETA PONDERADA DEL ESTUDIANTE
                item {
                    var sumaPonderada = 0.0
                    var totalCreditos = 0
                    var creditosAprobados = 0
                    val filasBoleta = calificacionesCursos.map { curso ->
                        val al = curso.alumnosCalificaciones.firstOrNull {
                            it.nombreAlumno.equals(user.nombreCompleto, ignoreCase = true) ||
                            user.nombreCompleto.contains(it.nombreAlumno, ignoreCase = true) ||
                            it.nombreAlumno.contains(user.nombreCompleto, ignoreCase = true)
                        } ?: curso.alumnosCalificaciones.first()

                        val prom = al.calcularPromedioFinal()
                        val cred = 4
                        sumaPonderada += prom * cred
                        totalCreditos += cred
                        if (prom >= 13.0) creditosAprobados += cred
                        Triple(curso.curso, cred, prom)
                    }
                    val promedioPonderadoCiclo = if (totalCreditos > 0) sumaPonderada / totalCreditos else 0.0

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF002B49))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("CONSOLIDADO OFICIAL DE NOTAS DEL CICLO", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFD54F))
                            Text("Estudiante: ${user.nombreCompleto} | Carrera: ${user.carrera ?: "General"}", fontSize = 11.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Promedio Ponderado", fontSize = 10.sp, color = Color.LightGray)
                                    Text(String.format("%.2f", promedioPonderadoCiclo), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Créditos Aprobados", fontSize = 10.sp, color = Color.LightGray)
                                    Text("$creditosAprobados / $totalCreditos cr.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Resumen Consolidado por Asignatura", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0277BD))
                            filasBoleta.forEach { (nombreCurso, cred, prom) ->
                                val aprobado = prom >= 13.0
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(nombreCurso, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF002B49))
                                        Text("$cred Créditos • ${if (aprobado) "APROBADO" else "EN RIESGO"}", fontSize = 10.sp, color = if (aprobado) Color(0xFF2E7D32) else Color(0xFFC62828))
                                    }
                                    Surface(color = if (aprobado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = String.format("%.2f", prom),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (aprobado) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                                Divider()
                            }

                            Button(
                                onClick = {
                                    val resumenTexto = buildString {
                                        appendLine("CONSOLIDADO ACADÉMICO TECSUPPLUS")
                                        appendLine("Estudiante: ${user.nombreCompleto}")
                                        appendLine("Promedio Ponderado: ${String.format("%.2f", promedioPonderadoCiclo)}")
                                        filasBoleta.forEach { (c, cr, p) -> appendLine("- $c ($cr cr): ${String.format("%.2f", p)}") }
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, resumenTexto)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Compartir Consolidado de Notas"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Exportar / Compartir Consolidado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // CONSOLIDADO ACTA RESUMEN PARA DOCENTES Y ADMINS
                items(calificacionesCursos) { curso ->
                    val promedios = curso.alumnosCalificaciones.map { it.calcularPromedioFinal() }
                    val promAula = if (promedios.isNotEmpty()) promedios.average() else 0.0
                    val aprobados = promedios.count { it >= 13.0 }
                    val pctAprobados = if (promedios.isNotEmpty()) (aprobados * 100) / promedios.size else 0

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Acta Consolidada: ${curso.curso}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                            Text("Carrera: ${curso.carreraAsociada} | Docente: ${curso.docenteAsignado}", fontSize = 11.sp, color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Promedio Aula: ${String.format("%.2f", promAula)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002B49))
                                Text("Aprobados: $aprobados/${promedios.size} ($pctAprobados%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Divider()
                            curso.alumnosCalificaciones.forEach { al ->
                                val pf = al.calcularPromedioFinal()
                                val aprobado = pf >= 13.0
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("• ${al.nombreAlumno}", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${String.format("%.2f", pf)} (${if (aprobado) "Aprobado" else "Desaprobado"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (aprobado) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // VISTA DETALLE POR SEMANA (CON GUARDADO PERSISTENTE EN ROOM DB + PUSH)
            items(calificacionesCursos) { item ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${item.curso} (${item.carreraAsociada})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                        Text(text = "Docente: ${item.docenteAsignado}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(text = "Fórmula: ${item.formula}", fontSize = 10.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        val alumnosAMostrar = if (!esDocenteOAdmin) {
                            item.alumnosCalificaciones.filter {
                                it.nombreAlumno.equals(user.nombreCompleto, ignoreCase = true) ||
                                user.nombreCompleto.contains(it.nombreAlumno, ignoreCase = true) ||
                                it.nombreAlumno.contains(user.nombreCompleto, ignoreCase = true)
                            }.ifEmpty { listOf(item.alumnosCalificaciones.first()) }
                        } else {
                            item.alumnosCalificaciones
                        }

                        alumnosAMostrar.forEach { alumno ->
                            val claveAlumno = "${item.codigo}|${alumno.nombreAlumno}"
                            val notasOficiales = listOf(alumno.nota1, alumno.nota2, alumno.nota3, alumno.nota4, alumno.nota5)
                            val textosBorrador = borradorNotas[claveAlumno] ?: notasOficiales.map { it.toString() }

                            val notasPreview = textosBorrador.mapIndexed { idx, txt ->
                                val parsed = txt.toDoubleOrNull()
                                if (parsed != null && parsed in 0.0..20.0) parsed else notasOficiales[idx]
                            }
                            val promedioPreview = notasPreview[0] * 0.30 + notasPreview[1] * 0.20 + notasPreview[2] * 0.20 + notasPreview[3] * 0.20 + notasPreview[4] * 0.10
                            val hayCambiosNotas = borradorNotas.containsKey(claveAlumno) && notasPreview != notasOficiales

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = if (esDocenteOAdmin) alumno.nombreAlumno else "${user.nombreCompleto} (Tú)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF002B49))
                                            if (hayCambiosNotas) {
                                                Text("Notas editadas sin guardar en BD", fontSize = 9.sp, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "Promedio: ${String.format("%.2f", if (hayCambiosNotas) promedioPreview else alumno.calcularPromedioFinal())}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val etiquetas = listOf("S1 (Teoría)", "S2 (Lab 1)", "S3 (Parcial)", "S4 (Lab 2)", "S5 (Examen)")

                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        etiquetas.forEachIndexed { idx, etiqueta ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(etiqueta, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (esDocenteOAdmin) {
                                                    OutlinedTextField(
                                                        value = textosBorrador[idx],
                                                        onValueChange = { nuevoValor ->
                                                            alumnoGuardadoMensaje = null
                                                            val listaMutable = textosBorrador.toMutableList()
                                                            listaMutable[idx] = nuevoValor
                                                            val mapaActualizado = borradorNotas.toMutableMap()
                                                            mapaActualizado[claveAlumno] = listaMutable
                                                            borradorNotas = mapaActualizado
                                                        },
                                                        modifier = Modifier.width(65.dp).height(50.dp),
                                                        singleLine = true,
                                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                    )
                                                } else {
                                                    Surface(color = Color(0xFFE1F5FE), shape = RoundedCornerShape(4.dp)) {
                                                        Text(
                                                            text = "${notasOficiales[idx]}",
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0277BD),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (esDocenteOAdmin) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    alumno.nota1 = notasPreview[0]
                                                    alumno.nota2 = notasPreview[1]
                                                    alumno.nota3 = notasPreview[2]
                                                    alumno.nota4 = notasPreview[3]
                                                    alumno.nota5 = notasPreview[4]
                                                    val mapaActualizado = borradorNotas.toMutableMap()
                                                    mapaActualizado.remove(claveAlumno)
                                                    borradorNotas = mapaActualizado
                                                    alumnoGuardadoMensaje = claveAlumno

                                                    scope.launch {
                                                        repo.guardarNotaAlumno(
                                                            RegistroNotaPersistenteEntity(
                                                                clave = claveAlumno,
                                                                codigoCurso = item.codigo,
                                                                nombreAlumno = alumno.nombreAlumno,
                                                                nota1 = alumno.nota1,
                                                                nota2 = alumno.nota2,
                                                                nota3 = alumno.nota3,
                                                                nota4 = alumno.nota4,
                                                                nota5 = alumno.nota5
                                                            )
                                                        )
                                                        NotificationHelper.enviarNotificacionPush(
                                                            context,
                                                            "Calificaciones Actualizadas (${item.curso})",
                                                            "Se guardaron permanentemente las notas de ${alumno.nombreAlumno} (Promedio: ${String.format("%.2f", alumno.calcularPromedioFinal())})."
                                                        )
                                                    }
                                                },
                                                enabled = hayCambiosNotas,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF2E7D32),
                                                    disabledContainerColor = Color(0xFFBDBDBD)
                                                ),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text(
                                                    text = if (hayCambiosNotas) "Guardar Notas" else "Notas al día",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            if (hayCambiosNotas) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val mapaActualizado = borradorNotas.toMutableMap()
                                                        mapaActualizado.remove(claveAlumno)
                                                        borradorNotas = mapaActualizado
                                                    },
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("Descartar", fontSize = 11.sp, color = Color(0xFFC62828))
                                                }
                                            }
                                        }

                                        if (alumnoGuardadoMensaje == claveAlumno) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Notas de ${alumno.nombreAlumno} guardadas en Room DB.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorarioClasesView(user: UsuarioEntity) {
    var esVistaGeneral by remember { mutableStateOf(true) }
    val cursosDelUser = obtenerCursosParaUsuario(user)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("HORARIO DE CLASES - 2026-2", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0277BD))
                Text(text = if (esVistaGeneral) "Modo: Matriz General Semanal" else "Modo: Detalle por Día", fontSize = 11.sp, color = Color.Gray)
            }
            Button(onClick = { esVistaGeneral = !esVistaGeneral }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)), modifier = Modifier.height(36.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (esVistaGeneral) "Cambiar a Por Día" else "Cambiar a General", fontSize = 11.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (esVistaGeneral) {
            val horizontalScrollState = rememberScrollState()
            Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp).horizontalScroll(horizontalScrollState)) {
                    Row(modifier = Modifier.background(Color(0xFF0277BD)).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.Center) { Text("Hora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado").forEach { dia ->
                            Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.Center) { Text(dia, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    cursosDelUser.forEach { curso ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(70.dp).background(Color(0xFFF0F7FF)).padding(4.dp), contentAlignment = Alignment.Center) { Text(curso.horaClase, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD)) }
                            listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado").forEach { dia ->
                                val esDiaClase = curso.diaClase.equals(dia, ignoreCase = true)
                                Box(
                                    modifier = Modifier.width(110.dp).height(55.dp).background(if (esDiaClase) Color(0xFFE1F5FE) else Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (esDiaClase) {
                                        Text("${curso.codigo}\n${curso.aulaDocente}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            var diaSeleccionado by remember { mutableStateOf("Lunes") }
            val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
            val scrollDiasState = rememberScrollState()
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollDiasState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dias.forEach { dia ->
                    val seleccionado = dia == diaSeleccionado
                    Button(onClick = { diaSeleccionado = dia }, colors = ButtonDefaults.buttonColors(containerColor = if (seleccionado) Color(0xFF0277BD) else Color.White, contentColor = if (seleccionado) Color.White else Color(0xFF0277BD)), modifier = Modifier.height(38.dp)) {
                        Text(dia, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val clasesFiltradasDia = cursosDelUser.filter { it.diaClase.equals(diaSeleccionado, ignoreCase = true) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                if (clasesFiltradasDia.isEmpty()) {
                    item {
                        Text("No hay clases programadas para el día $diaSeleccionado.", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    items(clasesFiltradasDia) { curso ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(color = Color(0xFFE1F5FE), shape = RoundedCornerShape(4.dp)) {
                                    Text(curso.horaClase, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("${curso.nombre} (${curso.seccion})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF002B49))
                                Text("Ubicación: ${curso.aulaDocente} | Docente: ${curso.docenteAsignado}", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapaCampusArequipaView(user: UsuarioEntity) {
    CampusMapScreen(user)
}

@Composable
fun SituacionActualView(user: UsuarioEntity) {
    val cursos = remember(user) { obtenerCursosParaUsuario(user) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Usuario: ${user.nombreCompleto}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Rol: ${user.rol} | Carrera: ${user.carrera ?: "General"}", fontSize = 13.sp, color = Color.DarkGray)
                    Text(text = "Sede: Arequipa | Estado: ACTIVO (Semana 6)", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Text(if (esProfesorNormal(user)) "Mi Curso Asignado" else "Resumen de Cursos y Accesos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0277BD)) }
        items(cursos) { item ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Docente: ${item.docenteAsignado} | ${item.detalle}", fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun InscripcionCursosView(user: UsuarioEntity) {
    val cursosInscritos = remember(user) { obtenerCursosParaUsuario(user) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text(if (esProfesorNormal(user)) "Mi Curso y Enlace de Comunicación" else "Cursos y Enlaces a Grupos de WhatsApp", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text("Haz clic en 'Grupo' para unirte al chat oficial del curso.", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(cursosInscritos) { item ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0277BD), modifier = Modifier.weight(1f))
                        Surface(color = Color(0xFFE1F5FE), shape = RoundedCornerShape(4.dp)) {
                            Text(item.seccion, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Código: ${item.codigo} | Docente: ${item.docenteAsignado}", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF25D366),
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Canal de Comunicación", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                Text("Enlace oficial para coordinaciones", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Grupo",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EncuestasView() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Encuestas Institucionales", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Evaluación Docente Ciclo 2026-2", fontWeight = FontWeight.Bold)
                Text("Estado: Pendiente por realizar", fontSize = 12.sp, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))) { Text("Completar Encuesta", color = Color.White) }
            }
        }
    }
}

@Composable
fun AsistenteIAView(user: UsuarioEntity) {
    val context = LocalContext.current
    val repo = remember { TecsupRepository(context) }
    val scope = rememberCoroutineScope()

    // Solicitar permiso de notificaciones Push en Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        NotificationHelper.crearCanal(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Estado real de conexión Online / Offline
    val estaOnline = remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(net)
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    var promptTexto by remember { mutableStateOf("") }
    var cargandoIA by remember { mutableStateOf(false) }
    var abrirNearpodInline by remember { mutableStateOf(false) }

    val historialChat = remember {
        mutableStateListOf(
            "IA TecsupPlus (Groq Llama 3.1)" to "¡Hola ${user.nombreCompleto}! Soy tu asistente académico conectado a Groq IA. Puedes escribirme o usar el botón de micrófono para dictarme por voz."
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // Control por Voz (Speech-to-Text nativo de Android)
    val vozLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoDictado = matches?.firstOrNull()
            if (!textoDictado.isNullOrBlank()) {
                promptTexto = textoDictado
            }
        }
    }

    if (abrirNearpodInline) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedButton(
                onClick = { abrirNearpodInline = false },
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                Text("Volver al Asistente IA por Voz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            AprendizajeCognitivoView(user)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Asistente Académico IA + Voz", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
                    Text("Impulsado por Groq (Llama 3.1 8B Instant)", fontSize = 11.sp, color = Color.Gray)
                }
                Surface(
                    color = if (estaOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (estaOnline) "● Online" else "● Modo Offline",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Accesos rápidos a Aprendizaje Cognitivo y Notificación Push de prueba
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { abrirNearpodInline = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Aprendizaje Cognitivo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        NotificationHelper.enviarNotificacionPush(
                            context,
                            "Recordatorio TecsupPlus",
                            "Tu siguiente clase comienza en 15 minutos. ¡Revisa tu aula en el Plano del Campus!"
                        )
                    },
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Probar Push", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                }
            }
        }

        items(historialChat.size) { index ->
            val (emisor, mensaje) = historialChat[index]
            val esUsuario = emisor == "Tú"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (esUsuario) Color(0xFFE1F5FE) else Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(emisor, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (esUsuario) Color(0xFF01579B) else Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mensaje, fontSize = 12.sp, color = Color(0xFF002B49))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = promptTexto,
                        onValueChange = { promptTexto = it },
                        placeholder = { Text("Escribe tu consulta o presiona Hablar...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-PE")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicta tu consulta para la IA...")
                                }
                                try {
                                    vozLauncher.launch(intent)
                                } catch (_: Exception) {
                                    historialChat.add("Sistema" to "El reconocimiento de voz no está disponible en este dispositivo.")
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Hablar (Voz)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                        }

                        Button(
                            onClick = {
                                val pregunta = promptTexto.trim()
                                if (pregunta.isNotBlank() && !cargandoIA) {
                                    historialChat.add("Tú" to pregunta)
                                    promptTexto = ""
                                    cargandoIA = true
                                    scope.launch {
                                        val resumenDatos = buildString {
                                            val notasGuardadas = repo.obtenerMapaNotasGuardadas()
                                            obtenerCalificacionesMemoria(user).forEach { c ->
                                                appendLine("Curso: ${c.curso} (${c.codigo}) - Docente: ${c.docenteAsignado}")
                                                c.alumnosCalificaciones.forEach { al ->
                                                    val g = notasGuardadas["${c.codigo}|${al.nombreAlumno}"]
                                                    val prom = if (g != null) (g.nota1 * 0.30 + g.nota2 * 0.20 + g.nota3 * 0.20 + g.nota4 * 0.20 + g.nota5 * 0.10) else al.calcularPromedioFinal()
                                                    appendLine("  - Alumno: ${al.nombreAlumno} | Promedio: ${String.format("%.2f", prom)} (${if (prom >= 13.0) "Aprobado" else "En riesgo / Desaprobado"})")
                                                }
                                            }
                                        }
                                        val res = repo.consultarAsistenteIA(pregunta, user.rol ?: "ESTUDIANTE", user.carrera, resumenDatos)
                                        cargandoIA = false
                                        res.onSuccess { resp ->
                                            historialChat.add("IA TecsupPlus (Groq)" to resp)
                                        }.onFailure { err ->
                                            historialChat.add("IA TecsupPlus (Offline/Aviso)" to "No se pudo conectar con Groq (${err.message}). Verifica tu internet.")
                                        }
                                    }
                                }
                            },
                            enabled = !cargandoIA && promptTexto.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(if (cargandoIA) "Pensando..." else "Enviar a IA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilYAwardView(user: UsuarioEntity) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var bio by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var googleUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf("") }
    var galeriaFotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var minutosConcentracion by remember { mutableStateOf(0) }
    var arcadeRecord by remember { mutableStateOf(0) }
    var guardadoOk by remember { mutableStateOf(false) }

    var comentarios by remember { mutableStateOf<List<com.tecsup.tecsupplus.data.local.ComentarioMuroEntity>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }

    val pickerFotoPerfil = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) fotoUri = uri.toString()
    }
    val pickerGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) galeriaFotos = galeriaFotos + uri.toString()
    }

    LaunchedEffect(user.username) {
        val extra = db.extraDao().obtenerPerfilExtra(user.username)
        if (extra != null) {
            bio = extra.bio
            githubUrl = extra.githubUrl
            youtubeUrl = extra.youtubeUrl
            googleUrl = extra.googleUrl
            linkedinUrl = extra.linkedinUrl
            fotoUri = extra.fotoPerfilUri
            minutosConcentracion = extra.minutosConcentracion
            arcadeRecord = extra.arcadeHighScore
        }
        comentarios = db.extraDao().obtenerComentariosPerfil(user.username)
    }

    val insignias = listOf(
        Triple("🏆 Madrugador", "Asistir a primera hora 5 semanas seguidas", true),
        Triple("🧠 Atento", "Quedar en el Top 3 del reto cognitivo", true),
        Triple("⚡ Puntualidad Extrema", "Entregar laboratorios antes del límite", true),
        Triple("🎯 Mente Enfocada", "Sesiones en Modo Examen ($minutosConcentracion min)", minutosConcentracion > 0)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF002B49))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Text("Rol: ${user.rol} | Carrera: ${user.carrera ?: "Institucional"} | Récord Arcade: $arcadeRecord pts", fontSize = 11.sp, color = Color(0xFFFFD54F))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickerFotoPerfil.launch("image/*") }) {
                            Text(if (fotoUri.isBlank()) "Subir Foto Perfil" else "Cambiar Foto Perfil", fontSize = 10.sp, color = Color.White)
                        }
                        OutlinedButton(onClick = { pickerGaleria.launch("image/*") }) {
                            Text("+ Añadir Foto Galería (${galeriaFotos.size})", fontSize = 10.sp, color = Color(0xFFFFD54F))
                        }
                    }
                }
            }
        }

        // Insignias y Logros (Gamificación / Badges)
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Insignias y Logros Desbloqueados (${insignias.count { it.third }}/${insignias.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                    insignias.forEach { (nombre, desc, desbloqueada) ->
                        Surface(
                            color = if (desbloqueada) Color(0xFFFFF8E1) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3E2723))
                                    Text(desc, fontSize = 10.sp, color = Color.DarkGray)
                                }
                                Text(if (desbloqueada) "OBTENIDA" else "BLOQUEADA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (desbloqueada) Color(0xFF2E7D32) else Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Edición de Descripción y Enlaces (GitHub, YouTube, Google, LinkedIn)
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Personalizar Perfil y Redes Profesionales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                    OutlinedTextField(value = bio, onValueChange = { bio = it; guardadoOk = false }, label = { Text("Descripción / Biografía", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = githubUrl, onValueChange = { githubUrl = it; guardadoOk = false }, label = { Text("Enlace GitHub (https://github.com/...)", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it; guardadoOk = false }, label = { Text("Enlace YouTube", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = googleUrl, onValueChange = { googleUrl = it; guardadoOk = false }, label = { Text("Enlace Google / Portafolio", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = linkedinUrl, onValueChange = { linkedinUrl = it; guardadoOk = false }, label = { Text("Enlace LinkedIn", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            scope.launch {
                                db.extraDao().guardarPerfilExtra(
                                    com.tecsup.tecsupplus.data.local.PerfilUsuarioExtraEntity(
                                        username = user.username,
                                        bio = bio,
                                        githubUrl = githubUrl,
                                        youtubeUrl = youtubeUrl,
                                        googleUrl = googleUrl,
                                        linkedinUrl = linkedinUrl,
                                        fotoPerfilUri = fotoUri,
                                        minutosConcentracion = minutosConcentracion,
                                        arcadeHighScore = arcadeRecord
                                    )
                                )
                                guardadoOk = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Perfil y Redes en Room DB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    if (guardadoOk) {
                        Text("Perfil actualizado permanentemente.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        // Muro de Comentarios para Alumnos, Docentes y Admins
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Muro de Comentarios Académicos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        placeholder = { Text("Escribe un comentario o reconocimiento...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (nuevoComentario.isNotBlank()) {
                                val textoLimpio = nuevoComentario.trim()
                                nuevoComentario = ""
                                scope.launch {
                                    val fecha = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                    db.extraDao().agregarComentario(
                                        com.tecsup.tecsupplus.data.local.ComentarioMuroEntity(
                                            perfilObjetivoUsername = user.username,
                                            autorNombre = user.nombreCompleto,
                                            autorRol = user.rol ?: "USUARIO",
                                            texto = textoLimpio,
                                            fecha = fecha
                                        )
                                    )
                                    comentarios = db.extraDao().obtenerComentariosPerfil(user.username)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publicar Comentario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    comentarios.forEach { com ->
                        Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${com.autorNombre} (${com.autorRol}) • ${com.fecha}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0277BD))
                                Text(com.texto, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}