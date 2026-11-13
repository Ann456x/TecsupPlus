package com.tecsup.tecsupplus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tecsup.tecsupplus.data.local.dao.AsistenciaDao
import com.tecsup.tecsupplus.data.local.dao.CursoDao
import com.tecsup.tecsupplus.data.local.entity.AsistenciaEntity
import com.tecsup.tecsupplus.data.local.entity.CursoEntity

@Database(entities = [CursoEntity::class, AsistenciaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cursoDao(): CursoDao
    abstract fun asistenciaDao(): AsistenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tecsupplus_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
