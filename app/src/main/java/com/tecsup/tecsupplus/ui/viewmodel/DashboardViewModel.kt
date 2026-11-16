package com.tecsup.tecsupplus.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class AlumnoItem(
    val id: String,
    val nombre: String,
    val carrera: String,
    val cursoAsignado: String,
    var presente: Boolean,
    var nota: Double
)

class DashboardViewModel : ViewModel() {

    // Estado global de cambios sin guardar para Asistencias y Notas
    var hasUnsavedChanges by mutableStateOf(false)
        private set

    // Lista de alumnos filtrados estrictamente según el rol, carrera y curso del docente
    var alumnosFiltradosList by mutableStateOf<List<AlumnoItem>>(emptyList())
        private set

    /**
     * REGLA DE NEGOCIO ESTRICTA:
     * - Profesor Regular: Solo ve y modifica alumnos de SU carrera Y de SU curso asignado.
     * - Profesor Administrador: Ve notas y asistencias de TODOS los cursos de SU carrera correspondiente.
     */
    fun cargarDatosSegunRolDocente(
        carreraDocente: String,
        cursoAsignado: String,
        esProfesorAdmin: Boolean
    ) {
        val baseDatosInstitucional = listOf(
            AlumnoItem("1", "Aníbal Cuper Apaza", "Diseño y Desarrollo de Software", "Desarrollo de Aplicaciones Móviles (Android/Kotlin)", true, 18.0),
            AlumnoItem("2", "Jhosep Sebastian Mamani", "Diseño y Desarrollo de Software", "Desarrollo de Aplicaciones Móviles (Android/Kotlin)", true, 16.0),
            AlumnoItem("3", "Ricardo Alonso Pimentel", "Diseño y Desarrollo de Software", "Programación Orientada a Objetos", true, 15.0),
            AlumnoItem("4", "Franco Alessandro Zúñiga", "Ingeniería de Minas", "Geología General", true, 15.0),
            AlumnoItem("5", "Luciana Belén Valdivia", "Ingeniería de Minas", "Geología General", false, 12.0),
            AlumnoItem("6", "Luis Alberto Mendoza", "Ingeniería de Minas", "Topografía Subterránea", true, 14.0),
            AlumnoItem("7", "Matías Alejandro Benavides", "Creación de Videojuegos", "Diseño de Niveles y Gameplay 3D", true, 17.0),
            AlumnoItem("8", "Ariana Nicole Sotomayor", "Creación de Videojuegos", "Diseño de Niveles y Gameplay 3D", true, 19.0),
            AlumnoItem("9", "Bruno Alonso Morales", "Creación de Videojuegos", "Programación de Motores de Videojuegos (Unity/Unreal)", true, 16.5)
        )

        alumnosFiltradosList = if (esProfesorAdmin) {
            baseDatosInstitucional.filter { it.carrera.equals(carreraDocente, ignoreCase = true) }
        } else {
            baseDatosInstitucional.filter {
                it.carrera.equals(carreraDocente, ignoreCase = true) &&
                it.cursoAsignado.equals(cursoAsignado, ignoreCase = true)
            }
        }
        
        hasUnsavedChanges = false
    }

    fun actualizarAsistencia(alumnoId: String, nuevoEstado: Boolean) {
        alumnosFiltradosList = alumnosFiltradosList.map {
            if (it.id == alumnoId) it.copy(presente = nuevoEstado) else it
        }
        hasUnsavedChanges = true
    }

    fun actualizarNota(alumnoId: String, nuevaNota: Double) {
        alumnosFiltradosList = alumnosFiltradosList.map {
            if (it.id == alumnoId) it.copy(nota = nuevaNota) else it
        }
        hasUnsavedChanges = true
    }

    fun guardarCambiosPermanentes() {
        // Persistencia definitiva en Base de Datos Local o Servidor
        hasUnsavedChanges = false
    }
}
