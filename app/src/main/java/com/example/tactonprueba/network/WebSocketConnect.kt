package com.example.tactonprueba.network

import android.util.Log
import com.google.gson.Gson
import okhttp3.*

class WebSocketClient(
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
                Log.d("WebSocket", "✅ Conectado a $serverIp:$port")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val msg = gson.fromJson(text, PositionMessage::class.java)
                    onMessageReceived(text)
                } catch (e: Exception) {
                    Log.e("WebSocket", "❌ Error parseando JSON: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "⚠️ Error: ${t.message}")
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

