package com.example.tactonprueba.network

import android.util.Log
import com.google.gson.Gson
import okhttp3.*

class WebSocketConnect(
    private val onMessageReceived: (String) -> Unit
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun connect(serverIp: String, port: Int) {
        val request = Request.Builder()
            .url("ws://$serverIp:$port")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Conectado al Servidor TACTON")
                WebSocketHolder.isConnected.value = true
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    onMessageReceived(text) // 👈 delega al cliente para procesar
                } catch (e: Exception) {
                    Log.e("WebSocket", "❌ Error procesando mensaje: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "⚠️ Error: ${t.message}")
                WebSocketHolder.isConnected.value = false
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "❌ Conexión cerrada")
                WebSocketHolder.isConnected.value = false
            }

        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Cerrado por el usuario")
    }
}
