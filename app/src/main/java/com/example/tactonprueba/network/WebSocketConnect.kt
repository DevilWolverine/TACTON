
import android.util.Log
import com.google.gson.Gson
import com.mapbox.geojson.Point
import okhttp3.*

data class PositionMessage(
    val type: String,
    val user: String,
    val point: Point,
    val bearing: Double? = null
)

class WebSocketClient(
    private val onMessageReceived: (PositionMessage) -> Unit
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
                    Log.d("WebSocket", "📩 Mensaje bruto: $text")
                    val msg = gson.fromJson(text, PositionMessage::class.java)
                    onMessageReceived(msg)
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

