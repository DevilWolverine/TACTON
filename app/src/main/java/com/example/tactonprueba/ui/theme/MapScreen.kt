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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.tactonprueba.utils.ToolBar
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
import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

// WebSocket ======================================================================================
data class PositionMessage(
    val type: String,
    val user: String,
    val point: Point,
    val bearing: Double? = null
)


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MapScreen() {
    // Variables ===================================================================================
    // Mapeado =====================================================================================
    val coroutineScope = rememberCoroutineScope()
    val currentLocation = remember { mutableStateOf<Point?>(null) }
    val currentStyle = remember { mutableStateOf(MapInit.NORMAL) }
    val heading = remember { mutableFloatStateOf(0f) }
    val locationPoint = remember { mutableStateOf<Point?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Dibujar en mapa =============================================================================
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconSizePx = with(density) { 48.dp.toPx().toInt() }
    val pointAnnotationManager = remember { mutableStateOf<PointAnnotationManager?>(null) }
    val selectedMarker = remember { mutableStateOf<PointAnnotation?>(null) }
    val selectedMarkerBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val selectedMarkerOption = remember { mutableStateOf<MarkerOption?>(null) }
    val sizePx = with(LocalDensity.current) { 32.dp.toPx().toInt() }
    val view = LocalView.current
    var polylineManager = remember { mutableStateOf<PolylineAnnotationManager?>(null) }

    // Iconos por defecto ==========================================================================
    val defaultMarkerBitmap = AppCompatResources.getDrawable(context, R.drawable.pin)
        ?.toBitmap(width = iconSizePx, height = iconSizePx)!!
    val navBitmap = AppCompatResources.getDrawable(context, R.drawable.nav)
        ?.toBitmap(width = sizePx, height = sizePx)!!
    val medevacIcon = AppCompatResources.getDrawable(context, R.drawable.hospital)
        ?.toBitmap(width = iconSizePx, height = iconSizePx)
    val tutelaIcon = AppCompatResources.getDrawable(context, R.drawable.style)
        ?.toBitmap(width = iconSizePx, height = iconSizePx)
    val warningIcon = AppCompatResources.getDrawable(context, R.drawable.warning)
        ?.toBitmap(width = iconSizePx, height = iconSizePx)

    // Estados =====================================================================================
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
    val isUTMVisible = remember { mutableStateOf(true) }
    var isPlacingMarker = remember { mutableStateOf(false) }

    // Manejo de listas ============================================================================
    val markerIdCounter = remember { mutableIntStateOf(1) }
    val markerList = remember { mutableStateListOf<MarkerData>() }
    val measuringMarker = remember { mutableStateOf<Point?>(null) }
    val measuringTarget = remember { mutableStateOf<PointAnnotation?>(null) }
    val medevacIdCounter = remember { mutableIntStateOf(1) }
    val medevacList = remember { mutableStateListOf<MedevacData?>() }
    val medevacPoint = remember { mutableStateOf<Point?>(null) }
    val tutelaIdCounter = remember { mutableIntStateOf(1) }
    val tutelaList = remember { mutableStateListOf<TutelaData?>() }
    val tutelaPoint = remember { mutableStateOf<Point?>(null) }
    val warningIdCounter = remember { mutableIntStateOf(1) }

    // WebSocket ===================================================================================
    val remoteMarker = remember { mutableStateOf<PointAnnotation?>(null) }
    // WebSocket ===================================================================================
    val remoteUsers = remember { mutableStateMapOf<String, Pair<Point, Double>>() }
    val userSourceRef = remember { mutableStateOf<GeoJsonSource?>(null) }
// Guardamos jobs activos por usuario
    val animationJobs = remember { mutableMapOf<String, Job>() }

    val wsClient = remember {
        WebSocketClient { msg: PositionMessage ->
            if (msg.type == "position") {
                val oldPoint = remoteUsers[msg.user]?.first
                val oldBearing = remoteUsers[msg.user]?.second ?: (msg.bearing ?: 0.0)
                val newPoint = msg.point
                val newBearing = msg.bearing ?: 0.0

                // Cancelar animación previa de este usuario si existe
                animationJobs[msg.user]?.cancel()

                if (oldPoint != null) {
                    // 🔹 Animación frame-by-frame con easing
                    animationJobs[msg.user] = CoroutineScope(Dispatchers.Main).launch {
                        val duration = 300L
                        val startTime = System.currentTimeMillis()
                        while (isActive) {
                            val t = (System.currentTimeMillis() - startTime).toFloat() / duration
                            val fraction = min(1f, t) // clamp [0..1]

                            // Easing tipo smoothstep
                            val eased = fraction * fraction * (3 - 2 * fraction)

                            // Interpolar posición
                            val lat = oldPoint.latitude() +
                                    (newPoint.latitude() - oldPoint.latitude()) * eased
                            val lng = oldPoint.longitude() +
                                    (newPoint.longitude() - oldPoint.longitude()) * eased
                            val interpolatedPoint = Point.fromLngLat(lng, lat)

                            // Interpolar bearing con shortest path
                            val interpolatedBearing = interpolateBearing(oldBearing, newBearing, eased)

                            // Guardar valores interpolados
                            remoteUsers[msg.user] = interpolatedPoint to interpolatedBearing

                            // Reconstruir features y refrescar el GeoJsonSource
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
                            delay(16) // ~60 fps
                        }
                    }
                    cameraMove(mapViewRef.value, newPoint)
                } else {
                    // 🔹 Primer punto recibido (sin animación)
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
        }
    }


    // Fin Variables ===============================================================================

    // Contenedor Principal ========================================================================
    Box(modifier = Modifier.fillMaxSize()) {

        // Vista Mapa ==================================================================================
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    MapInit.provideAccessToken(ctx)
                    compass.enabled = false
                    scalebar.enabled = false
                    attribution.enabled = false

                    // Configuración inicial del puck
                    location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                        pulsingColor = 1
                        locationPuck = LocationPuck2D(
                            topImage = navBitmap?.let { ImageHolder.from(it) },
                            bearingImage = navBitmap?.let { ImageHolder.from(it) }
                        )
                        puckBearing = PuckBearing.HEADING
                        puckBearingEnabled = true
                    }

                    mapViewRef.value = this
                    pointAnnotationManager.value = annotations.createPointAnnotationManager()
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                // 👇 Aquí ya NO cargas el estilo
                // puedes dejarlo vacío o meter ajustes menores, por ejemplo:
                if (!isCenteredMap.value) {
                    coroutineScope.launch {
                        centerMapOnUserLocation(context, mapView)
                        isCenteredMap.value = true
                    }
                }
            }
        )

        /*AndroidView(
    factory = { ctx ->
        MapView(ctx).apply {
            // Configuración inicial mapeado mapbox
            MapInit.provideAccessToken(ctx)
            compass.enabled = false
            scalebar.enabled = false
            attribution.enabled = false
            // Configuración de la flecha de orientación
            location.updateSettings {
                enabled = true
                pulsingEnabled = true
                pulsingColor = 1
                locationPuck = LocationPuck2D(
                    topImage = navBitmap?.let { ImageHolder.from(it) },
                    bearingImage = navBitmap?.let { ImageHolder.from(it) }
                )
                puckBearing = PuckBearing.HEADING
                puckBearingEnabled = true
            }

            mapViewRef.value = this
            pointAnnotationManager.value = annotations.createPointAnnotationManager()
        }
    },
    modifier = Modifier.fillMaxSize(),
    update = { mapView ->
        // Cargar estilo de mapa
        mapView.mapboxMap.loadStyle(currentStyle.value)
        // Centrado de mapa
        if (!isCenteredMap.value) {
            coroutineScope.launch {
                centerMapOnUserLocation(context, mapView)
                isCenteredMap.value = true
            }
        }
    }


)*/

// Referencia Mapa =============================================================================
        mapViewRef.value?.let { mapView ->

// Componentes =================================================================================
// Brújula  ====================================================================================
            CustomCompass(
                context = context,
                mapView = mapView,
                heading = heading.floatValue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )

// Pantalla Introducción Coordenadas ===========================================================
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

// Caja Coordenadas ============================================================================
            if (isUTMVisible.value) {
                UTMCoordinateBox(
                    location = currentLocation.value,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

// Distancia Entre Puntos (Marcadores) =========================================================
            if (measuringMarker.value != null && currentLocation.value != null) {
                // Ubicación del dispositivo (Usuario)
                val userPoint = Point.fromLngLat(
                    currentLocation.value!!.longitude(),
                    currentLocation.value!!.latitude()
                )
                // Ubicación del marcador
                val markerPoint = measuringMarker.value!!

                // Cálculo de distancia
                val distance = TurfMeasurement.distance(
                    userPoint,
                    markerPoint,
                    TurfConstants.UNIT_METERS
                )

                // Dibujo línea de unión
                drawDistanceLine(polylineManager.value, userPoint, markerPoint)

                //Componente de distancia
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

// Menú/Informe Marcador Seleccionado ==========================================================
            selectedMarker.value?.let { marker ->
                val markerData = markerList.find { it.point == marker.point }
                if (markerData != null) {
                    // Muestra los componentes según el tipo de marcador y centra el mapa
                    // 3 Tipos de marcadores: NORMAL, MEDEVAC, TUTELA
                    // 1 Menú (NORMAL) y 3 Informes (1 MEDEVAC, 2 TUTELA)
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
                    } else if (markerData.type == MarkerType.MEDEVAC &&
                        markerData.medevac != null
                    ) {
                        cameraMove(mapView, markerData.point)
                        MedevacReportPanel(
                            medevac = markerData.medevac,
                            onDismiss = { selectedMarker.value = null },

                            )
                    } else if (markerData.type == MarkerType.TUTELA &&
                        markerData.tutela != null
                    ) {
                        cameraMove(mapView, markerData.point)
                        TutelaReportPanel(
                            tutela = markerData.tutela,
                            selectedMarker = marker,
                            onDismiss = { selectedMarker.value = null },
                        )
                    }
                }
            }

// ToolBar Superior ============================================================================
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .zIndex(2f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ToolBar(
                    context = context,
                    mapView = mapViewRef.value,
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

            }

// Menú Desplegable ============================================================================
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

                        // Manejo de opciones especificas
                        when (selected) {

                            "Ir" -> {
                                coroutineScope.launch {
                                    delay(130)
                                    isCoordOpen.value = true
                                }

                            }

                            "Medir" -> {
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


// Modo Editar Marcador ========================================================================
            if (isEditingMarker.value != null) {
                isPlacingMarker.value = true
                MarkerPicker(
                    onSelect = { option ->
                        isEditingMarker.value?.let { marker ->
                            // Actualiza el icono
                            val bmp = AppCompatResources.getDrawable(context, option.iconRes)
                                ?.toBitmap(width = iconSizePx, height = iconSizePx)

                            if (bmp != null) {
                                marker.iconImageBitmap = bmp

                                // Actualiza el mapa
                                pointAnnotationManager.value?.update(marker)

                                // Actualiza el marcador seleccionado
                                updateMarkerIconByPoint(
                                    point = marker.point,
                                    newIcon = bmp,
                                    markers = markerList,
                                    annotationManager = pointAnnotationManager.value
                                )

                            }

                            // Actualiza el nombre del marcador
                            marker.setData(JsonPrimitive(option.label))

                        }

                        // Sale del modo marcador
                        isEditingMarker.value = null
                        isPlacingMarker.value = false
                    },
                    editor = false,
                    onDismissRequest = {
                        // Cierre del panel de edición
                        isEditingMarker.value = null
                        isPlacingMarker.value = false
                    }
                )

            }

// Formulario Medevac ==========================================================================
            if (isMedevacFormOpen.value && medevacPoint.value != null) {
                MedevacFormPanel(
                    point = medevacPoint.value!!,
                    onDismissRequest = { isMedevacFormOpen.value = false },
                    onSubmit = { medevacData ->
                        val mgr = pointAnnotationManager.value ?: return@MedevacFormPanel

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

                            // Añade el icono a la lista
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
                            // Actualización de contadores
                            medevacIdCounter.intValue++
                            markerIdCounter.intValue++
                        }
                        // Cierre de formulario
                        isMedevacFormOpen.value = false
                    }
                )

            }

// Formulario Tutela ===========================================================================
            if (isTutelaFormOpen.value && tutelaPoint.value != null) {
                TutelaFormPanel(
                    puestoPoint = tutelaPoint.value!!,
                    onDismissRequest = {
                        isTutelaFormOpen.value = false
                        locationPoint.value = null  // 👈 resetear al cancelar
                    },
                    onSubmit = { tutelaData ->
                        val mgr = pointAnnotationManager.value ?: return@TutelaFormPanel

                        val distance = TurfMeasurement.distance(
                            currentLocation.value!!,
                            tutelaPoint.value!!,
                            TurfConstants.UNIT_METERS
                        )

                        // Guardamos la localización del puesto observado
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

                        // Crea el marcador de observación
                        locationPoint.value?.let { loc ->

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

                        // Cierre del formulario
                        isTutelaFormOpen.value = false
                        locationPoint.value = null
                    },
                    onPickLocation = {
                        isTutelaFormOpen.value = false
                        isPickingLocation.value = true
                    },
                    selectedLocalizacion = locationPoint,
                )
            }


// Cancelar Colocación Marcador ================================================================
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
                        mapViewRef.value?.annotations?.createPolylineAnnotationManager()
                            ?.deleteAll()
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
// FIN Componentes =============================================================================


// Efectos Para Componentes ====================================================================
// Distancia Entre Puntos (Mapa) ===============================================================
            DisposableEffect(mapViewRef.value, isMeasuringMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                // Cuando se activa escucha el toque en el mapa
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isMeasuringMode.value) return@OnMapClickListener false

                    val mgr = pointAnnotationManager.value ?: return@OnMapClickListener false

                    // Calcula la distancia entre el punto y la ubicación
                    var distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        point,
                        TurfConstants.UNIT_METERS
                    )

                    // Crea un marcador por defecto y lo coloca en el punto
                    val bmp = defaultMarkerBitmap
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

                    // Conteo de marcadores
                    markerIdCounter.intValue++

                    // Inicio de medición medir
                    measuringMarker.value = point

                    // Salir del modo medir
                    isMeasuringMode.value = false
                    true
                }

                if (isMeasuringMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

// Modo colocar marcador =======================================================================
            DisposableEffect(mapViewRef.value, isPlacingMarker.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isPlacingMarker.value) return@OnMapClickListener false

                    // Recoge el icono seleccionado
                    val bmp = selectedMarkerBitmap.value ?: return@OnMapClickListener false
                    val mgr = pointAnnotationManager.value ?: return@OnMapClickListener false

                    var distance = TurfMeasurement.distance(
                        currentLocation.value!!,
                        point,
                        TurfConstants.UNIT_METERS
                    )

                    // Crea marcador
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

                    // Salir del modo colocar
                    isPlacingMarker.value = false
                    selectedMarkerBitmap.value = null
                    true
                }

                if (isPlacingMarker.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

// Modo Medevac ================================================================================
            DisposableEffect(mapViewRef.value, isMedevacMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isMedevacMode.value) return@OnMapClickListener false

                    // Guardamos la ubicación seleccionada
                    medevacPoint.value = point

                    // Abrimos el formulario
                    isMedevacFormOpen.value = true

                    // Salir del modo medevac
                    isMedevacMode.value = false
                    true
                }

                if (isMedevacMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

// Modo Tutela Puesto Observación ==============================================================
            DisposableEffect(mapViewRef.value, isTutelaMode.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isTutelaMode.value) return@OnMapClickListener false

                    tutelaPoint.value = point

                    // Abrimos el formulario
                    isTutelaFormOpen.value = true

                    // Salir del modo tutela
                    isTutelaMode.value = false

                    true
                }

                if (isTutelaMode.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }

// Modo Tutela Punto Observado =================================================================

            DisposableEffect(mapViewRef.value, isPickingLocation.value) {
                val mapView = mapViewRef.value ?: return@DisposableEffect onDispose {}
                val listener = com.mapbox.maps.plugin.gestures.OnMapClickListener { point ->
                    if (!isPickingLocation.value) return@OnMapClickListener false

                    // Guardamos la localización seleccionada
                    locationPoint.value = point

                    // Salir del modo punto observado
                    isPickingLocation.value = false

                    // Reabrimos formulario
                    isTutelaFormOpen.value = true

                    true
                }

                if (isPickingLocation.value) mapView.gestures.addOnMapClickListener(listener)
                onDispose { mapView.gestures.removeOnMapClickListener(listener) }
            }
// FIN Efectos Para Componentes ================================================================

        }
// FIN Referencia al mapa ======================================================================

    }
// FIN Contenedor Principal ====================================================================

// Efectos =====================================================================================
// Retraso de aparición del cajón de coordenadas ===============================================
    LaunchedEffect(isMenuOpen.value) {
        if (isMenuOpen.value) {
            delay(50)
            isCoordOpen.value = false
            isUTMVisible.value = false
        } else {
            delay(120) // ⏳ Retardo de 300 ms antes de reaparecer
            isUTMVisible.value = true
        }
    }

// Actualización de posición ===================================================================
    LaunchedEffect(isFollowingLocation.value) {
        locationUpdatesFlow(context).collect { loc ->
            if (loc != null) {
                val point = Point.fromLngLat(loc.longitude, loc.latitude)
                currentLocation.value = point

                // 🔹 Enviar posición por WebSocket en cada actualización
                val myUser = "Wolf" // 👉 aquí defines tu nombre
                val msg = PositionMessage(
                    type = "position",
                    user = myUser,
                    point = point,
                    bearing = heading.floatValue.toDouble()
                )
                wsClient.sendMessage(Gson().toJson(msg))

                if (isFollowingLocation.value) {
                    // Mantener zoom actual
                    val currentZoom = mapViewRef.value?.mapboxMap?.cameraState?.zoom ?: 16.0

                    mapViewRef.value?.mapboxMap?.easeTo(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(currentZoom)
                            .build(),
                        MapAnimationOptions.mapAnimationOptions {
                            // Animación suave
                            duration(500)
                        }
                    )

                    // Activa la orientación desde el dispositivo
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

    /*LaunchedEffect(isFollowingLocation.value) {
locationUpdatesFlow(context).collect { loc ->
    if (loc != null) {
        val point = Point.fromLngLat(loc.longitude, loc.latitude)
        currentLocation.value = point

        if (isFollowingLocation.value) {
            // Mantener zoom actual
            val currentZoom = mapViewRef.value?.mapboxMap?.cameraState?.zoom ?: 16.0

            mapViewRef.value?.mapboxMap?.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(currentZoom)
                    .build(),
                MapAnimationOptions.mapAnimationOptions {
                    // Animación suave
                    duration(500)
                }
            )

            // Activa la orientación desde el dispositivo
            mapViewRef.value?.location?.updateSettings {
                enabled = true
                pulsingEnabled = true
                puckBearing = PuckBearing.HEADING
                puckBearingEnabled = true
            }
        }
    }
}
}*/

// Mantiene la orientación del dispositivo =====================================================
    LaunchedEffect(Unit) {
        context.orientationFlow().collect { azimuth ->
            heading.floatValue = azimuth
        }
    }

// Ocultar barra de estado =====================================================================
    LaunchedEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowInsetsControllerCompat(window, view)

        controller.hide(
            WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars() or
                    WindowInsetsCompat.Type.systemBars()
        )

// Para que vuelva con gesto y no sea permanente
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

// Iteracción con marcadores ===================================================================
    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.let { mapView ->
            pointAnnotationManager.value = mapView.annotations.createPointAnnotationManager()
        }
    }

// Interacción con el estilo del mapa ==========================================================
    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.let { mapView ->
            mapView.mapboxMap.getStyle { _ ->
                polylineManager.value = mapView.annotations.createPolylineAnnotationManager()
            }
        }
    }


// Conexión WebSocket ==========================================================================
    LaunchedEffect(Unit) {
        wsClient.connect("192.168.1.32", 8080) // IP de tu Ubuntu Server
// 🔹 Identifícate con tu usuario
        val myUser = "Wolf" // 👉 aquí pones tu nombre
        val initMsg = """{"type":"hello","user":"$myUser"}"""
        wsClient.sendMessage(initMsg)
    }

// Enviar posición =============================================================================

// Doble acción para cerrar aplicación =========================================================
    DoubleBackToExitApp()

// 🔹 Cargar estilo una sola vez
    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.mapboxMap?.loadStyle(currentStyle.value) { style ->

            // 🔹 Crear GeoJsonSource vacío
            val source = GeoJsonSource.Builder("remote-users-source")
                .featureCollection(FeatureCollection.fromFeatures(emptyList()))
                .build()
            style.addSource(source)
            userSourceRef.value = source  // 👈 guardamos referencia global

            // 🔹 Añadir icono por defecto
            style.addImage("remote-user-icon", navBitmap)

            // 🔹 Añadir capa para usuarios
            val symbolLayer = com.mapbox.maps.extension.style.layers.generated.SymbolLayer(
                "remote-users-layer",
                "remote-users-source"
            ).apply {
                iconImage("remote-user-icon")
                iconAllowOverlap(true)
                iconIgnorePlacement(true)
                textField(get("user"))   // mostrar el nombre del usuario
                textOffset(listOf(0.0, 1.5))
                textSize(12.0)
                iconRotate(get("bearing"))
            }
            style.addLayer(symbolLayer)
        }
    }


}

private fun CoroutineScope.interpolateBearing(oldBearing: Double, newBearing: Double, fraction: Float): Double {
    var delta = (newBearing - oldBearing + 540) % 360 - 180
    return (oldBearing + delta * fraction + 360) % 360
}

