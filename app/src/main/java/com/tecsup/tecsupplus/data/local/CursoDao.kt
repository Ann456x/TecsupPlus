package com.tecsup.tecsupplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CursoDao {
    @Query("SELECT * FROM curso_table")
    fun obtenerCursos(): Flow<List<CursoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCurso(curso: CursoEntity)
}
