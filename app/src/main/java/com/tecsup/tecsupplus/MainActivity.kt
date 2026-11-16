package com.tecsup.tecsupplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import com.tecsup.tecsupplus.ui.view.DashboardScreen
import com.tecsup.tecsupplus.ui.view.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar Room Database
        AppDatabase.getDatabase(applicationContext)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                var loggedUser by remember { mutableStateOf<UsuarioEntity?>(null) }

                if (loggedUser == null) {
                    LoginScreen(
                        onLoginSuccess = { user ->
                            loggedUser = user
                        },
                        onLoginError = {}
                    )
                } else {
                    DashboardScreen(user = loggedUser!!) {
                        loggedUser = null // Cerrar sesión
                    }
                }
            }
        }
    }
}
