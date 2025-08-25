@file:Suppress("DEPRECATION")

package com.example.tactonprueba.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tactonprueba.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.DefaultLocationProvider
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlin.math.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine



@OptIn(ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
suspend fun centerMapOnUserLocation(context: Context, mapView: MapView): Location? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val location = suspendCancellableCoroutine<Location?> { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { cont.resume(it, onCancellation = null) }
            .addOnFailureListener {
                Log.e("ubi.kt", "Error obteniendo ubicación: ${it.message}")
                cont.resume(null, onCancellation = null)
            }
    }

    location?.let {
        val userPoint = Point.fromLngLat(it.longitude, it.latitude)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(userPoint)
                .zoom(16.0)
                .build()
        )
    } ?: Log.w("ubi.kt", "No se encontró la ubicación actual.")

    return location
}

// =============== Prueba orientacion movil ===============
@SuppressLint("MissingPermission")
fun Context.orientationFlow(): Flow<Float> = callbackFlow {
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            trySend(azimuth)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)

    awaitClose { sensorManager.unregisterListener(listener) }
}

@Composable
fun UTMCoordinateBox(modifier: Modifier = Modifier, location: Point?) {
    var showUTM by remember { mutableStateOf(true) }  // Alterna entre UTM y LatLon
    val utm = location?.let { latLonToUTM(it.latitude(), it.longitude()) }

    Box(
        modifier = modifier
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, enabled = location != null) {
                showUTM = !showUTM
            }
            .background(
                color = Color.Black.copy(alpha = 1f),
                shape = RoundedCornerShape(topEnd = 12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .width(180.dp)

    ) {

        val text = if (location == null) {
            "Localizando..."
        } else {
            if (showUTM) {
                val huso = "${utm!!.zone}${utm.band}"
                "Indicativo: Devil\nCoordenadas UTM\n$huso X: ${utm.x.toInt()} Y: ${utm.y.toInt()} "
            } else {
                "Indicativo: Devil\nCoordenadas Lat/Lon\nX: ${"%.5f".format(location.latitude())} Y: ${"%.5f".format(location.longitude())}"
            }
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CoordinateInputPanel(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    mapView: MapView,
    modifier: Modifier = Modifier,
    marca: Bitmap,
    markerIdCounter: MutableState<Int>,
    pointAnnotationManager: PointAnnotationManager,
    onMarkerClicked: (PointAnnotation) -> Unit,
    markerList: SnapshotStateList<MarkerData>,
    currentLocation: MutableState<Point?>
) {
    var mode by remember { mutableStateOf("UTM") }

    // UTM State
    var x by remember { mutableStateOf("") }
    var y by remember { mutableStateOf("") }
    var band by remember { mutableStateOf(TextFieldValue("30T")) }

    // Lat/Lon State
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }

    val zoneBandFocusRequester = remember { FocusRequester() }
    var shouldSelectZoneBand by remember { mutableStateOf(false) }


    // Focus requesters para cada campo UTM
    val xFocus = remember { FocusRequester() }
    val yFocus = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleGoToUTM = {
        val parsedX = x.replace(',', '.').toDoubleOrNull()
        val parsedY = y.replace(',', '.').toDoubleOrNull()
        val match = Regex("""^(\d{1,2})([C-HJ-NP-X])$""").find(band.text.trim().uppercase())
        val parsedZone = match?.groups?.get(1)?.value?.toIntOrNull()
        val bandChar = match?.groups?.get(2)?.value?.firstOrNull()

        if (parsedX != null && parsedY != null && parsedZone != null && bandChar != null) {
            goToUTM(
                mapView,
                parsedX,
                parsedY,
                parsedZone,
                bandChar,
                pointAnnotationManager,
                marca,
                markerIdCounter,
                onMarkerClicked,
                markerList,
                currentLocation)
        }

        keyboardController?.hide()
        onDismissRequest()
    }

    val handleGoToLatLon = {
        val normalizedLat = lat.replace(',', '.')
        val normalizedLon = lon.replace(',', '.')

        val parsedLat = normalizedLat.toDoubleOrNull()
        val parsedLon = normalizedLon.toDoubleOrNull()

        if (parsedLat != null && parsedLon != null) {
            goToLatLon(
                mapView,
                parsedLat,
                parsedLon,
                pointAnnotationManager,
                marca,
                markerIdCounter,
                onMarkerClicked,
                markerList,
                currentLocation)
        }
        keyboardController?.hide()
        onDismissRequest()
    }

    val customTextColors =  TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.LightGray,
        cursorColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    //Mantiene los valores por defecto cada vez que se oculta el panel
    LaunchedEffect(isVisible) {
        if (isVisible) {
            x = ""
            y = ""
            band = TextFieldValue("30T")
            lat = ""
            lon = ""
            mode = "UTM"
        }
    }

    // Manejo de Focus para los campos de texto de UTM
    LaunchedEffect(shouldSelectZoneBand) {
        if (shouldSelectZoneBand) {
            band = band.copy(
                selection = TextRange(0, band.text.length)
            )
            shouldSelectZoneBand = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
        exit = fadeOut(tween(300)) + scaleOut(tween(300)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            onDismissRequest()
                        }
                    )
            )

            Column(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .background(Color.Black.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .fillMaxHeight(0.55f)
                    .clickable(enabled = false) {}
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                )  {
                    Text(
                        text = "Coordenadas del destino",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Selector de modo
                Row(Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Center/*Arrangement.SpaceEvenly*/

                ) {

                    Button(
                        onClick = { mode = "UTM" },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "UTM") Color.DarkGray else Color.Gray
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("UTM")
                    }
                    Button(
                        onClick = { mode = "LatLon" },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "LatLon") Color.DarkGray else Color.Gray
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Lat/Lon")
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (mode == "UTM") {
                    Column (modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Column (modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            OutlinedTextField(
                                value = x,
                                onValueChange = { x = it },
                                label = { Text("Coordenada X") },
                                singleLine = true,
                                colors = customTextColors,
                                modifier = Modifier
                                    .focusRequester(xFocus),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { yFocus.requestFocus() }
                                )
                            )
                            OutlinedTextField(
                                value = y,
                                onValueChange = { y = it },
                                label = { Text("Coordenada Y") },
                                singleLine = true,
                                colors = customTextColors,
                                modifier = Modifier
                                    .focusRequester(yFocus),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { zoneBandFocusRequester.requestFocus() }
                                )
                            )
                            OutlinedTextField(
                                value = band,
                                onValueChange = { band = it.copy(it.text.uppercase()) },
                                label = { Text("Huso") },
                                singleLine = true,
                                colors = customTextColors,
                                modifier = Modifier
                                    .focusRequester(zoneBandFocusRequester)
                                    .onFocusEvent { event ->
                                        if (event.isFocused) {
                                            shouldSelectZoneBand = true
                                        }
                                    },
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { handleGoToUTM() }
                                )
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Box (
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .background(Color(0xFF4B1818), shape = RoundedCornerShape(12.dp))
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            //Boton Ir a destino UTM
                            Button(
                                modifier = Modifier.matchParentSize(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B1818)),
                                onClick = { handleGoToUTM() }
                            ) {
                                Text("Ir al destino")
                            }
                        }
                    }

                } else {

                    Column (modifier = Modifier.align(Alignment.CenterHorizontally)) {

                        Column {

                            OutlinedTextField(
                                value = lat,
                                onValueChange = { lat = it },
                                label = { Text("Latitud") },
                                singleLine = true,
                                colors = customTextColors,
                                modifier = Modifier
                                    .focusRequester(xFocus),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { yFocus.requestFocus() }
                                )
                            )

                            OutlinedTextField(
                                value = lon,
                                onValueChange = { lon = it },
                                label = { Text("Longitud") },
                                singleLine = true,
                                colors = customTextColors,
                                modifier = Modifier
                                    .focusRequester(yFocus),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { handleGoToLatLon() }
                                )
                            )
                        }

                        Spacer(Modifier.height(80.dp))

                        Box (
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .background(Color(0xFF4B1818), shape = RoundedCornerShape(12.dp))
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            //Boton Ir a destino LAT/LON
                            Button(
                                modifier = Modifier.matchParentSize(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B1818)),
                                onClick = { handleGoToLatLon() }
                            ) {
                                Text("Ir al destino")
                            }
                        }

                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission", "NewApi")
@Composable
fun Botonera(
    context: Context,
    mapView: MapView,
    coroutineScope: CoroutineScope,
    isMenuOpen: MutableState<Boolean>,
    isCoordOpen: MutableState<Boolean>,
    isFollowingLocation: MutableState<Boolean>,
    isPlacingMarker: MutableState<Boolean>,
    selectedMarker: MutableState<PointAnnotation?>,
    isMeasuringMode: MutableState<Boolean>,
    isPickingLocalizacion: MutableState<Boolean>,
    isMedevacMode: MutableState<Boolean>,
    isTutelaMode: MutableState<Boolean>,
) {
    var isRemoving: Boolean = !isPlacingMarker.value &&
        selectedMarker.value == null &&
        !isMeasuringMode.value &&
        !isPickingLocalizacion.value &&
        !isMedevacMode.value &&
        !isTutelaMode.value
    var change: Boolean = isPlacingMarker.value ||
            isMeasuringMode.value ||
            isPickingLocalizacion.value ||
            isMedevacMode.value ||
            isTutelaMode.value

    // ========= Botón para abrir el menú ========
    IconButton(
        onClick = { isMenuOpen.value = !isMenuOpen.value },
        enabled = isRemoving
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menú",
            tint = if (change) Color.Gray else LocalContentColor.current
        )
    }

    // ========== Botón para ir a coordenadas ==========
    IconButton(
        onClick = {
            isCoordOpen.value = !isCoordOpen.value
            if (isCoordOpen.value) {
                isMenuOpen.value = false
            }
        },
        enabled = isRemoving
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Ir a coordenadas",
            tint = if (change) Color.Gray else LocalContentColor.current
        )
    }

    // ========== Botón para fijar ubicación ==========
    IconButton(
        onClick = {
            isFollowingLocation.value = !isFollowingLocation.value

            if (isFollowingLocation.value) {
                Toast.makeText(context, "Modo Fijo", Toast.LENGTH_SHORT).show()
                // 1. Centrar inmediatamente
                coroutineScope.launch {
                    centerMapOnUserLocation(context, mapView)
                }

                // 2. Bloquear gestos del mapa
                mapView.gestures.updateSettings {
                    scrollEnabled = false
                }

                mapView.location.setLocationProvider(DefaultLocationProvider(context))

            } else {
                Toast.makeText(context, "Modo Libre", Toast.LENGTH_SHORT).show()
                // 4. Volver al estado libre (gestos ON)
                mapView.gestures.updateSettings {
                    scrollEnabled = true
                }
            }
        },
        enabled = isRemoving
    ) {
        Icon(
            painter = painterResource(
                id = if (!isFollowingLocation.value)
                    R.drawable.arrows_output
                else
                    R.drawable.arrows_input
            ),
            contentDescription = if (!isFollowingLocation.value)
                "Desactivar seguimiento"
            else
                "Activar seguimiento",
            modifier = Modifier.size(24.dp),
            tint = if (change) Color.Gray else LocalContentColor.current
        )
    }

}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun CustomCompass(
    context: Context,
    mapView: MapView,
    modifier: Modifier = Modifier,
    heading: Float,
) {
    // Estado del bearing del mapa
    val bearing by northMapBearing(mapView)

    // Estado → modo de la brújula
    var useDeviceOrientation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calcular la rotación
    val rotation = if (useDeviceOrientation) {
        -heading -44f// 🔹 brújula al norte magnético del móvil
    } else {
        -bearing - 44f // 🔹 brújula al norte del mapa (con ajuste de icono)
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .padding(16.dp)
            .graphicsLayer { rotationZ = rotation }
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    // 🔹 Centrar mapa y resetear bearing
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder().bearing(0.0).build()
                    )
                    coroutineScope.launch {
                        centerMapOnUserLocation(context, mapView)
                    }
                },
                onLongClick = {
                    // 🔹 Cambiar modo de brújula
                    useDeviceOrientation = !useDeviceOrientation
                    if (useDeviceOrientation) {
                        Toast.makeText(context, "Modo Dispositivo", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Modo Mapa", Toast.LENGTH_SHORT).show()
                    }
                }
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.compass),
            contentDescription = "Brújula personalizada",
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(useDeviceOrientation, heading) {
        if (useDeviceOrientation) {
            mapView.mapboxMap.easeTo(
                CameraOptions.Builder()
                    .bearing(heading.toDouble())
                    .build(),
                mapAnimationOptions {
                    duration(250) // en ms → medio segundo suave
                }
            )
        }
    }

}

@Composable
fun northMapBearing(mapView: MapView): State<Float> {
    val bearingState = remember { mutableFloatStateOf(0f) }

    DisposableEffect(mapView) {
        val listener = OnCameraChangeListener {
            bearingState.floatValue = mapView.mapboxMap.cameraState.bearing.toFloat()
        }
        mapView.mapboxMap.addOnCameraChangeListener(listener)
        onDispose {
            mapView.mapboxMap.removeOnCameraChangeListener(listener)
        }
    }

    return bearingState
}

@SuppressLint("MissingPermission")
fun locationUpdatesFlow(context: Context): Flow<Location?> = callbackFlow {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
        .setMinUpdateIntervalMillis(1000L)
        .build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            trySend(result.lastLocation).isSuccess
        }
    }

    fusedClient.requestLocationUpdates(request, callback, null)

    awaitClose {
        fusedClient.removeLocationUpdates(callback)
    }
}

