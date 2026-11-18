package com.tecsup.tecsupplus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.CursoEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TecsupViewModel(application: Application) : AndroidViewModel(application) {
    private val cursoDao = AppDatabase.getDatabase(application).cursoDao()

    val cursos: StateFlow<List<CursoEntity>> = cursoDao.obtenerCursos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarCurso(nombre: String, docente: String, creditos: Int) {
        viewModelScope.launch {
            cursoDao.insertarCurso(CursoEntity(nombre = nombre, docente = docente, creditos = creditos))
        }
    }
}