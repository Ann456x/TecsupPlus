package com.tecsup.tecsupplus.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.tecsupplus.data.local.AppDatabase
import com.tecsup.tecsupplus.data.local.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLoginSuccess: (UsuarioEntity) -> Unit,
    onLoginError: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tecsupBlue = Color(0xFF0277BD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tecsupBlue)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tecsup",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TECNOLOGÍA CON SENTIDO",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Portal de Estudiantes",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Usuario", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Ocultar" else "Ver", color = Color.White, fontSize = 12.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(300.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = Color(0xFFFF6B6B), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Ingrese usuario y contraseña"
                        return@Button
                    }
                    scope.launch {
                        val db = AppDatabase.getDatabase(context)
                        val user: UsuarioEntity? = withContext(Dispatchers.IO) {
                            db.usuarioDao().obtenerUsuarioPorCredenciales(username.trim(), password.trim())
                        }
                        if (user != null) {
                            onLoginSuccess(user)
                        } else {
                            errorMessage = "Credenciales incorrectas"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.width(300.dp).height(48.dp)
            ) {
                Text(text = "Iniciar sesión", color = tecsupBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "¿Olvidaste tu contraseña?", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }
    }
}