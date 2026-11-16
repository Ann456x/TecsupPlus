package com.tecsup.tecsupplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun login(user: String, pass: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun obtenerUsuarioPorCredenciales(user: String, pass: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodosUsuarios(): List<UsuarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}