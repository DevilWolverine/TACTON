package com.example.tactonprueba.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.tactonprueba.utils.MarkerData
import com.example.tactonprueba.utils.MarkerType
import com.example.tactonprueba.utils.MedevacData
import com.example.tactonprueba.utils.TutelaData
import com.example.tactonprueba.utils.placeMarker
import com.example.tactonprueba.utils.removeMarkerByPoint
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import kotlin.math.min

object WebSocketHolder {
    var wsClient: WebSocketClient? = null
    val isConnected = mutableStateOf(false)
    val shouldReconnect = mutableStateOf(false)

}

// Posición de usuario
data class PositionMessage(
    val type: String,
    val user: String,
    val point: Point,
    val bearing: Double? = null
)

// Creación del marcador
data class MarkerMessage(
    val id: Int? = null,
    val type: String = "create",
    val user: String,
    val icon: String? = null,
    val marker: MarkerEdit,
    val medevac: MedevacData? = null,
    val tutela: TutelaData? = null,
)

// Falso marcador
data class MarkerEdit(
    val id: Int,
    val name: String,
    val point: Point,
    val icon: String?,
    val iconRes: Int? = null,
    val createdBy: String,
    val distance: Double?,
    val type: MarkerType,
)

data class DeleteMessage(
    val id: Int,
    val point: Point,
    val type: String = "delete"
)

data class InitStateMessage(
    val type: String = "init_state",
    val users: Conect,
    val markers: List<MarkerMessage>
)

data class Conect(
    val id: Int,
    val user: String,
    val point: Point,
    val bearing: Double
)

// 🌍 Variables globales para toda la app




