package com.tecsup.tecsupplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val password: String,
    val nombreCompleto: String,
    val rol: String,
    val carrera: String?
)
