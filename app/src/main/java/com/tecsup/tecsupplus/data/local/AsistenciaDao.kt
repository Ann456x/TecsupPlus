package com.tecsup.tecsupplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {
    @Query("SELECT * FROM asistencia_table WHERE carrera = :carrera")
    fun getAsistenciaPorCarrera(carrera: String): Flow<List<AsistenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAsistenciaONota(asistencia: AsistenciaEntity)
}
