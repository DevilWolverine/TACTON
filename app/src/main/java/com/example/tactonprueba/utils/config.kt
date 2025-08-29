package com.example.tactonprueba.utils

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import com.example.tactonprueba.network.*
import com.example.tactonprueba.network.WebSocketHolder.wsClient


@Composable
fun UserConfigurationScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userData by UserPreferences.getUserData(context).collectAsState(initial = emptyMap())

    var indicativo by remember { mutableStateOf(userData["indicativo"] ?: "") }
    var cia by remember { mutableStateOf(userData["cia"] ?: "") }
    var scc by remember { mutableStateOf(userData["scc"] ?: "") }
    var pn by remember { mutableStateOf(userData["pn"] ?: "") }
    var empleo by remember { mutableStateOf(userData["empleo"] ?: "Soldado") }

    val empleos = listOf(
        "Soldado", "Cabo", "Cabo 1º", "Cabo Mayor",
        "Sargento", "Sargento 1º", "Brigada", "Subteniente", "Suboficial Mayor",
        "Alférez", "Teniente", "Capitán", "Comandante", "Teniente Coronel", "Coronel"
    )

    // Scroll general
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    var isEditable by remember { mutableStateOf(false) }

    LaunchedEffect(userData) {
        indicativo = userData["indicativo"] ?: ""
        cia = userData["cia"] ?: ""
        scc = userData["scc"] ?: ""
        pn = userData["pn"] ?: ""
        empleo = userData["empleo"] ?: "Soldado"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🔹 Título + botón de bloqueo
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Configuración de Usuario",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(
                onClick = { isEditable = !isEditable }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = if (isEditable) Color.Red else Color.Black
                )
            }

        }

        // Campo helper
        @Composable
        fun InputRow(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label, modifier = Modifier.width(100.dp), color = Color.Black)

                if (enabled) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(value, fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            InputRow("Indicativo", indicativo, { indicativo = it }, enabled = isEditable)
            InputRow("Cía", cia, { cia = it }, enabled = isEditable)
            InputRow("Scc", scc, { scc = it }, enabled = isEditable)
            InputRow("Pn", pn, { pn = it }, enabled = isEditable)

            // Dropdown empleo estilo filtro de marcadores
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Empleo", modifier = Modifier.width(100.dp), color = Color.Black)

                Box(Modifier.weight(1f)) {
                    Button(
                        onClick = { if (isEditable) expanded = true },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(empleo, fontSize = 14.sp)
                            Text("▾", fontSize = 18.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                    ) {
                        empleos.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 14.sp) },
                                onClick = {
                                    empleo = option
                                    expanded = false
                                },
                                modifier = Modifier.height(36.dp) // 👈 más pequeño
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        UserPreferences.saveUserData(context, indicativo, cia, scc, pn, empleo)
                        isEditable = false // se bloquea al guardar
                        Toast.makeText(
                            context,
                            "Ajustes guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
fun ServerConfigurationScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado de la IP introducida
    var serverIp by remember { mutableStateOf("") }

    // Estado de usuarios conectados (según WebSocketClient → remoteUsers)
    val connectedUsers = remember { mutableStateListOf<String>() }

    // Estado de conexión (verde/rojo)
    val isConnected = WebSocketHolder.isConnected

    // Recoger el indicativo del usuario (guardado en UserPreferences)
    val userData by UserPreferences.getUserData(context).collectAsState(initial = emptyMap())
    val username = userData["indicativo"] ?: "Anon"

    // Scroll general por si se llena de usuarios
    val scrollState = rememberScrollState()

    LaunchedEffect(userData) {
       serverIp = userData["servidor"] ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Configuración del Servidor",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Caja para la IP
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Servidor", modifier = Modifier.width(100.dp), color = Color.Black)
            BasicTextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        // Botones conectar / desconectar
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    scope.launch {
                        if (serverIp.isNotBlank()) {
                            UserPreferences.saveUserData(
                                context,
                                userData["indicativo"] ?: "Anon",
                                userData["cia"] ?: "",
                                userData["scc"] ?: "",
                                userData["pn"] ?: "",
                                userData["empleo"] ?: "Soldado",
                                serverIp
                            )

                            // 👇 dispara el trigger para que MapScreen reconecte
                            WebSocketHolder.wsClient?.connect(serverIp,8080,username)

                            Toast.makeText(context, "Conectando a $serverIp...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Introduce una IP válida", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Conectar")
            }

            Button(
                onClick = {
                    scope.launch {
                        //WebSocketHolder.shouldReconnect.value = false
                        WebSocketHolder.wsClient?.close()
                        Toast.makeText(context, "Desconectado del servidor", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
            ) {
                Text("Desconectar")
            }
        }

        // Estado de conexión
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isConnected.value) Color.Green else Color.Red, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isConnected.value) "Conectado" else "Desconectado",
                color = Color.Black
            )
        }

        HorizontalDivider()

        // Lista de usuarios conectados
        Text("Usuarios conectados:", fontWeight = FontWeight.Bold, color = Color.Black)
        if (connectedUsers.isEmpty()) {
            Text("Ninguno", color = Color.Gray)
        } else {
            connectedUsers.forEach { user ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(user, color = Color.Black)
                }
            }
        }

        // 🔹 Actualizar lista cuando cambien los usuarios
        LaunchedEffect(WebSocketHolder.wsClient, isConnected.value) {
            WebSocketHolder.wsClient?.let { ws ->
                connectedUsers.clear()
                connectedUsers.addAll(ws.getConnectedUsers())
            }
        }
    }
}




@Composable
fun DoubleBackToExitApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    var backPressedOnce by remember { mutableStateOf(false) }

    // Escucha del botón atrás
    BackHandler {
        if (backPressedOnce) {
            wsClient?.close()
            activity?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Repita la acción para salir", Toast.LENGTH_SHORT).show()
        }
    }

    // Resetear el estado tras 2 segundos
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }
}