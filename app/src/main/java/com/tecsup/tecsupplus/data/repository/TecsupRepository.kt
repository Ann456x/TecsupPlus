package com.tecsup.tecsupplus.data.repository

import com.tecsup.tecsupplus.data.local.dao.AsistenciaDao
import com.tecsup.tecsupplus.data.local.dao.CursoDao
import com.tecsup.tecsupplus.data.local.entity.AsistenciaEntity
import com.tecsup.tecsupplus.data.local.entity.CursoEntity
import com.tecsup.tecsupplus.data.remote.Content
import com.tecsup.tecsupplus.data.remote.GeminiApiService
import com.tecsup.tecsupplus.data.remote.GeminiRequest
import com.tecsup.tecsupplus.data.remote.Part
import kotlinx.coroutines.flow.Flow

class TecsupRepository(
    private val cursoDao: CursoDao,
    private val asistenciaDao: AsistenciaDao,
    private val geminiApiService: GeminiApiService
) {
    val cursos: Flow<List<CursoEntity>> = cursoDao.obtenerCursos()

    suspend fun insertarCurso(curso: CursoEntity) {
        cursoDao.insertarCurso(curso)
    }

    fun obtenerAsistencias(cursoId: Long): Flow<List<AsistenciaEntity>> {
        return asistenciaDao.obtenerAsistenciasPorCurso(cursoId)
    }

    suspend fun insertarAsistencia(asistencia: AsistenciaEntity) {
        asistenciaDao.insertarAsistencia(asistencia)
    }

    suspend fun consultarGemini(apiKey: String, prompt: String): String {
        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))
        val response = geminiApiService.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
            ?: "No se obtuvo respuesta de la IA"
    }
}
