package com.tecsup.tecsupplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TecsupExtraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAsistencias(lista: List<RegistroAsistenciaPersistenteEntity>)

    @Query("SELECT * FROM asistencias_persistentes")
    suspend fun obtenerTodasAsistencias(): List<RegistroAsistenciaPersistenteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNota(nota: RegistroNotaPersistenteEntity)

    @Query("SELECT * FROM notas_persistentes")
    suspend fun obtenerTodasNotas(): List<RegistroNotaPersistenteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarQuiz(quiz: NearpodQuizEntity): Long

    @Query("SELECT * FROM nearpod_quizzes ORDER BY id DESC")
    suspend fun obtenerQuizzes(): List<NearpodQuizEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarResultadoQuiz(resultado: NearpodResultadoEntity)

    @Query("SELECT * FROM nearpod_resultados WHERE quizId = :quizId ORDER BY puntaje DESC, aciertos DESC")
    suspend fun obtenerRankingPorQuiz(quizId: Int): List<NearpodResultadoEntity>

    @Query("SELECT * FROM nearpod_resultados ORDER BY id DESC")
    suspend fun obtenerTodosResultadosQuiz(): List<NearpodResultadoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEnlaceCurso(enlace: CursoEnlaceEntity)

    @Query("SELECT * FROM curso_enlaces WHERE codigoCurso = :codigo LIMIT 1")
    suspend fun obtenerEnlaceCurso(codigo: String): CursoEnlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfilExtra(perfil: PerfilUsuarioExtraEntity)

    @Query("SELECT * FROM perfiles_extra WHERE username = :username LIMIT 1")
    suspend fun obtenerPerfilExtra(username: String): PerfilUsuarioExtraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarComentario(comentario: ComentarioMuroEntity)

    @Query("SELECT * FROM comentarios_muro WHERE perfilObjetivoUsername = :username ORDER BY id DESC")
    suspend fun obtenerComentariosPerfil(username: String): List<ComentarioMuroEntity>
}