// ============ Centrado de camara al punto ==============
fun cameraMove(
    mapView: MapView?,
    point: Point?
) {
        // 🔹 Aquí centras la cámara
        mapView?.mapboxMap?.setCamera(
            CameraOptions.Builder()
                .center(point)
                .zoom(16.0)
                .build()
        )
}

// =========================================
// Funciones Ir a Coordenadas
// =========================================

fun goToUTM(
    mapView: MapView,
    x: Double,
    y: Double,
    zone: Int,
    hemisphere: Char,
    pointAnnotationManager: PointAnnotationManager,
    defaultMarkerBitmap: Bitmap,
    markerIdCounter: MutableState<Int>,
    onMarkerClicked: (PointAnnotation) -> Unit,
    markerList: SnapshotStateList<MarkerData>,
    currentLocation: MutableState<Point?>
) {
    val (lat, lon) = utmToLatLon(x, y, zone, hemisphere)
    val point = Point.fromLngLat(lon, lat)


    cameraMove(mapView, point)

    var distance = TurfMeasurement.distance(
        currentLocation.value!!,
        point,
        TurfConstants.UNIT_METERS
    )

    placeMarker(
        mgr = pointAnnotationManager,
        bmp = defaultMarkerBitmap,
        point = point,
        id = markerIdCounter.value,
        distance = distance,
        markerList = markerList,
        onMarkerClicked = onMarkerClicked,
    )


    // Incrementar ID para el siguiente
    markerIdCounter.value++

}