class WebSocketClient(
    private val remoteUsers: MutableMap<String, Pair<Point, Double>>,
    private val userSourceRef: MutableState<GeoJsonSource?>,
    private val coroutineScope: CoroutineScope,
    private val onMessage: (String) -> Unit = {}
) {
    private val animationJobs = mutableMapOf<String, Job>()
    private var config: WebSocketConnect? = null

    private var onMessageCallback: ((String) -> Unit)? = null

    fun setOnMessage(callback: (String) -> Unit) {
        onMessageCallback = callback
    }

    fun connect(serverIp: String, port: Int, username: String) {
        config = WebSocketConnect { raw ->
            onMessage(raw) // 👈 delega al callback que definiste en MapScreen
        }

        config?.connect(serverIp, port)

        val helloMsg = """{"type":"hello","user":"$username"}"""
        config?.sendMessage(helloMsg)

        WebSocketHolder.isConnected.value = true
        Log.d("WebSocketClient", "👋 Usuario $username identificado en $serverIp:$port")
    }

    /**
     * Enviar un mensaje al servidor.
     */
    fun sendMessage(msg: String) {
        config?.sendMessage(msg)
    }

    /**
     * Cerrar conexión.
     */
    fun close() {
        config?.close()
        WebSocketHolder.isConnected.value = false
    }

    // Manejar mensajes entrantes
    fun handleIncomingRawMessage(
        rawMsg: String,
        pointAnnotationManager: PointAnnotationManager?,
        markerList: SnapshotStateList<MarkerData>,
        selectedMarker: MutableState<PointAnnotation?>,
        medevacList: SnapshotStateList<MedevacData?>,
        tutelaList: SnapshotStateList<TutelaData?>,
        medevacIcon: Bitmap?,
        warningIcon: Bitmap?,
        tutelaIcon: Bitmap?,
        isMedevacMode: MutableState<Boolean>,
        isTutelaMode: MutableState<Boolean>,
        currentLocation: MutableState<Point?>,
        measuringMarker: MutableState<Point?>,
        polylineManager: MutableState<PolylineAnnotationManager?>,
        onMarkerClicked: (annotation: PointAnnotation) -> Unit,
    ) {
        try {
            val gson = Gson()
            val base = gson.fromJson(rawMsg, Map::class.java) // miramos solo el "type"

            when (base["type"]) {
                "init_state" -> {
                    val msg = gson.fromJson(rawMsg, InitStateMessage::class.java)

                    markerList.clear()
                    medevacList.clear()
                    tutelaList.clear()
                    pointAnnotationManager?.deleteAll()

                    msg.markers.forEach { marker ->
                        pointAnnotationManager?.let { mgr ->
                            val fakeCreate = MarkerMessage(
                                id = marker.id,
                                type = "create",
                                user = marker.marker.createdBy,
                                medevac = marker.medevac,
                                tutela = marker.tutela,
                                marker = marker.marker
                            )
                            // Reutilizas la misma ruta que un mensaje entrante normal
                            handleIncomingRawMessage(
                                Gson().toJson(fakeCreate),
                                pointAnnotationManager,
                                markerList,
                                selectedMarker,
                                medevacList,
                                tutelaList,
                                medevacIcon,
                                warningIcon,
                                tutelaIcon,
                                isMedevacMode,
                                isTutelaMode,
                                currentLocation,
                                measuringMarker,
                                polylineManager,
                                onMarkerClicked = { clicked ->
                                    selectedMarker.value = clicked
                                }
                            )
                        }
                    }
                }

                "position" -> {
                    val msg = gson.fromJson(rawMsg, PositionMessage::class.java)
                    handleIncomingLocation(msg)   // 👈 aquí reutilizas tu función ya hecha

                }

                "create" -> {
                    val msg = gson.fromJson(rawMsg, MarkerMessage::class.java)
                    val point = Point.fromLngLat(msg.marker.point.longitude(), msg.marker.point.latitude())

                    // seleccionar icono según tipo (aquí de momento genérico)
                    Log.d("WebSocket", "📩 Mensaje bruto: ")

                    val option = msg.marker.type

                    when (option) {
                        MarkerType.NORMAL -> {
                            val bmp = base64ToBitmap(msg.marker.icon!!)
                            pointAnnotationManager?.let { mgr ->
                                placeMarker(
                                    mgr = mgr,
                                    bmp = bmp,
                                    point = point,
                                    id = markerList.size.toInt()+1,
                                    currentLocation = currentLocation,
                                    markerList = markerList,
                                    usuario = msg.user,
                                    onMarkerClicked = { clicked ->
                                        selectedMarker.value = clicked
                                    }
                                )

                            }
                        }

                        MarkerType.MEDEVAC -> {
                            pointAnnotationManager?.let { mgr ->
                                placeMarker(
                                    mgr = mgr,
                                    bmp = medevacIcon!!,
                                    point = point,
                                    id = markerList.size.toInt()+1,
                                    usuario = msg.user,
                                    currentLocation = currentLocation,
                                    markerList = markerList,
                                    medevacData = msg.medevac!!,
                                    medevacList = medevacList,
                                    isMedevacMode = isMedevacMode,
                                    onMarkerClicked = { clicked ->
                                        selectedMarker.value = clicked
                                    }
                                )

                                markerList.add(
                                    MarkerData(
                                        id = markerList.size.toInt()+1,
                                        name = "Medevac ${medevacList.size.toInt()}",
                                        createdBy = msg.user,
                                        distance = medevacList.last()?.distancia,
                                        icon = medevacIcon,
                                        point = msg.medevac.line1!!,
                                        type = MarkerType.MEDEVAC,
                                        medevac = msg.medevac   // 👈 ahora sí
                                    )
                                )

                            }
                        }

                        MarkerType.TUTELA -> {
                            if ((tutelaList.size+1) %2 != 0) {
                                pointAnnotationManager?.let { mgr ->
                                    placeMarker(
                                        mgr = mgr,
                                        bmp = tutelaIcon!!,
                                        point = point,
                                        id = markerList.size.toInt() +1 ,
                                        currentLocation = currentLocation,
                                        markerList = markerList,
                                        usuario = msg.user,
                                        isTutelaMode = isTutelaMode,
                                        onMarkerClicked = { clicked ->
                                            selectedMarker.value = clicked
                                        },
                                        tutelaList = tutelaList,
                                        tutelaData = msg.tutela
                                    )

                                    markerList.add(
                                        MarkerData(
                                            id = markerList.size.toInt() + 1,
                                            name = "Tutela ${(tutelaList.size+1)/2}",
                                            createdBy = msg.user,
                                            distance = tutelaList.last()?.distancia,
                                            icon = tutelaIcon,
                                            point = point,
                                            type = MarkerType.TUTELA,
                                            tutela = msg.tutela
                                        )
                                    )
                                }

                            } else {
                                pointAnnotationManager?.let { mgr ->
                                    val updatedTutela = tutelaList.last()?.copy(localizacion = tutelaList.last()?.localizacion)
                                    placeMarker(
                                        mgr = mgr,
                                        bmp = warningIcon!!,
                                        point = msg.tutela?.puesto!!,
                                        id = markerList.size +1,
                                        currentLocation = currentLocation,
                                        markerList = markerList,
                                        usuario = msg.user,
                                        isTutelaMode = isTutelaMode,
                                        onMarkerClicked = { clicked ->
                                            selectedMarker.value = clicked
                                        },
                                        tutelaList = tutelaList,
                                        tutelaData = updatedTutela
                                    )

                                    markerList.add(
                                        MarkerData(
                                            id = markerList.size.toInt() + 1,
                                            name = "Observación ${(tutelaList.size.toInt())/2}",
                                            createdBy = "Tutela ${(tutelaList.size.toInt())/2} -" +
                                                    " ${msg.user}",
                                            icon = warningIcon,
                                            distance = tutelaList.last()?.distancia?.toDouble(),
                                            point = msg.tutela.puesto!!,
                                            type = MarkerType.TUTELA,
                                            tutela = updatedTutela
                                        )
                                    )

                                }
                            }

                        } else -> {}
                    }
                }

                "delete" -> {
                    val gson = Gson()
                    val msg = gson.fromJson(rawMsg, DeleteMessage::class.java)
                    Log.d("WebSocket", "🗑 Eliminar marcador en ${msg.point} recibido")
                    removeMarkerByPoint(
                         msg.point,
                         markerList,
                         medevacList,
                         tutelaList,
                         pointAnnotationManager!!,
                     )

                    if (measuringMarker.value == msg.point) {
                        measuringMarker.value = null
                        polylineManager.value?.deleteAll()
                    }
                }

                "user_disconnect" -> {
                    val user = base["user"] as String
                    remoteUsers.remove(user)
                    Log.d("WebSocket", "❌ Usuario $user desconectado")

                    val features = remoteUsers.map { (id, pair) ->
                        val (pt, brg) = pair
                        Feature.fromGeometry(pt).apply {
                            addStringProperty("user", id)
                            addNumberProperty("bearing", brg)
                        }
                    }
                    userSourceRef.value?.featureCollection(
                        FeatureCollection.fromFeatures(features)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketConfig", "❌ Error parseando mensaje: ${e.message}")
        }
    }

    // Manejar mensajes de posición
    fun handleIncomingLocation(msg: PositionMessage) {
        if (msg.type != "position") return

        val oldPoint = remoteUsers[msg.user]?.first
        val oldBearing = remoteUsers[msg.user]?.second ?: (msg.bearing ?: 0.0)
        val newPoint = msg.point
        val newBearing = msg.bearing ?: 0.0

        // cancelar animación previa si existe
        animationJobs[msg.user]?.cancel()

        if (oldPoint != null) {
            animationJobs[msg.user] = coroutineScope.launch(Dispatchers.Main) {
                val duration = 300L
                val startTime = System.currentTimeMillis()
                while (isActive) {
                    val t = (System.currentTimeMillis() - startTime).toFloat() / duration
                    val fraction = min(1f, t)
                    val eased = fraction * fraction * (3 - 2 * fraction)

                    val lat = oldPoint.latitude() + (newPoint.latitude() - oldPoint.latitude()) * eased
                    val lng = oldPoint.longitude() + (newPoint.longitude() - oldPoint.longitude()) * eased
                    val interpolatedPoint = Point.fromLngLat(lng, lat)

                    val interpolatedBearing = interpolateBearing(oldBearing, newBearing, eased)

                    remoteUsers[msg.user] = interpolatedPoint to interpolatedBearing

                    val features = remoteUsers.map { (id, pair) ->
                        val (pt, brg) = pair
                        Feature.fromGeometry(pt).apply {
                            addStringProperty("user", id)
                            addNumberProperty("bearing", brg)
                        }
                    }
                    userSourceRef.value?.featureCollection(
                        FeatureCollection.fromFeatures(features)
                    )

                    if (fraction >= 1f) break
                    delay(16)
                }
            }

        } else {
            remoteUsers[msg.user] = newPoint to newBearing
            val features = remoteUsers.map { (id, pair) ->
                val (pt, brg) = pair
                Feature.fromGeometry(pt).apply {
                    addStringProperty("user", id)
                    addNumberProperty("bearing", brg)
                }
            }
            userSourceRef.value?.featureCollection(
                FeatureCollection.fromFeatures(features)
            )
        }
    }


    fun getConnectedUsers(): List<String> {
        return remoteUsers.keys.toList()
    }

    fun connectAndIdentify(wsClient: WebSocketConnect, username: String) {
        wsClient.connect("192.168.1.102", 8080)  // o la IP de tu ServerConfigurationScreen
        val initMsg = """{"type":"hello","user":"$username"}"""
        wsClient.sendMessage(initMsg)
        Log.d("WebSocketClient", "👋 Usuario $username identificado en el servidor")
    }


    private fun interpolateBearing(oldBearing: Double, newBearing: Double, fraction: Float): Double {
        var delta = (newBearing - oldBearing + 540) % 360 - 180
        return (oldBearing + delta * fraction + 360) % 360
    }
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun base64ToBitmap(base64Str: String): Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

object MapStyleConfig {

    fun applyRemoteUsersStyle(style: Style, userSourceRef: MutableState<GeoJsonSource?>, navBitmap: Bitmap) {
        // Crear GeoJsonSource vacío
        val source = GeoJsonSource.Builder("remote-users-source")
            .featureCollection(FeatureCollection.fromFeatures(emptyList()))
            .build()
        style.addSource(source)
        userSourceRef.value = source

        // Añadir icono por defecto
        style.addImage("remote-user-icon", navBitmap)

        // Añadir capa para usuarios
        val symbolLayer = SymbolLayer("remote-users-layer", "remote-users-source").apply {
            iconImage("remote-user-icon")
            iconAllowOverlap(true)
            iconIgnorePlacement(true)
            textField(get("user"))   // mostrar nombre del usuario
            textOffset(listOf(0.0, 1.5))
            textSize(12.0)
            iconRotate(get("bearing"))
        }
        style.addLayer(symbolLayer)
    }
}

