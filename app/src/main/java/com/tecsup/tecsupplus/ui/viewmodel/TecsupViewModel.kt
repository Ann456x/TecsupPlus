package com.tecsup.tecsupplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.tecsupplus.data.local.entity.CursoEntity
import com.tecsup.tecsupplus.data.repository.TecsupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TecsupViewModel(private val repository: TecsupRepository) : ViewModel() {

    val cursos: StateFlow<List<CursoEntity>> = repository.cursos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarCurso(nombre: String, docente: String, creditos: Int) {
        viewModelScope.launch {
            repository.insertarCurso(CursoEntity(nombre = nombre, docente = docente, creditos = creditos))
        }
    }
}