fun goToLatLon(
    mapView: MapView,
    latitude: Double,
    longitude: Double,
    pointAnnotationManager: PointAnnotationManager,
    defaultMarkerBitmap: Bitmap,
    markerIdCounter: MutableState<Int>,
    onMarkerClicked: (PointAnnotation) -> Unit,
    markerList: SnapshotStateList<MarkerData>,
    currentLocation: MutableState<Point?>
) {
    val point = Point.fromLngLat(longitude, latitude)
    cameraMove(mapView, point)

    var distance = TurfMeasurement.distance(
        currentLocation.value!!,
        point,
        TurfConstants.UNIT_METERS
    )

    placeMarker(
        mgr = pointAnnotationManager,
        bmp = defaultMarkerBitmap,
        point = point,
        id = markerIdCounter.value,
        distance = distance,
        markerList = markerList,
        onMarkerClicked = onMarkerClicked,
    )

    // Incrementar ID para el siguiente
    markerIdCounter.value++

}

// =========================================
// 🔽 Conversión LAT/LON → UTM y viceversa
// =========================================

data class UTMCoord(val x: Double, val y: Double, val zone: Int, val band: Char)

fun latLonToUTM(lat: Double, lon: Double): UTMCoord {
    val a = 6378137.0 // Radio ecuatorial
    val e = 0.0818191908 // Excentricidad
    val k0 = 0.9996

    val zone = ((lon + 180) / 6).toInt() + 1
    val lonOrigin = (zone - 1) * 6 - 180 + 3
    val lonOriginRad = Math.toRadians(lonOrigin.toDouble())
    val latRad = Math.toRadians(lat)
    val lonRad = Math.toRadians(lon)

    val n = a / sqrt(1 - e * e * sin(latRad).pow(2.0))
    val t = tan(latRad).pow(2.0)
    val c = (e * e / (1 - e * e)) * cos(latRad).pow(2.0)
    val b = cos(latRad) * (lonRad - lonOriginRad)

    val m = a * (
            (1 - e * e / 4 - 3 * e.pow(4.0) / 64 - 5 * e.pow(6.0) / 256) * latRad
                    - (3 * e * e / 8 + 3 * e.pow(4.0) / 32 + 45 * e.pow(6.0) / 1024) * sin(2 * latRad)
                    + (15 * e.pow(4.0) / 256 + 45 * e.pow(6.0) / 1024) * sin(4 * latRad)
                    - (35 * e.pow(6.0) / 3072) * sin(6 * latRad)
            )

    val x = k0 * n * (
            b + (1 - t + c) * b.pow(3.0) / 6 +
                    (5 - 18 * t + t.pow(2.0) + 72 * c - 58 * (e * e / (1 - e * e))) * b.pow(5.0) / 120
            ) + 500000.0

    var y = k0 * (
            m + n * tan(latRad) * (
                    b * b / 2 + (5 - t + 9 * c + 4 * c * c) * b.pow(4.0) / 24 +
                            (61 - 58 * t + t * t + 600 * c - 330 * (e * e / (1 - e * e))) * b.pow(6.0) / 720
                    )
            )

    if (lat < 0) y += 10000000.0

    // 🟩 Calcular la banda según la latitud
    val band = when (lat) {
        in -80.0..-72.0 -> 'C'
        in -72.0..-64.0 -> 'D'
        in -64.0..-56.0 -> 'E'
        in -56.0..-48.0 -> 'F'
        in -48.0..-40.0 -> 'G'
        in -40.0..-32.0 -> 'H'
        in -32.0..-24.0 -> 'J'
        in -24.0..-16.0 -> 'K'
        in -16.0..-8.0 -> 'L'
        in -8.0..0.0 -> 'M'
        in 0.0..8.0 -> 'N'
        in 8.0..16.0 -> 'P'
        in 16.0..24.0 -> 'Q'
        in 24.0..32.0 -> 'R'
        in 32.0..40.0 -> 'S'
        in 40.0..48.0 -> 'T'
        in 48.0..56.0 -> 'U'
        in 56.0..64.0 -> 'V'
        in 64.0..72.0 -> 'W'
        in 72.0..84.0 -> 'X'
        else -> 'Z' // fuera de rango válido UTM
    }

    return UTMCoord(x, y, zone, band)
}

