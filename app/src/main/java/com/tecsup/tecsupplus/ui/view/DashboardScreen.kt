package com.tecsup.tecsupplus.ui.view

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
        CursoInscritoModel("Desarrollo de Videojuegos Multijugador", "V24-55018", "Sección: V24-4-B", "5 Créditos", "Videojuegos", "Ing. Rodrigo Andrés Silva", "https://wa.me/51949263864?text=Grupo_Multiplayer", "04:20 pm", "Miércoles", "Lab Redes Gaming"),
        CursoInscritoModel("Arte Conceptual y Texturizado PBR", "V24-55019", "Sección: V24-4-B", "4 Créditos", "Videojuegos", "Ing. Melissa Alejandra Flores", "https://wa.me/51949263864?text=Grupo_ArtePBR", "09:00 am", "Sábado", "Lab Diseño Digital")
    )
}

fun obtenerAlumnosPorCurso(codigoCurso: String, carrera: String): List<String> {
    return when (codigoCurso) {
        // SOFTWARE: cada curso tiene su propio grupo de alumnos matriculados
        "C92317" -> listOf("Aníbal Cuper Apaza", "Gianfranco Vargas", "Jorge Quispe")
        "C66527" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani", "Chrona Ruiz")
        "C66538" -> listOf("Jhosep Sebastian Mamani", "Gianfranco Vargas", "Daniela Condori")
        "C34327" -> listOf("Aníbal Cuper Apaza", "Luis Fernando Mamani", "Valeria Paredes")
        "C66541" -> listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani", "Jorge Quispe", "Chrona Ruiz")
        "C84475" -> listOf("Jhosep Sebastian Mamani", "Andrés Felipe Huamán", "Sofía Ramos")
        "C84474" -> listOf("Aníbal Cuper Apaza", "Gianfranco Vargas", "Sofía Ramos")
        "C66550" -> listOf("Jhosep Sebastian Mamani", "Chrona Ruiz", "Andrés Felipe Huamán")
        "C66552" -> listOf("Aníbal Cuper Apaza", "Daniela Condori", "Luis Fernando Mamani")

        // MINAS: cada curso tiene su propio grupo de alumnos matriculados
        "M24-11021" -> listOf("Franco Alessandro Zúñiga", "Diego Armando Condori", "Brayan Mamani")
        "M24-11022" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia", "Valeria Choque")
        "M24-11023" -> listOf("Luciana Belén Valdivia", "Diego Armando Condori", "Kevin Ticona")
        "M24-11024" -> listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia", "Brayan Mamani")
        "M24-11025" -> listOf("Luciana Belén Valdivia", "Valeria Choque", "Kevin Ticona")
        "M24-11026" -> listOf("Franco Alessandro Zúñiga", "Diego Armando Condori", "Paola Quispe")
        "M24-11027" -> listOf("Luciana Belén Valdivia", "Brayan Mamani", "Paola Quispe")
        "M24-11028" -> listOf("Franco Alessandro Zúñiga", "Valeria Choque", "Kevin Ticona")
        "M24-11029" -> listOf("Luciana Belén Valdivia", "Diego Armando Condori", "Brayan Mamani")

        // VIDEOJUEGOS: cada curso tiene su propio grupo de alumnos matriculados
        "V24-55011" -> listOf("Matías Alejandro Benavides", "Camila Torres", "Renato Paredes")
        "V24-55012" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor", "Sebastián Huanca")
        "V24-55013" -> listOf("Ariana Nicole Sotomayor", "Camila Torres", "Gabriel Mendoza", "Lucía Salazar")
        "V24-55014" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor", "Renato Paredes")
        "V24-55015" -> listOf("Ariana Nicole Sotomayor", "Sebastián Huanca", "Gabriel Mendoza")
        "V24-55016" -> listOf("Matías Alejandro Benavides", "Camila Torres", "Lucía Salazar")
        "V24-55017" -> listOf("Ariana Nicole Sotomayor", "Renato Paredes", "Gabriel Mendoza")
        "V24-55018" -> listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor", "Sebastián Huanca", "Lucía Salazar")
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
    var cursoExpandido by remember { mutableStateOf<String?>(null) }
    var semanaSeleccionada by remember { mutableStateOf("Semana 6") }
    val listaAsistencias = remember(user) { obtenerAsistenciasMemoria(user) }

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
                                        Text("  * Pendiente de guardar", fontSize = 9.sp, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
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
                                        alumnosAMostrar.forEach { alumnoReg ->
                                            val clave = "${item.codigo}|$semanaSeleccionada|${alumnoReg.nombreAlumno}"
                                            val nuevoVal = mapaRestante.remove(clave)
                                            if (nuevoVal != null) {
                                                alumnoReg.estado = nuevoVal
                                            }
                                        }
                                        borradorAsistencias = mapaRestante
                                        mensajeGuardadoCurso = "${item.codigo}_$semanaSeleccionada"
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
                                    text = "Asistencias de $semanaSeleccionada guardadas correctamente.",
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
    val calificacionesCursos = remember(user) { obtenerCalificacionesMemoria(user) }
    val esDocenteOAdmin = esProfesorAdmin(user) || esProfesorNormal(user)

    var borradorNotas by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var alumnoGuardadoMensaje by remember { mutableStateOf<String?>(null) }

    val subtituloNotas = when {
        esAdminGeneral(user) -> "Vista Administrador General: Visualiza y edita calificaciones de todas las carreras."
        esProfesorAdmin(user) -> "Vista Profesor Administrador (${user.carrera}): Visualiza todos los cursos y alumnos de tu carrera."
        esProfesorNormal(user) -> "Vista Docente: Edita las notas de tus alumnos matriculados (0-20) y presiona 'Guardar Notas'."
        else -> "Tus calificaciones obtenidas por semana y promedio final."
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Calificaciones Detalladas por Estudiante", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
            Text(subtituloNotas, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
        }
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
                                            Text("Notas editadas sin guardar", fontSize = 9.sp, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
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
                                            text = "Notas de ${alumno.nombreAlumno} guardadas correctamente.",
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
    var minutosRestantes by remember { mutableStateOf(14) }
    var segundosRestantes by remember { mutableStateOf(30) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (segundosRestantes > 0) segundosRestantes-- else { minutosRestantes--; segundosRestantes = 59 }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF0277BD))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("VISTA CENITAL - CAMPUS TECSUP AREQUIPA", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Próxima clase (${user.carrera ?: "Software"})", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Ubicación exacta:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text("Laboratorio de Mecánica de Rocas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Inicia en:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text(text = String.format("%02d:%02d min", minutosRestantes, segundosRestantes), color = Color(0xFFFFEB3B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(360.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEF))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        drawRect(color = Color(0xFFD7E2E9), size = size)
                        drawRoundRect(color = Color(0xFFCFD8DC), topLeft = Offset(w * 0.08f, h * 0.15f), size = Size(w * 0.35f, h * 0.50f), cornerRadius = CornerRadius(12f, 12f))
                        drawRoundRect(color = Color(0xFFC8E6C9), topLeft = Offset(w * 0.47f, h * 0.20f), size = Size(w * 0.18f, h * 0.40f), cornerRadius = CornerRadius(24f, 24f))
                        drawRoundRect(color = Color(0xFFFFCC80), topLeft = Offset(w * 0.70f, h * 0.18f), size = Size(w * 0.22f, h * 0.25f), cornerRadius = CornerRadius(8f, 8f))
                        drawRoundRect(color = Color(0xFF90CAF9), topLeft = Offset(w * 0.70f, h * 0.52f), size = Size(w * 0.22f, h * 0.28f), cornerRadius = CornerRadius(8f, 8f))

                        val origen = Offset(w * 0.56f, h * 0.40f)
                        val destino = Offset(w * 0.78f, h * 0.30f)

                        drawLine(
                            color = Color(0xFF0277BD),
                            start = origen,
                            end = destino,
                            strokeWidth = 8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                        )

                        drawCircle(color = Color.White, radius = 16f, center = origen)
                        drawCircle(color = Color(0xFF0288D1), radius = 11f, center = origen)
                        drawCircle(color = Color.White, radius = 18f, center = destino)
                        drawCircle(color = Color(0xFFD32F2F), radius = 12f, center = destino)
                    }

                    Text("Pabellón Tecnológico", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F), modifier = Modifier.padding(start = 24.dp, top = 20.dp))
                    Text("Patio Central", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp))
                    Text("Lab. Mecánica de Rocas", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 22.dp))
                    Text("FABLAB Tecsup", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 80.dp))

                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF0288D1), CircleShape))
                            Text(" Tú (Patio)   ", fontSize = 10.sp, color = Color.DarkGray)
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
                            Text(" Destino (Lab. Mecánica de Rocas)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
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
    var pregunta by remember { mutableStateOf("") }
    var respuesta by remember { mutableStateOf("Hola ${user.nombreCompleto}, soy tu asistente Gemini oficial de TecsupPlus. ¿En qué puedo ayudarte con tus cursos hoy?") }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        Text("Asistente Académico Inteligente (Gemini API Real)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0277BD))
                } else {
                    SelectionContainer {
                        Text(text = respuesta, fontSize = 14.sp, color = Color(0xFF01579B), lineHeight = 20.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pregunta, 
                onValueChange = { pregunta = it }, 
                placeholder = { Text("Escribe tu consulta para Gemini...") }, 
                modifier = Modifier.weight(1f), 
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (pregunta.isNotBlank()) {
                        val promptUsuario = pregunta
                        pregunta = ""
                        cargando = true
                        scope.launch {
                            try {
                                val generativeModel = GenerativeModel(
                                    modelName = "gemini-1.5-flash",
                                    apiKey = BuildConfig.TECSUP_API_KEY
                                )
                                val resultado = withContext(Dispatchers.IO) {
                                    generativeModel.generateContent(promptUsuario)
                                }
                                respuesta = resultado.text ?: "No se obtuvo respuesta del modelo."
                            } catch (e: Exception) {
                                respuesta = "Error al conectar con la API de Gemini: ${e.localizedMessage}"
                            } finally {
                                cargando = false
                            }
                        }
                    }
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)), 
                modifier = Modifier.height(56.dp)
            ) {
                Text("Enviar", color = Color.White)
            }
        }
    }
}

@Composable
fun PerfilYAwardView(user: UsuarioEntity) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Credencial Digital y Rol", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Nombre Completo: ${user.nombreCompleto}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Usuario institucional: ${user.username}", fontSize = 13.sp)
                    Text(text = "Rol en Sistema: ${user.rol}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                    Text(text = "Carrera Asignada: ${user.carrera ?: "Todas (Admin)"}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text(text = "Acceso a Campus Arequipa: HABILITADO", modifier = Modifier.padding(8.dp), fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Ayuda y Soporte Institucional", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0277BD))
                    Text("Números de contacto rápido para trámites y asistencia en Arequipa.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Secretaría Académica:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002B49))
                            Text("+51 (054) 432190", fontSize = 13.sp, color = Color(0xFF0277BD))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Admisión e Inscripciones:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002B49))
                            Text("+51 949 263 864", fontSize = 13.sp, color = Color(0xFF0277BD))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Profesor Tutor / Profe Admin:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002B49))
                            Text("Patricia Llerena (Anexo 402)", fontSize = 13.sp, color = Color(0xFF0277BD))
                        }
                    }
                }
            }
        }
    }
}