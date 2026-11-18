package com.tecsup.tecsupplus.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.NearpodQuizEntity
import com.tecsup.tecsupplus.data.local.NearpodResultadoEntity
import com.tecsup.tecsupplus.data.local.RegistroAsistenciaPersistenteEntity
import com.tecsup.tecsupplus.data.local.RegistroNotaPersistenteEntity
import com.tecsup.tecsupplus.data.remote.GroqClient

data class PreguntaNearpod(
    val enunciado: String,
    val opciones: List<String>,
    val indiceCorrecto: Int
)

class TecsupRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val extraDao = db.extraDao()
    private val gson = Gson()

    suspend fun guardarAsistencias(registros: List<RegistroAsistenciaPersistenteEntity>) {
        extraDao.guardarAsistencias(registros)
    }

    suspend fun obtenerMapaAsistenciasGuardadas(): Map<String, String> {
        return extraDao.obtenerTodasAsistencias().associate { it.clave to it.estado }
    }

    suspend fun guardarNotaAlumno(registro: RegistroNotaPersistenteEntity) {
        extraDao.guardarNota(registro)
    }

    suspend fun obtenerMapaNotasGuardadas(): Map<String, RegistroNotaPersistenteEntity> {
        return extraDao.obtenerTodasNotas().associateBy { it.clave }
    }

    suspend fun consultarAsistenteIA(
        pregunta: String,
        rolUsuario: String,
        carrera: String?,
        contextoAcademicoReal: String = ""
    ): Result<String> {
        val sistema = """
            Eres el Asistente Académico Inteligente de Tecsup Arequipa (Sede Sur - Socabaya).
            Usuario activo -> Rol: $rolUsuario | Carrera: ${carrera ?: "General"}.
            Nota mínima aprobatoria en Tecsup: 13.0 (sobre 20).
            Datos reales actuales del sistema académico del usuario:
            $contextoAcademicoReal
            Responde en español, con datos precisos basados en la información anterior cuando te pregunten por cursos, alumnos, notas o asistencias.
        """.trimIndent()
        return GroqClient.consultarGroq(sistema, pregunta, 0.5)
    }

    suspend fun generarPreguntasConIA(materialTexto: String): Result<List<PreguntaNearpod>> {
        val sistema = """
            Eres un generador experto de evaluaciones académicas tipo Nearpod/Kahoot para educación superior tecnológica.
            Genera EXACTAMENTE 10 preguntas de opción múltiple basadas en el texto proporcionado.
            Cada pregunta debe tener EXACTAMENTE 4 opciones y un índice correcto (0, 1, 2 o 3).
            Responde ÚNICAMENTE con un JSON válido (sin texto extra ni bloques markdown) con este formato exacto:
            [{"enunciado":"...","opciones":["op1","op2","op3","op4"],"indiceCorrecto":0}]
        """.trimIndent()

        val resp = GroqClient.consultarGroq(sistema, "Genera 10 preguntas con 4 alternativas sobre este tema o documento:\n\n$materialTexto", 0.4)
        return resp.mapCatching { raw ->
            val inicio = raw.indexOf('[')
            val fin = raw.lastIndexOf(']')
            require(inicio >= 0 && fin > inicio) { "La IA no devolvió un arreglo JSON válido." }
            val jsonLimpio = raw.substring(inicio, fin + 1)
            val tipo = object : TypeToken<List<PreguntaNearpod>>() {}.type
            val lista: List<PreguntaNearpod> = gson.fromJson(jsonLimpio, tipo)
            require(lista.isNotEmpty()) { "Lista de preguntas vacía." }
            lista.take(10).map { p ->
                val ops = if (p.opciones.size >= 4) p.opciones.take(4) else p.opciones + List(4 - p.opciones.size) { "Opción ${it + 1}" }
                p.copy(opciones = ops, indiceCorrecto = p.indiceCorrecto.coerceIn(0, 3))
            }
        }
    }

    fun serializarPreguntas(preguntas: List<PreguntaNearpod>): String = gson.toJson(preguntas)

    fun deserializarPreguntas(json: String): List<PreguntaNearpod> {
        return try {
            val tipo = object : TypeToken<List<PreguntaNearpod>>() {}.type
            gson.fromJson(json, tipo) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun publicarQuiz(
        titulo: String,
        cursoCodigo: String,
        semana: String,
        creadorUsername: String,
        tiempoSeg: Int,
        preguntas: List<PreguntaNearpod>
    ): Long {
        return extraDao.guardarQuiz(
            NearpodQuizEntity(
                titulo = titulo,
                cursoCodigo = cursoCodigo,
                semana = semana,
                creadorUsername = creadorUsername,
                tiempoPorPreguntaSeg = tiempoSeg,
                preguntasJson = serializarPreguntas(preguntas)
            )
        )
    }

    suspend fun obtenerQuizzesDisponibles(): List<NearpodQuizEntity> = extraDao.obtenerQuizzes()

    suspend fun guardarResultadoYCalcularPodio(resultado: NearpodResultadoEntity): List<NearpodResultadoEntity> {
        extraDao.guardarResultadoQuiz(resultado)
        val ranking = extraDao.obtenerRankingPorQuiz(resultado.quizId)
        ranking.forEachIndexed { idx, item ->
            val bonus = when (idx) {
                0 -> 2.0
                1 -> 1.5
                2 -> 1.0
                else -> 0.0
            }
            if (item.puntosExtra != bonus) {
                extraDao.guardarResultadoQuiz(item.copy(puntosExtra = bonus))
            }
        }
        return extraDao.obtenerRankingPorQuiz(resultado.quizId)
    }

    suspend fun obtenerRankingPorQuiz(quizId: Int): List<NearpodResultadoEntity> = extraDao.obtenerRankingPorQuiz(quizId)

    suspend fun obtenerTodosLosResultados(): List<NearpodResultadoEntity> = extraDao.obtenerTodosResultadosQuiz()
}