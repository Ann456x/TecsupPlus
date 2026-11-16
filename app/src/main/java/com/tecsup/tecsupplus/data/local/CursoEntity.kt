package com.tecsup.tecsupplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "curso_table")
data class CursoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nombre: String,
    val docente: String,
    val creditos: Int
)
