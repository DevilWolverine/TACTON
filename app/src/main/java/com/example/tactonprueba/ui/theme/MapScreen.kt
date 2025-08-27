package com.example.tactonprueba.ui.theme

import com.example.tactonprueba.network.PositionMessage
import com.example.tactonprueba.network.WebSocketClient
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.*
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
import com.example.tactonprueba.network.MapStyleConfig
import com.example.tactonprueba.network.MarkerMessage
import com.example.tactonprueba.network.WebSocketConfig
import com.example.tactonprueba.network.WebSocketHolder
import com.example.tactonprueba.network.bitmapToBase64
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
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val remoteUsers = remember { mutableStateMapOf<String, Pair<Point, Double>>() }
    val userSourceRef = remember { mutableStateOf<GeoJsonSource?>(null) }

    val wsConfig = remember {
        WebSocketConfig(
            remoteUsers = remoteUsers,
            userSourceRef = userSourceRef,
            coroutineScope = coroutineScope
        )
    }

    val wsClient = remember {
        WebSocketClient { rawMsg ->
            wsConfig.handleIncomingRawMessage(
                rawMsg = rawMsg,
                pointAnnotationManager = pointAnnotationManager.value,
                defaultMarkerBitmap = defaultMarkerBitmap,
                markerList = markerList,
                medevacList = medevacList,
                tutelaList = tutelaList,
                medevacIcon = medevacIcon,
                isMedevacMode = isMedevacMode,
                tutelaIcon = tutelaIcon,
                warningIcon = warningIcon,
                isTutelaMode = isTutelaMode,
                markerIdCounter = markerIdCounter,
                measuringMarker = measuringMarker,
                polylineManager = polylineManager,
                medevacIdCounter = medevacIdCounter,
                tutelaIdCounter = tutelaIdCounter,
                waringIdCounter = warningIdCounter,
                currentLocation = currentLocation,
                selectedMarker = selectedMarker,
                onMarkerClicked = { clicked ->
                    selectedMarker.value = clicked
                }
            )
        }
    }.also {
        WebSocketHolder.wsClient = it
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
                            topImage = navBitmap.let { ImageHolder.from(it) },
                            bearingImage = navBitmap.let { ImageHolder.from(it) }
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
                if (!isCenteredMap.value) {
                    coroutineScope.launch {
                        centerMapOnUserLocation(context, mapView)
                        isCenteredMap.value = true
                    }
                }
            }
        )

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
                wsClient = wsClient,
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

                        if (medevacIcon != null) {
                            placeMarker(
                                mgr = mgr,
                                bmp = medevacIcon,
                                point = medevacPoint.value!!,
                                id = markerList.size + 1,
                                currentLocation = currentLocation,
                                markerList = markerList,
                                isMedevacMode = isMedevacFormOpen,
                                onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                                medevacList = medevacList,
                                medevacData = medevacData
                            )

                            // Añade el icono a la lista
                            markerList.add(
                                MarkerData(
                                    id = markerList.size + 1,
                                    name = "Medevac ${medevacList.size}",
                                    createdBy = "Operador",
                                    distance = medevacList.last()?.distancia,
                                    icon = medevacIcon,
                                    point = medevacPoint.value!!,
                                    type = MarkerType.MEDEVAC,
                                    medevac = medevacData   // 👈 ahora sí
                                )
                            )

                            val markerMsg = MarkerMessage(
                                id = markerList.last().point,
                                type = "create",
                                user = "Devil",
                                marker = markerList.last(),
                                medevac = medevacList.last()
                            )

                            wsClient.sendMessage(Gson().toJson(markerMsg))

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

                        // Guardamos la localización del puesto observado
                        val updatedTutela = tutelaData.copy(localizacion = locationPoint.value)

                        if (tutelaIcon != null) {
                            placeMarker(
                                mgr = mgr,
                                bmp = tutelaIcon,
                                point = tutelaPoint.value!!,
                                id = markerList.size +1,
                                markerList = markerList,
                                isTutelaMode = isTutelaFormOpen,
                                onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                                tutelaList = tutelaList,
                                tutelaData = tutelaData,
                                currentLocation = currentLocation
                            )

                            val aux = if (tutelaList.isEmpty()) tutelaList.size+1 else ((tutelaList.size)/2)

                            markerList.add(
                                MarkerData(
                                    id = markerList.size + 1,
                                    name = "Tutela ${(tutelaList.size+1)/2}",
                                    createdBy = "Operador",
                                    distance = tutelaList.last()?.distancia,
                                    icon = tutelaIcon,
                                    point = tutelaPoint.value!!,
                                    type = MarkerType.TUTELA,
                                    tutela = updatedTutela
                                )
                            )

                            val markerMsg = MarkerMessage(
                                id = markerList.last().point,
                                type = "create",
                                user = "Devil",
                                marker = markerList.last(),
                                tutela = tutelaList.last()

                            )

                            wsClient.sendMessage(Gson().toJson(markerMsg))
                        }

                        // Crea el marcador de observación
                        locationPoint.value?.let { loc ->

                            if (warningIcon != null) {
                                placeMarker(
                                    mgr = mgr,
                                    bmp = warningIcon,
                                    point = loc,
                                    id = markerList.size +1,
                                    currentLocation = currentLocation,
                                    markerList = markerList,
                                    isTutelaMode = isTutelaFormOpen,
                                    onMarkerClicked = { clicked -> selectedMarker.value = clicked },
                                    tutelaList = tutelaList,
                                    tutelaData = updatedTutela
                                )

                                markerList.add(
                                    MarkerData(
                                        id = markerList.size +1,
                                        name = "Observación ${(tutelaList.size)/2}",
                                        createdBy = "Tutela ${(tutelaList.size)/2}",
                                        distance = tutelaList.last()?.distancia,
                                        icon = warningIcon,
                                        point = loc,
                                        type = MarkerType.TUTELA,
                                        tutela = updatedTutela
                                    )
                                )

                                val markerMsg = MarkerMessage(
                                    id = markerList.last().point,
                                    type = "create",
                                    user = "Devil",
                                    marker = markerList.last(),
                                    tutela = tutelaList.last()

                                )

                                wsClient.sendMessage(Gson().toJson(markerMsg))

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
                        isPickingLocation.value = false
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


                    // Crea un marcador por defecto y lo coloca en el punto
                    val bmp = defaultMarkerBitmap
                    placeMarker(
                        mgr = mgr,
                        bmp = bmp,
                        point = point,
                        id = markerList.size + 1,
                        currentLocation = currentLocation,
                        markerList = markerList,
                        onMarkerClicked = { clicked ->
                            selectedMarker.value = clicked
                        },
                    )

                    val icon = bitmapToBase64(bmp)

                    val markerMsg = MarkerMessage(
                        id = markerList.last().point,
                        type = "create",
                        user = "Devil",
                        icon = icon,
                        marker = markerList.last(),

                        )

                    wsClient.sendMessage(Gson().toJson(markerMsg))

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

                    // Crea marcador
                    placeMarker(
                        mgr = mgr,
                        bmp = bmp,
                        point = point,
                        id = markerList.size +1,
                        currentLocation = currentLocation,
                        markerList = markerList,
                        onMarkerClicked = { clicked ->
                            selectedMarker.value = clicked
                        },
                    )

                    val icon = bitmapToBase64(bmp)

                    val markerMsg = MarkerMessage(
                        id = markerList.last().point,
                        type = "create",
                        user = "Devil",
                        icon = icon,
                        marker = markerList.last(),

                    )

                    wsClient.sendMessage(Gson().toJson(markerMsg))

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
// Retraso de aparición del cajón de coordenadas
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

// Actualización de posición
    LaunchedEffect(isFollowingLocation.value) {
        locationUpdatesFlow(context).collect { loc ->
            if (loc != null) {
                val point = Point.fromLngLat(loc.longitude, loc.latitude)
                currentLocation.value = point

                // 🔹 Enviar posición por WebSocket en cada actualización
                val myUser = "Devil" // 👉 aquí defines tu nombre
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

// Mantiene la orientación del dispositivo
    LaunchedEffect(Unit) {
        context.orientationFlow().collect { azimuth ->
            heading.floatValue = azimuth
        }
    }

// Ocultar barra de estado
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

    // Iteracción con marcadores
    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.let { mapView ->
            pointAnnotationManager.value = mapView.annotations.createPointAnnotationManager()
        }
    }

    // Interacción con el estilo del mapa
    LaunchedEffect(mapViewRef.value) {
        mapViewRef.value?.let { mapView ->
            mapView.mapboxMap.getStyle { _ ->
                polylineManager.value = mapView.annotations.createPolylineAnnotationManager()
            }
        }
    }

    // Conexión WebSocket
    LaunchedEffect(Unit) {
        wsConfig.connectAndIdentify(wsClient, "Devil")  // 👈 delegamos en WebSocketConfig
    }

    // Cerrar aplicación
    DoubleBackToExitApp()

    // Aplicar estilo
    LaunchedEffect(mapViewRef.value, currentStyle.value) {
        mapViewRef.value?.mapboxMap?.loadStyle(currentStyle.value) { style ->
            MapStyleConfig.applyRemoteUsersStyle(style, userSourceRef, navBitmap)
        }
    }

}