package com.example.riego

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// Paleta de colores en azul
val BluePrimary = Color(0xFF1565C0)
val BlueSecondary = Color(0xFF1E88E5)
val BlueBackground = Color(0xFFE3F2FD)
val DarkBlueText = Color(0xFF0D47A1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dbHelper = remember { DatabaseHelper(this) }
            RiegoAppBlueTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(dbHelper = dbHelper, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RiegoAppBlueTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = BluePrimary,
        secondary = BlueSecondary,
        background = BlueBackground,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = DarkBlueText
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

enum class Screen { LOGIN, REGISTER, DASHBOARD }

@Composable
fun MainNavigation(dbHelper: DatabaseHelper, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var userEmail by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueBackground)
    ) {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen(
                dbHelper = dbHelper,
                onLoginSuccess = { email ->
                    userEmail = email
                    currentScreen = Screen.DASHBOARD
                },
                onNavigateToRegister = { currentScreen = Screen.REGISTER }
            )
            Screen.REGISTER -> RegisterScreen(
                dbHelper = dbHelper,
                onRegisterSuccess = { currentScreen = Screen.LOGIN },
                onNavigateToLogin = { currentScreen = Screen.LOGIN }
            )
            Screen.DASHBOARD -> DashboardScreen(
                userEmail = userEmail,
                onLogout = {
                    userEmail = ""
                    currentScreen = Screen.LOGIN
                }
            )
        }
    }
}

@Composable
fun LoginScreen(
    dbHelper: DatabaseHelper,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sistema de Riego IoT", style = MaterialTheme.typography.headlineMedium, color = DarkBlueText)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo o Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    val isValid = dbHelper.validarUsuario(email, password)
                    if (isValid) {
                        Toast.makeText(context, "Bienvenido/a", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(email)
                    } else {
                        Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate aquí", color = BluePrimary)
        }
    }
}

@Composable
fun RegisterScreen(
    dbHelper: DatabaseHelper,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Nueva Cuenta", style = MaterialTheme.typography.headlineMedium, color = DarkBlueText)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (password == confirmPassword) {
                        val exito = dbHelper.registrarUsuario(email, password)
                        if (exito) {
                            Toast.makeText(context, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y Registrar", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Volver al Inicio de Sesión", color = BluePrimary)
        }
    }
}

@Composable
fun DashboardScreen(userEmail: String, onLogout: () -> Unit) {
    var isValveOpen by remember { mutableStateOf(false) }
    var humidityLevel by remember { mutableStateOf(50) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Panel de Riego IoT", style = MaterialTheme.typography.headlineSmall, color = DarkBlueText)
        Text("Usuario activo: $userEmail", style = MaterialTheme.typography.bodyMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Lectura Sensor de Humedad:", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$humidityLevel %",
                    style = MaterialTheme.typography.displaySmall,
                    color = BluePrimary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Control del Sistema", style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isValveOpen) "Válvula: ABIERTA" else "Válvula: CERRADA")
                    Switch(
                        checked = isValveOpen,
                        onCheckedChange = { isValveOpen = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                    )
                }
            }
        }

        Button(
            onClick = { humidityLevel = (20..85).random() },
            colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary)
        ) {
            Text("Actualizar Sensor", color = Color.White)
        }

        OutlinedButton(onClick = onLogout) {
            Text("Cerrar Sesión", color = BluePrimary)
        }
    }
}