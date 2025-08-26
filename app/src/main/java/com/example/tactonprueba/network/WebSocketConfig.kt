package com.example.tactonprueba.network

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.tactonprueba.utils.MarkerData
import com.example.tactonprueba.utils.placeMarker
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
import kotlinx.coroutines.*
import kotlin.math.min

// Posición de usuario
data class PositionMessage(
    val type: String,
    val user: String,
    val point: Point,
    val bearing: Double? = null
)

// Creación de marcador
data class MarkerMessage(
    val type: String = "marker_create",
    val user: String,
    val id: Int,
    val point: Point,
    val icon: String? = null, // "NORMAL", "MEDEVAC", "TUTELA"...
    val label: String? = null
)


class WebSocketConfig(
    private val remoteUsers: MutableMap<String, Pair<Point, Double>>,
    private val userSourceRef: MutableState<GeoJsonSource?>,
    private val coroutineScope: CoroutineScope
) {
    private val animationJobs = mutableMapOf<String, Job>()

    // Manejar mensajes entrantes
    fun handleIncomingRawMessage(
        rawMsg: String,
        pointAnnotationManager: PointAnnotationManager?,
        defaultMarkerBitmap: Bitmap,
        markerList: SnapshotStateList<MarkerData>,
        onMarkerClicked: (PointAnnotation) -> Unit
    ) {
        try {
            val gson = Gson()
            val base = gson.fromJson(rawMsg, Map::class.java) // miramos solo el "type"

            when (base["type"]) {
                "position" -> {
                    val msg = gson.fromJson(rawMsg, PositionMessage::class.java)
                    handleIncomingLocation(msg)   // 👈 aquí reutilizas tu función ya hecha

                }

                "marker_create" -> {
                    val msg = gson.fromJson(rawMsg, MarkerMessage::class.java)
                    val point = Point.fromLngLat(msg.point.longitude(), msg.point.latitude())

                    // seleccionar icono según tipo (aquí de momento genérico)
                    Log.d("WebSocket", "📩 Mensaje bruto: ")
                    val bmp = defaultMarkerBitmap

                    pointAnnotationManager?.let { mgr ->
                        placeMarker(
                            mgr = mgr,
                            bmp = bmp,
                            point = point,
                            id = msg.id,
                            distance = 0.0,
                            markerList = markerList,
                            onMarkerClicked = onMarkerClicked
                        )
                    }
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



    fun connectAndIdentify(wsClient: WebSocketClient, username: String) {
        wsClient.connect("192.168.1.32", 8080)  // 👈 puedes parametrizar IP/puerto si quieres
        val initMsg = """{"type":"hello","user":"$username"}"""
        wsClient.sendMessage(initMsg)
        Log.d("WebSocketConfig", "👋 Usuario $username identificado en el servidor")
    }

    private fun interpolateBearing(oldBearing: Double, newBearing: Double, fraction: Float): Double {
        var delta = (newBearing - oldBearing + 540) % 360 - 180
        return (oldBearing + delta * fraction + 360) % 360
    }
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
