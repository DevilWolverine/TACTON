import android.graphics.Point
import android.util.Log
import com.example.tactonprueba.ui.theme.PositionMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okio.ByteString

class WebSocketClient(private val onPositionReceived: (PositionMessage) -> Unit){

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun connect(serverIp: String = "192.168.1.39", port: Int = 8080) {
        val request = Request.Builder()
            .url("ws://$serverIp:$port")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Conectado al servidor WebSocket")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "📩 Mensaje recibido: $text")

                try {
                    val json = gson.fromJson(text, JsonObject::class.java)

                    val msg = gson.fromJson(text, PositionMessage::class.java)
                    if (msg.type == "position") {
                        onPositionReceived(msg) // 👈 ahora sí es correcto
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "❌ Error parseando JSON: ${e.message}")
                }
            }
                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e("WebSocket", "❌ Error: ${t.message}")
                }

        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Cliente cerrando")
    }
}
