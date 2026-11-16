package com.tecsup.tecsupplus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(entities = [CursoEntity::class, AsistenciaEntity::class, UsuarioEntity::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cursoDao(): CursoDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val seedMutex = Mutex()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tecsup_plus_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { asegurarDatosIniciales(it) }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { asegurarDatosIniciales(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun asegurarDatosIniciales(database: AppDatabase) {
            seedMutex.withLock {
                val userDao = database.usuarioDao()
                if (userDao.contarUsuarios() > 0) return

                val cursoDao = database.cursoDao()
                val listaUsuarios = mutableListOf<UsuarioEntity>()

                val nombresAdmin = listOf(
                    "Mg. Carmen Rosa Quispe",
                    "Lic. Roberto Carlos Sánchez",
                    "Dra. Ana María Torres",
                    "Ing. Javier Alejandro Pérez",
                    "Lic. Patricia Elena Gómez"
                )
                for (i in 1..5) {
                    listaUsuarios.add(
                        UsuarioEntity(
                            username = "admin$i",
                            password = "123",
                            nombreCompleto = nombresAdmin[i - 1],
                            rol = "ADMIN",
                            carrera = null
                        )
                    )
                }

                val carrerasDatos = mapOf(
                    "MINAS" to Triple(
                        "Ing. Renzo Huamán Ticona",
                        listOf(
                            "Ing. Luis Alberto Mendoza", "Ing. Mario Fernando Vargas", "Ing. Sofía Lucía Rojas",
                            "Ing. David Ernesto Cáceres", "Ing. Claudia Milagros Medina", "Ing. Jorge Luis Paredes",
                            "Ing. Victor Manuel Silva", "Ing. Rosa Elena Morales", "Ing. Carlos Alberto Castro"
                        ),
                        listOf("Franco Alessandro Zúñiga", "Luciana Belén Valdivia")
                    ),
                    "SOFTWARE" to Triple(
                        "Ing. Gianfranco Espinoza Díaz",
                        listOf(
                            "Ing. Ricardo Alonso Pimentel", "Ing. Valeria Nicole Huertas", "Ing. Gabriel Esteban Navarro",
                            "Ing. Andrea Jimena Salazar", "Ing. Marco Antonio Becerra", "Ing. Daniel Alejandro León",
                            "Ing. Karina Lizeth Ramos", "Ing. Eduardo José Pastor", "Ing. Tatiana Marisol Cárdenas"
                        ),
                        listOf("Aníbal Cuper Apaza", "Jhosep Sebastian Mamani")
                    ),
                    "VIDEOJUEGOS" to Triple(
                        "Ing. Christian Alonso Orosco",
                        listOf(
                            "Ing. Bruno Alonso Morales", "Ing. Camila Alejandra Vega", "Ing. Diego Alonso Torres",
                            "Ing. Fiorella Nicole Meléndez", "Ing. Alonso Javier Paredes", "Ing. Sebastián Enrique Ramos",
                            "Ing. Lucía Fernanda Castillo", "Ing. Rodrigo Andrés Silva", "Ing. Melissa Alejandra Flores"
                        ),
                        listOf("Matías Alejandro Benavides", "Ariana Nicole Sotomayor")
                    )
                )

                val listaCursos = listOf(
                    CursoEntity(nombre = "Cálculo Aplicado", docente = "Ing. Ricardo Alonso Pimentel", creditos = 4),
                    CursoEntity(nombre = "Desarrollo de Bases de Datos", docente = "Ing. Valeria Nicole Huertas", creditos = 5),
                    CursoEntity(nombre = "Arquitectura de Software", docente = "Ing. Gabriel Esteban Navarro", creditos = 4),
                    CursoEntity(nombre = "Aplicaciones Móviles con Kotlin", docente = "Ing. Gianfranco Espinoza Díaz", creditos = 5),
                    CursoEntity(nombre = "Innovación y Emprendimiento", docente = "Ing. Andrea Jimena Salazar", creditos = 3)
                )
                for (curso in listaCursos) {
                    cursoDao.insertarCurso(curso)
                }

                for ((carreraKey, triple) in carrerasDatos) {
                    val (nombreProfAdmin, profs, ests) = triple
                    val carreraLower = carreraKey.lowercase()

                    listaUsuarios.add(
                        UsuarioEntity(
                            username = "prof_admin_$carreraLower",
                            password = "123",
                            nombreCompleto = nombreProfAdmin,
                            rol = "PROFESOR_ADMIN",
                            carrera = carreraKey
                        )
                    )

                    for (i in profs.indices) {
                        listaUsuarios.add(
                            UsuarioEntity(
                                username = "prof_${carreraLower}_${i + 1}",
                                password = "123",
                                nombreCompleto = profs[i],
                                rol = "PROFESOR_REGULAR",
                                carrera = carreraKey
                            )
                        )
                    }

                    for (i in ests.indices) {
                        listaUsuarios.add(
                            UsuarioEntity(
                                username = "est_${carreraLower}_${i + 1}",
                                password = "123",
                                nombreCompleto = ests[i],
                                rol = "ESTUDIANTE",
                                carrera = carreraKey
                            )
                        )
                    }
                }

                userDao.insertarUsuarios(listaUsuarios)
            }
        }
    }
}