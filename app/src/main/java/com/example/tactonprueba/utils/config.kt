package com.example.tactonprueba.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.tactonprueba.network.WebSocketHolder.wsClient
import kotlinx.coroutines.delay

@Composable
fun AjustesUsuario (){}

@Composable
fun ConfiguracionGeneral (){}


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