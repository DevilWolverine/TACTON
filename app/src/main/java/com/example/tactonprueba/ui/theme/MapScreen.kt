package com.example.tactonprueba.ui.theme

import WebSocketClient
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.tactonprueba.R
import com.example.tactonprueba.utils.Botonera
import com.example.tactonprueba.utils.BottomPanelMenu
import com.example.tactonprueba.utils.CoordinateInputPanel
import com.example.tactonprueba.utils.CustomCompass
import com.example.tactonprueba.utils.DistantToMarker
import com.example.tactonprueba.utils.DoubleBackToExitApp
import com.example.tactonprueba.utils.MapInit
import com.example.tactonprueba.utils.MarkerCancel
import com.example.tactonprueba.utils.MarkerData
import com.example.tactonprueba.utils.MarkerMenu
import com.example.tactonprueba.utils.MarkerOption
import com.example.tactonprueba.utils.MarkerPicker
import com.example.tactonprueba.utils.MarkerType
import com.example.tactonprueba.utils.MedevacData
import com.example.tactonprueba.utils.MedevacFormPanel
import com.example.tactonprueba.utils.MedevacReportPanel
import com.example.tactonprueba.utils.TutelaData
import com.example.tactonprueba.utils.TutelaFormPanel
import com.example.tactonprueba.utils.TutelaReportPanel
import com.example.tactonprueba.utils.UTMCoordinateBox
import com.example.tactonprueba.utils.cameraMove
import com.example.tactonprueba.utils.centerMapOnUserLocation
import com.example.tactonprueba.utils.drawDistanceLine
import com.example.tactonprueba.utils.locationUpdatesFlow
import com.example.tactonprueba.utils.orientationFlow
import com.example.tactonprueba.utils.placeMarker
import com.example.tactonprueba.utils.updateMarkerIconByPoint
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PositionMessage(
    val type: String,
    val user: String,
    val point: Point
)


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MapScreen() {
    // ==================== Variables ====================
    // ==================== Mapeado ======================
    val coroutineScope = rememberCoroutineScope()
    val currentLocation = remember { mutableStateOf<Point?>(null) }
    val currentStyle = remember { mutableStateOf(MapInit.NORMAL) }
    val heading = remember { mutableFloatStateOf(0f) }
    val locationPoint = remember { mutableStateOf<Point?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // ==================== Estados =======================
    val isCenteredMap = remember { mutableStateOf(false) }
    val isCoordOpen = remember { mutableStateOf(false) }
    val isEditingMarker = remember { mutableStateOf<PointAnnotation?>(null) }
    val isFollowingLocation = remember { mutableStateOf(false) }
    val isInfoVisible = remember { mutableStateOf(true) }
    val isMeasuringMode = remember { mutableStateOf(false) }
    val isMedevacFormOpen = remember { mutableStateOf(false) }
    val isMedevacMode = remember { mutableStateOf(false) }
    val isMenuOpen = remember { mutableStateOf(false) }
    val isPickingLocation = remember { mutableStateOf(false) }
    val isTutelaFormOpen = remember { mutableStateOf(false) }
    val isTutelaMode = remember { mutableStateOf(false) }


    val markerIdCounter = remember { mutableIntStateOf(1) }
    val markerList = remember { mutableStateListOf<MarkerData>() }
    val measuringMarker = remember { mutableStateOf<Point?>(null) }
    val measuringTarget = remember { mutableStateOf<PointAnnotation?>(null) }
    val medevacIdCounter = remember { mutableIntStateOf(1) }
    val medevacList = remember { mutableStateListOf<MedevacData?>() }
    val medevacPoint = remember { mutableStateOf<Point?>(null) }

    val selectedMarker = remember { mutableStateOf<PointAnnotation?>(null) }
    val selectedMarkerBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val selectedMarkerOption = remember { mutableStateOf<MarkerOption?>(null) }
    val showUTMBox = remember { mutableStateOf(true) }
    val tutelaIdCounter = remember { mutableIntStateOf(1) }
    val tutelaList = remember { mutableStateListOf<TutelaData?>() }
    val tutelaPoint = remember { mutableStateOf<Point?>(null) }
    val warningIdCounter = remember { mutableIntStateOf(1) }
    var isPlacingMarker = remember { mutableStateOf(false) }
    var polylineManager = remember { mutableStateOf<PolylineAnnotationManager?>(null) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val iconSizePx = with(density) { 48.dp.toPx().toInt() }
    val sizePx = with(LocalDensity.current) { 32.dp.toPx().toInt() }
    val pointAnnotationManager = remember { mutableStateOf<PointAnnotationManager?>(null) }
    val navBitmap = AppCompatResources.getDrawable(context, R.drawable.nav)
        ?.toBitmap(width = sizePx, height = sizePx)
    val defaultMarkerBitmap = AppCompatResources.getDrawable(context, R.drawable.pin)
        ?.toBitmap(width = iconSizePx, height = iconSizePx)!!

    val remoteMarker = remember { mutableStateOf<PointAnnotation?>(null) }
    val wsClient = remember {
        WebSocketClient { posMsg ->
            val mgr = pointAnnotationManager.value
            val bmp = navBitmap

            if (mgr != null && bmp != null) {
                val point = posMsg.point // 👈 Ya es un Point de Mapbox

                if (remoteMarker.value == null) {
                    placeMarker(
                        mgr = mgr,
                        bmp = bmp,
                        point = point,
                        id = 999,
                        distance = 0.0,
                        markerList = markerList,
                        onMarkerClicked = { clicked -> selectedMarker.value = clicked }
                    )
                    //remoteMarker.value = ann
                    remoteMarker.value = mgr.annotations.lastOrNull()
                } else {
                    remoteMarker.value?.let { marker ->
                        marker.point = point
                        mgr.update(marker)
                    }
                }
            }
        }
    }


// Mantener referencia a marcador del compañero





    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.let { mapView ->
            pointAnnotationManager.value = mapView.annotations.createPointAnnotationManager()
        }
    }




    //========== Contenedor Principal ============
    Box(modifier = Modifier.fillMaxSize()) {

        // =========== Vista Mapa ============
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    MapInit.provideAccessToken(ctx)
                    compass.enabled = false
                    scalebar.enabled = false
                    attribution.enabled = false
                    location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                        pulsingColor = 1
                        // Puck por defecto con flecha de orientación
                        //locationPuck = createDefault2DPuck(withBearing = true)
                        locationPuck = LocationPuck2D(
                            topImage = navBitmap?.let { ImageHolder.from(it) },
                            bearingImage = navBitmap?.let { ImageHolder.from(it) }

                        )
                        // El rumbo de la flecha sigue el curso del dispositivo
                        puckBearing = PuckBearing.HEADING
                        puckBearingEnabled = true
                    }
                    mapViewRef.value = this
                    pointAnnotationManager.value = annotations.createPointAnnotationManager()
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.mapboxMap.loadStyle(currentStyle.value)
                if (!isCenteredMap.value) {
                    coroutineScope.launch {
                        centerMapOnUserLocation(context, mapView)
                        isCenteredMap.value = true
                    }
                }
            }

        )

        // ========== Referencia al mapa ============
        mapViewRef.value?.let { mapView ->

            // ========= Brújula personalizada ============
            CustomCompass(
                context = context,
                mapView =mapView,
                heading = heading.floatValue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )

            // ======== Panel Introduccion coordenadas =========
            CoordinateInputPanel(
                currentLocation = currentLocation,
                isVisible = isCoordOpen.value && mapViewRef.value != null,
                onDismissRequest = { isCoordOpen.value = false },
                mapView = mapView,
                modifier = Modifier.align(Alignment.Center),
                marca = defaultMarkerBitmap,
                markerIdCounter = markerIdCounter,
                pointAnnotationManager = pointAnnotationManager.value!!,
                markerList = markerList,
                onMarkerClicked = { clicked -> selectedMarker.value = clicked }

            )

            // ======= Coordenadas en UTM visibles en pantalla =========
            if (showUTMBox.value) {
                UTMCoordinateBox(
                    location = currentLocation.value,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            //============= Medir entre dos puntos =============
            if (measuringMarker.value != null && currentLocation.value != null /*&& polylineManager != null*/) {
                val userPoint = Point.fromLngLat(
                    currentLocation.value!!.longitude(),
                    currentLocation.value!!.latitude()
                )
                val markerPoint = measuringMarker.value!!

                val distance = TurfMeasurement.distance(
                    userPoint,
                    markerPoint,
                    TurfConstants.UNIT_METERS
                )

                drawDistanceLine(polylineManager.value, userPoint, markerPoint)

                DistantToMarker(
                    distance = distance,
                    modifier = Modifier.align(Alignment.TopEnd),
                    polylineManager = polylineManager.value,
                    measuringMarker = measuringMarker,
                    userLocation = userPoint,
                    heading = heading.floatValue,
                    mapView = mapView,
                    placing = isPlacingMarker.value,
                    isMeasuringMode = isMeasuringMode,
                    isMedevacMode = isMedevacMode,
                    isTutelaMode = isTutelaMode,
                    isPickingLocalizacion = isPickingLocation,

                )

            }

            // ====== Menú contextual de marcador seleccionado =========
            selectedMarker.value?.let { marker ->
                val markerData = markerList.find { it.point == marker.point }
                if (markerData != null) {
                    if (markerData.type == MarkerType.NORMAL) {
                        MarkerMenu(
                            mapView = mapView,
                            selectedMarker = marker,
                            pointAnnotationManager = pointAnnotationManager.value!!,
                            onDismiss = { selectedMarker.value = null },
                            distantRequest = { point -> measuringMarker.value = point },
                            userLocation = currentLocation.value,
                            isInfoVisible = isInfoVisible,
                            isMeasuringMode = isMeasuringMode,
                            measuringMarker = measuringMarker,
                            markerList = markerList,
                            isEditingMarker = isEditingMarker,
                            polylineManager = polylineManager,

                        )
                    } else if (markerData.type == MarkerType.MEDEVAC && markerData.medevac != null) {
                        cameraMove(mapView, markerData.point)
                        MedevacReportPanel(
                            medevac = markerData.medevac,
                            selectedMarker = marker,
                            onDismiss = { selectedMarker.value = null },

                        )
                    } else if (markerData.type == MarkerType.TUTELA && markerData.tutela != null) {
                        cameraMove(mapView, markerData.point)
                        TutelaReportPanel(
                            tutela = markerData.tutela,
                            selectedMarker = marker,
                            onDismiss = { selectedMarker.value = null },
                        )
                    }
                }
            }

            // ======= Modo medir entre puntos =========
            DisposableEffect(mapViewRef.value, isMeasuringMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}

                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isMeasuringMode.value) return@OnMapClickListener false

                    val mgr = pointAnnotationManager.value ?: return@OnMapClickListener false

                    var distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        point,
                        TurfConstants.UNIT_METERS
                    )

                    // Crear marcador por defecto
                    val bmp = defaultMarkerBitmap // 👈 por ejemplo
                    placeMarker(
                        mgr = mgr,
                        bmp = bmp,
                        point = point,
                        id = markerIdCounter.intValue,
                        distance = distance,
                        markerList = markerList,
                        onMarkerClicked = { clicked ->
                            selectedMarker.value = clicked
                        },
                        medevacList = medevacList,
                    )

                    markerIdCounter.intValue++

                    // 👉 Iniciar la medición automáticamente
                    measuringMarker.value = point

                    // Salir del modo medir
                    isMeasuringMode.value = false
                    true
                }

                if (isMeasuringMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

            // ======= Modo colocar marcador ===========
            DisposableEffect(mapViewRef.value, isPlacingMarker.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isPlacingMarker.value) return@OnMapClickListener false
                    //Recoge el icono
                    val bmp = selectedMarkerBitmap.value ?: return@OnMapClickListener false
                    val mgr = pointAnnotationManager.value ?: return@OnMapClickListener false

                    var distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        point,
                        TurfConstants.UNIT_METERS
                    )

                    // Crear marcador por defect
                    placeMarker(
                        mgr = mgr,
                        bmp = bmp,
                        point = point,
                        id = markerIdCounter.intValue,
                        distance = distance,
                        markerList = markerList,
                        onMarkerClicked = { clicked ->
                            selectedMarker.value = clicked
                        },
                        medevacList = medevacList,
                    )

                    markerIdCounter.intValue++

                    // salir de modo colocar
                    isPlacingMarker.value = false
                    selectedMarkerBitmap.value = null
                    true
                }

                if (isPlacingMarker.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

            // ======= Modo solicitar evacuación (Medevac) =========
            DisposableEffect(mapViewRef.value, isMedevacMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isMedevacMode.value) return@OnMapClickListener false

                    // Guardamos la ubicación seleccionada
                    medevacPoint.value = point

                    // Abrimos el formulario
                    isMedevacFormOpen.value = true

                    // Cerramos el modo colocar
                    isMedevacMode.value = false
                    true
                }

                if (isMedevacMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

            // ======= Modo informe tutela =========
            DisposableEffect(mapViewRef.value, isTutelaMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isTutelaMode.value) return@OnMapClickListener false

                    // Guardamos la ubicación seleccionada
                    tutelaPoint.value = point

                    // Abrimos el formulario
                    isTutelaFormOpen.value = true

                    // Cerramos el modo colocar
                    isTutelaMode.value = false

                    true
                }

                if (isTutelaMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

            // ======= Selección de localización secundaria (Tutela) =========
            // ======= Selección de localización secundaria (Tutela) =========
            DisposableEffect(mapViewRef.value, isPickingLocation.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isPickingLocation.value) return@OnMapClickListener false

                    // Guardamos la localización seleccionada
                    locationPoint.value = point

                    // Cerramos modo selección y volvemos a abrir el panel
                    isPickingLocation.value = false
                    isTutelaFormOpen.value = true  // 👈 reabre el panel automáticamente

                    true
                }

                if (isPickingLocation.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }


            // =========== Botones de Esquina superior izquierda =========
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .zIndex(2f) ,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Botonera(
                    context = context,
                    mapView = mapView,
                    coroutineScope = coroutineScope,
                    isMenuOpen = isMenuOpen,
                    isCoordOpen = isCoordOpen,
                    isFollowingLocation = isFollowingLocation,
                    isPlacingMarker = isPlacingMarker,
                    selectedMarker = selectedMarker,
                    isMeasuringMode = isMeasuringMode,
                    isPickingLocalizacion = isPickingLocation,
                    isMedevacMode = isMedevacMode,
                    isTutelaMode = isTutelaMode,
                )
                Button(onClick = {
                    Log.d("WebSocket", "📤 Enviando mensaje de prueba...")
                    wsClient.sendMessage("""{
  "type": "position",
  "user": "wolvie",
  "point": {
    "type": "Point",
    "coordinates": [${currentLocation.value?.longitude()}, ${currentLocation.value?.latitude()}]
  }
}""")
                }) {
                    Text("Enviar posición")
                }


            }

            LaunchedEffect(mapViewRef.value) {
                mapViewRef.value?.let { mapView ->
                    mapView.mapboxMap.getStyle { _ ->
                        polylineManager.value = mapView.annotations.createPolylineAnnotationManager()
                    }
                }
            }

        }
        //=== FIN Referencia al mapa ==============

        // =========== Menú desplegable inferior ============
        pointAnnotationManager.value?.let { manager ->
            BottomPanelMenu(
                isVisible = isMenuOpen.value,
                mapView = mapViewRef.value,
                markerList = markerList,
                medevacList = medevacList,
                tutelaList = tutelaList,
                isMeasuringMode = isMeasuringMode,
                measuringMarker = measuringMarker,
                polylineManager = polylineManager,
                distantRequest = { point -> measuringMarker.value = point },
                onDismissRequest = { isMenuOpen.value = false },
                onOptionSelected = { selected ->
                    when (selected) {
                        "Ir" -> {
                            coroutineScope.launch {
                                delay(130)
                                isCoordOpen.value = true
                            }

                        }

                        "Medir" -> {   // 👈 nueva opción que disparará desde Topografía
                            coroutineScope.launch {
                                delay(130)
                                isMenuOpen.value = false
                                isMeasuringMode.value = true
                            }
                        }

                        "Solicitar" -> {
                            coroutineScope.launch {
                                delay(130)
                                isMenuOpen.value = false
                                isMedevacMode.value = true
                            }
                        }

                        "Informar" -> {
                            coroutineScope.launch {
                                delay(130)
                                isMenuOpen.value = false
                                isTutelaMode.value = true
                            }
                        }

                        "Cerrar" -> {
                            isMenuOpen.value = false
                        }
                    }
                },
                onStyleSelected = { style ->
                    currentStyle.value = style
                },
                onMarkerSelected = { markerOpt ->
                    val dr = AppCompatResources.getDrawable(context, markerOpt.iconRes)
                    selectedMarkerBitmap.value =
                        dr?.toBitmap(width = iconSizePx, height = iconSizePx)

                    selectedMarkerOption.value = markerOpt

                    isPlacingMarker.value = selectedMarkerBitmap.value != null
                    isMenuOpen.value = false

                },
                annotationManager = pointAnnotationManager.value!!,
            )
        }


        //========= Modo editar marcador ============
        if (isEditingMarker.value != null) {
            isPlacingMarker.value = true
            MarkerPicker(
                onSelect = { option ->
                    isEditingMarker.value?.let { marker ->
                        // Cambiar icono
                        val bmp = AppCompatResources.getDrawable(context, option.iconRes)
                            ?.toBitmap(width = iconSizePx, height = iconSizePx)

                        if (bmp != null) {
                            marker.iconImageBitmap = bmp

                            // ✅ Actualizar en el mapa
                            pointAnnotationManager.value?.update(marker)

                            updateMarkerIconByPoint(
                                point = marker.point,
                                newIcon = bmp,
                                markers = markerList,
                                annotationManager = pointAnnotationManager.value   // ✅ null-safe
                            )

                        }

                        // Cambiar texto opcional
                        marker.setData(JsonPrimitive(option.label))

                    }

                    isEditingMarker.value = null // cerrar picker
                    isPlacingMarker.value = false
                },
                editor = false,
                onDismissRequest = {
                    // 👈 cerrar al pulsar fuera
                    isEditingMarker.value = null
                    isPlacingMarker.value = false
                }
            )

        }

        // ======= Formulario de solicitud de evacuación =======
        if (isMedevacFormOpen.value && medevacPoint.value != null) {
            MedevacFormPanel(
                point = medevacPoint.value!!,
                onDismissRequest = { isMedevacFormOpen.value = false },
                onSubmit = { medevacData ->

                    val mgr = pointAnnotationManager.value ?: return@MedevacFormPanel

                    val medevacIcon = AppCompatResources.getDrawable(context, R.drawable.hospital)
                        ?.toBitmap(width = iconSizePx, height = iconSizePx)

                    var distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        medevacPoint.value!!,
                        TurfConstants.UNIT_METERS
                    )

                    if (medevacIcon != null) {
                        placeMarker(
                            mgr = mgr,
                            bmp = medevacIcon,
                            point = medevacPoint.value!!,
                            id = markerIdCounter.intValue,
                            distance = distance,
                            markerList = markerList,
                            isMedevacMode = isMedevacFormOpen,
                            onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                            medevacList = medevacList,
                            medevacData = medevacData
                        )

                        markerList.add(
                            MarkerData(
                                id = markerIdCounter.intValue,
                                name = "Medevac ${medevacIdCounter.intValue}",
                                createdBy = "Operador",
                                distance = distance,
                                icon = medevacIcon,
                                point = medevacPoint.value!!,
                                type = MarkerType.MEDEVAC,
                                medevac = medevacData   // 👈 ahora sí
                            )
                        )
                        medevacIdCounter.intValue++
                        markerIdCounter.intValue++
                    }

                    isMedevacFormOpen.value = false
                }
            )

        }

        if (isTutelaFormOpen.value && tutelaPoint.value != null) {
            TutelaFormPanel(
                puestoPoint = tutelaPoint.value!!,
                onDismissRequest = {
                    isTutelaFormOpen.value = false
                    locationPoint.value = null  // 👈 resetear al cancelar
                },
                onSubmit = { tutelaData ->
                    val mgr = pointAnnotationManager.value ?: return@TutelaFormPanel

                    val tutelaIcon = AppCompatResources.getDrawable(context, R.drawable.style)
                        ?.toBitmap(width = iconSizePx, height = iconSizePx)

                    val distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        tutelaPoint.value!!,
                        TurfConstants.UNIT_METERS
                    )

                    // Guardamos localización secundaria en el Tutela
                    val updatedTutela = tutelaData.copy(localizacion = locationPoint.value)

                    if (tutelaIcon != null) {
                        placeMarker(
                            mgr = mgr,
                            bmp = tutelaIcon,
                            point = tutelaPoint.value!!,
                            id = markerIdCounter.intValue,
                            distance = distance,
                            markerList = markerList,
                            isTutelaMode = isTutelaFormOpen,
                            onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                            tutelaList = tutelaList,
                            tutelaData = updatedTutela
                        )

                        markerList.add(
                            MarkerData(
                                id = markerIdCounter.intValue,
                                name = "Tutela ${tutelaIdCounter.intValue}",
                                createdBy = "Operador",
                                distance = distance,
                                icon = tutelaIcon,
                                point = tutelaPoint.value!!,
                                type = MarkerType.TUTELA,
                                tutela = updatedTutela
                            )
                        )

                        tutelaIdCounter.intValue++
                        markerIdCounter.intValue++
                    }

                    // ===== MARCADOR SECUNDARIO =====
                    locationPoint.value?.let { loc ->
                        val warningIcon = AppCompatResources.getDrawable(context, R.drawable.warning)
                            ?.toBitmap(width = iconSizePx, height = iconSizePx)

                        val distance2 = TurfMeasurement.distance(
                            currentLocation.value!!,
                            loc,
                            TurfConstants.UNIT_METERS
                        )

                        if (warningIcon != null) {
                            placeMarker(
                                mgr = mgr,
                                bmp = warningIcon,
                                point = loc,
                                id = warningIdCounter.intValue,
                                distance = distance2,
                                markerList = markerList,
                                isTutelaMode = isTutelaFormOpen,
                                onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                                tutelaList = tutelaList,
                                tutelaData = updatedTutela
                            )

                            markerList.add(
                                MarkerData(
                                    id = markerIdCounter.intValue,
                                    name = "Observación ${warningIdCounter.intValue}",
                                    createdBy = "Tutela ${tutelaIdCounter.intValue}",
                                    distance = distance2,
                                    icon = warningIcon,
                                    point = loc,
                                    type = MarkerType.TUTELA,
                                    tutela = updatedTutela
                                )
                            )
                            warningIdCounter.intValue++
                            markerIdCounter.intValue++
                        }
                    }

                    // ✅ al enviar, cerrar y limpiar selección secundaria
                    isTutelaFormOpen.value = false
                    locationPoint.value = null
                },
                onPickLocation = {
                    isTutelaFormOpen.value = false // cierra panel temporalmente
                    isPickingLocation.value = true // activa modo selección
                },
                selectedLocalizacion = locationPoint,
                isPickingLocalizacion = isPickingLocation
            )
        }


        //========= Icono Cancelar Marcador ============
        if (
            isPlacingMarker.value ||
            isEditingMarker.value != null ||
            isMeasuringMode.value ||
            isMedevacMode.value ||
            isTutelaMode.value ||
            isPickingLocation.value
            ) {
            MarkerCancel(
                onCancel = {
                    isPlacingMarker.value = false
                    selectedMarkerBitmap.value = null
                    isEditingMarker.value = null
                    measuringTarget.value = null
                    isMedevacMode.value = false
                    isTutelaMode.value = false
                    // limpiar línea de distancia
                    mapViewRef.value?.annotations?.createPolylineAnnotationManager()?.deleteAll()
                },
                onDismissRequest = {
                    isEditingMarker.value = null
                    isMeasuringMode.value = false
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 96.dp, end = 29.dp)
            )
        }

    }// =========== Fin Contenedor Principal ===========

    //========== Efectos ============
    LaunchedEffect(isMenuOpen.value) {
        if (isMenuOpen.value) {
            delay(50)
            isCoordOpen.value = false
            showUTMBox.value = false
        } else {
            delay(120) // ⏳ Retardo de 300 ms antes de reaparecer
            showUTMBox.value = true
        }
    }

    LaunchedEffect(isFollowingLocation.value) {
        locationUpdatesFlow(context).collect { loc ->
            if (loc != null) {
                val point = Point.fromLngLat(loc.longitude, loc.latitude)
                currentLocation.value = point

                if (isFollowingLocation.value) {
                    // Mantener zoom actual (no forzar siempre 16.0)
                    val currentZoom = mapViewRef.value?.mapboxMap?.cameraState?.zoom ?: 16.0

                    mapViewRef.value?.mapboxMap?.easeTo(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(currentZoom)
                            .build(),
                        MapAnimationOptions.mapAnimationOptions {
                            duration(500) // movimiento suave
                        }
                    )

                    // Aseguramos que la flecha se oriente al heading
                    mapViewRef.value?.location?.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                        puckBearing = PuckBearing.HEADING
                        puckBearingEnabled = true
                    }
                }
            }
        }
    }

    //Prueba orientacion
    LaunchedEffect(Unit) {
        context.orientationFlow().collect { azimuth ->
            heading.floatValue = azimuth
        }
    }

    val view = LocalView.current

    LaunchedEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowInsetsControllerCompat(window, view)

        // Ocultar barra de estado
        controller.hide(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.systemBars()
        )

        // Para que vuelva con gesto y no sea permanente
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    DoubleBackToExitApp()


    LaunchedEffect(Unit) {
        wsClient.connect("192.168.1.39", 8080) // IP de tu Ubuntu Server
    }

// Ejemplo: enviar tu posición


}
