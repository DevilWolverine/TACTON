package com.example.tactonprueba.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.turf.TurfMeasurement

// ========== Muestra la distancia entre dos puntos y cierra ===========
@Composable
fun DistantToMarker(
    distance: Double,
    modifier: Modifier,
    polylineManager: PolylineAnnotationManager?,
    measuringMarker: MutableState<Point?>,
    userLocation: Point,
    heading: Float,
    mapView: MapView,
    placing: Boolean,
    isMeasuringMode: MutableState<Boolean>,
    isPickingLocalizacion: MutableState<Boolean>,
    isTutelaMode: MutableState<Boolean>,
    isMedevacMode: MutableState<Boolean>,
) {

    val marker = measuringMarker.value
    val bearing = marker?.let {
        TurfMeasurement.bearing(userLocation, it).toFloat()
    } ?: 0f

    val useDeviceOrientation = remember { mutableStateOf(true) }

    // Distancia entre dos puntos
    Column(
        modifier =  if (!placing && !isMeasuringMode.value && !isPickingLocalizacion.value && !isTutelaMode.value && !isMedevacMode.value) modifier.padding(top = 96.dp, end = 13.dp)
                    else modifier.padding(top = 165.dp, end = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Metros
        Text(
            text = "${"%.1f".format(distance)} m",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                .padding(6.dp)
        )
        // Cajon cruz y flecha
        Row (verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(start = 8.dp)){
            Icon(
                Icons.Default.ArrowUpward,
                contentDescription = "Dirección al marcador",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = CircleShape)
                    .clickable { useDeviceOrientation.value = !useDeviceOrientation.value }
                    .graphicsLayer {
                        rotationZ = if (useDeviceOrientation.value) {
                            // 🔹 Orientación relativa con brújula
                            ((bearing - heading + 360f) % 360f)
                        } else {
                            // 🔹 Orientación relativa con la cámara del mapa
                            ((bearing - mapView.mapboxMap.cameraState.bearing.toFloat() + 360f) % 360f)
                        }
                    }
            )

            IconButton(
                onClick = {
                    measuringMarker.value = null
                    isMeasuringMode.value = false
                    polylineManager?.deleteAll()
                }
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Terminar medida",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }

    }
}

// ========== Dibuja la línea entre dos puntos ===========
fun drawDistanceLine(
    polylineManager: PolylineAnnotationManager?,
    userLocation: Point,
    markerLocation: Point
) {
    polylineManager?.deleteAll()
    val polylineOptions = PolylineAnnotationOptions()
        .withPoints(listOf(userLocation, markerLocation))
        .withLineColor("#FF0000")
        .withLineWidth(4.0)
    polylineManager?.create(polylineOptions)

}

fun removeMarkerAndCancelMeasure(
    point: Point,
    markers: MutableList<MarkerData>,
    annotationManager: PointAnnotationManager,
    medevacs: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    isMedevacMode: MutableState<Boolean> = mutableStateOf(false),
    tutelas: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    isTutelaMode: MutableState<Boolean> = mutableStateOf(false),
    isMeasuringMode: MutableState<Boolean>,
    measuringMarker: MutableState<Point?>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
) {
    // Eliminar marcador normalmente
    removeMarkerByPoint(
        point = point,
        markers = markers,
        medevacs = medevacs,
        tutelas = tutelas,
        isTutelaMode = isTutelaMode,
        annotationManager = annotationManager,
        isMedevacMode = isMedevacMode,
    )

    // 👇 SOLO limpiar la medición si el punto eliminado era el mismo que estaba midiendo
    if (measuringMarker.value == point) {
        measuringMarker.value = null
        isMeasuringMode.value = false
        polylineManager.value?.deleteAll()
    }
}
