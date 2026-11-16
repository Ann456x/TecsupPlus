package com.tecsup.tecsupplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencia_table")
data class AsistenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val carrera: String,
    val alumnoNombre: String,
    val asistencia: String,
    val nota: Float
)