fun utmToLatLon(x: Double, y: Double, zone: Int, band: Char): Pair<Double, Double> {
    val a = 6378137.0
    val e = 0.0818191908
    val k0 = 0.9996

    val e1sq = e * e / (1 - e * e)
    val xAdj = x - 500000.0

    val hemisphere = if (band.uppercaseChar() >= 'N') 'N' else 'S'
    val yAdj = if (hemisphere == 'S') y - 10000000.0 else y

    val lonOrigin = (zone - 1) * 6 - 180 + 3
    val m = yAdj / k0
    val mu = m / (a * (1 - e * e / 4 - 3 * e * e * e * e / 64 - 5 * e * e * e * e * e * e / 256))

    val e1 = (1 - sqrt(1 - e * e)) / (1 + sqrt(1 - e * e))
    val j1 = 3 * e1 / 2 - 27 * e1 * e1 * e1 / 32
    val j2 = 21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32
    val j3 = 151 * e1 * e1 * e1 / 96
    val j4 = 1097 * e1 * e1 * e1 * e1 / 512

    val fp = mu + j1 * sin(2 * mu) + j2 * sin(4 * mu) + j3 * sin(6 * mu) + j4 * sin(8 * mu)

    val c1 = e1sq * cos(fp).pow(2.0)
    val t1 = tan(fp).pow(2.0)
    val n1 = a / sqrt(1 - e * e * sin(fp).pow(2.0))
    val r1 = n1 * (1 - e * e) / (1 - e * e * sin(fp).pow(2.0))
    val d = xAdj / (n1 * k0)

    val lat = fp - (n1 * tan(fp) / r1) * (
            d * d / 2 -
                    (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * e1sq) * d * d * d * d / 24 +
                    (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * e1sq - 3 * c1 * c1) * d * d * d * d * d * d / 720
            )

    val lon = Math.toRadians(lonOrigin.toDouble()) + (
            d -
                    (1 + 2 * t1 + c1) * d * d * d / 6 +
                    (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * e1sq + 24 * t1 * t1) * d * d * d * d * d / 120
            ) / cos(fp)

    return Pair(Math.toDegrees(lat), Math.toDegrees(lon))
}