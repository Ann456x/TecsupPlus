package com.tecsup.tecsupplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencias")
data class AsistenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val cursoId: Long,
    val fecha: String,
    val estado: String // \"PRESENTE\", \"TARDANZA\", \"FALTA\"
)
