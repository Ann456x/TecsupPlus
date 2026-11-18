package com.tecsup.tecsupplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencias_persistentes")
data class RegistroAsistenciaPersistenteEntity(
    @PrimaryKey val clave: String,
    val codigoCurso: String,
    val semana: String,
    val nombreAlumno: String,
    val estado: String
)

@Entity(tableName = "notas_persistentes")
data class RegistroNotaPersistenteEntity(
    @PrimaryKey val clave: String,
    val codigoCurso: String,
    val nombreAlumno: String,
    val nota1: Double,
    val nota2: Double,
    val nota3: Double,
    val nota4: Double,
    val nota5: Double
)

@Entity(tableName = "nearpod_quizzes")
data class NearpodQuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val cursoCodigo: String,
    val semana: String,
    val creadorUsername: String,
    val tiempoPorPreguntaSeg: Int,
    val preguntasJson: String
)

@Entity(tableName = "nearpod_resultados")
data class NearpodResultadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: Int,
    val quizTitulo: String,
    val semana: String,
    val estudianteUsername: String,
    val estudianteNombre: String,
    val puntaje: Int,
    val aciertos: Int,
    val totalPreguntas: Int,
    val puntosExtra: Double,
    val stickmanConfig: String,
    val fechaHora: String
)

@Entity(tableName = "curso_enlaces")
data class CursoEnlaceEntity(
    @PrimaryKey val codigoCurso: String,
    val whatsappUrl: String
)

@Entity(tableName = "perfiles_extra")
data class PerfilUsuarioExtraEntity(
    @PrimaryKey val username: String,
    val bio: String = "",
    val githubUrl: String = "",
    val youtubeUrl: String = "",
    val googleUrl: String = "",
    val linkedinUrl: String = "",
    val fotoPerfilUri: String = "",
    val fotosGaleriaJson: String = "[]",
    val minutosConcentracion: Int = 0,
    val vecesTop3: Int = 0,
    val arcadeHighScore: Int = 0
)

@Entity(tableName = "comentarios_muro")
data class ComentarioMuroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val perfilObjetivoUsername: String,
    val autorNombre: String,
    val autorRol: String,
    val texto: String,
    val fecha: String
)