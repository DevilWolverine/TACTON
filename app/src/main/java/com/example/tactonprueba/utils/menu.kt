package com.example.tactonprueba.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager

// Componentes =====================================================================================
// Estilo Menú principal ===========================================================================
@Composable
fun MenuCard(pic: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(pic, contentDescription = "Icono", modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, fontSize = 16.sp)
            }

        }
    }
}

// Estilo Submenú ==================================================================================
@Composable
fun SubmenuCard(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = label, fontSize = 16.sp)
        }
    }
}

// Contenedor Menús ================================================================================
@Composable
fun BottomPanelMenu(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onOptionSelected: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    onMarkerSelected: (MarkerOption) -> Unit = {},
    markerList: SnapshotStateList<MarkerData> = mutableStateListOf(),
    mapView: MapView?,
    annotationManager: PointAnnotationManager,
    medevacList: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    tutelaList: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    distantRequest: (Point) -> Unit,
    measuringMarker: MutableState<Point?>,
    isMeasuringMode: MutableState<Boolean>,
    polylineManager: MutableState<PolylineAnnotationManager?>,

    ) {
    val activeSubmenu = remember { mutableStateOf<String?>(null) }
    val backStack = remember { mutableStateListOf<() -> Unit>() }
    var pickerOptions by remember { mutableStateOf<List<MarkerOption>>(emptyList()) }
    var showMarkerList  by remember { mutableStateOf(false) }
    var showMarkerPicker  by remember { mutableStateOf(false) }
    var showMedevacList  by remember { mutableStateOf(false) }
    var showPreferences  by remember { mutableStateOf(false) }
    var showServer by remember { mutableStateOf(false) }
    var showTutelaList  by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Reset del menú
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            activeSubmenu.value = null
            showMarkerPicker = false
            showMarkerList = false
            showMedevacList = false
            showTutelaList = false
            showExitDialog = false
            showPreferences = false
            showServer = false
            pickerOptions = emptyList()
            clearBack(backStack)
        }
    }

    // Animación de aparición
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            activeSubmenu.value = null
                            showMarkerPicker = false
                            clearBack(backStack)
                            onDismissRequest()

                        }
                    )
            )

            // Panel del menú
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .clickable(enabled = false) {}
            ) {
                // Muestra el menú o el submenú
                if (activeSubmenu.value == null) {
                    RenderMainMenu(
                        onSelect = { activeSubmenu.value = it },
                        onOptionSelected = {
                            if (it == "Salir") {
                                onDismissRequest()
                                showExitDialog = true
                            } else {
                                onOptionSelected(it)
                                onDismissRequest()
                            }
                        }
                    )
                } else {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${activeSubmenu.value}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = {
                                if (!popBack(backStack)) {
                                    clearBack(backStack)
                                    activeSubmenu.value = null
                                    showMarkerPicker = false
                                    showMarkerList = false
                                    showMedevacList = false
                                    showTutelaList = false
                                    showPreferences = false
                                    showServer = false
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ArrowBackIosNew,
                                contentDescription = "Cerrar",
                                modifier = Modifier.size(15.dp)
                            )

                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    RenderSubMenu(
                        submenu = activeSubmenu.value,
                        markerList = markerList,
                        medevacList = medevacList,
                        tutelaList = tutelaList,
                        polylineManager = polylineManager,
                        measuringMarker = measuringMarker,
                        isMeasuringMode = isMeasuringMode,
                        onStyleSelected = { style ->
                            onStyleSelected(style)
                            activeSubmenu.value = null
                            onDismissRequest()
                        },
                        onOptionSelected = { option ->
                            onOptionSelected(option)
                            activeSubmenu.value = null
                            onDismissRequest()
                        },
                        showMarkerPicker = showMarkerPicker,

                        showMarkerList = showMarkerList,
                        showMedevacList = showMedevacList,
                        showTutelaList = showTutelaList,
                        showPreferences = showPreferences,
                        showServer = showServer,
                        setShowTutelaList = { showTutelaList = it },
                        setShowMarkerPicker = { showMarkerPicker = it },
                        setShowMarkerList = { showMarkerList = it },
                        setShowMedevacList = { showMedevacList = it },
                        setShowPreferences = { showPreferences = it },
                        setShowServer = { showServer = it },
                        distantRequest = { distantRequest -> distantRequest(distantRequest) },
                        onMarkerSelected = onMarkerSelected,
                        setPickerOptions = { pickerOptions = it },
                        backStack = backStack,
                        mapView = mapView,
                        annotationManager = annotationManager
                    )

                }
            }

        }
    }
}

// Carga Menú Principal ============================================================================
@Composable
private fun RenderMainMenu(
    onSelect: (String) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            "Herramientas",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        IconButton(onClick = { onOptionSelected("Cerrar") } ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                modifier = Modifier.size(20.dp)
            )

        }
    }

    Spacer(Modifier.height(12.dp))

    // Lista de opciones de los submenús
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { MenuCard(Icons.Default.AutoAwesomeMotion,"Estilo de mapa") { onSelect("Mapas") } }
        item { MenuCard(Icons.Default.AddLocationAlt,"Marcadores") { onSelect("Marcadores") } }
        item { MenuCard(Icons.Default.Landscape,"Topografía") { onSelect("Topografía") } }
        item { MenuCard(Icons.Default.DataExploration,"Ir") { onOptionSelected("Ir") } }
        item { MenuCard(Icons.Default.BusAlert,"Medevac") { onSelect("Medevac") } }
        item { MenuCard(Icons.Default.Style,"Tutela") { onSelect("Tutela") } }
        item { MenuCard(Icons.Default.Construction,"Opciones") { onSelect("Opciones") } }
        item { MenuCard(Icons.Default.AdsClick,"Guía de usuario") { onOptionSelected("Guia") } }
        item { MenuCard(Icons.Default.Cancel,"Salir") { onOptionSelected("Salir") } }
    }
}

// Carga Submenú ===================================================================================
@Composable
fun RenderSubMenu(
    annotationManager: PointAnnotationManager,
    backStack: MutableList<() -> Unit>,
    distantRequest: (Point) -> Unit,
    isMeasuringMode: MutableState<Boolean>,
    mapView: MapView?,
    markerList: SnapshotStateList<MarkerData>,
    measuringMarker: MutableState<Point?>,
    medevacList: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    onMarkerSelected: (MarkerOption) -> Unit,
    onOptionSelected: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    polylineManager: MutableState<PolylineAnnotationManager?>,
    setPickerOptions: (List<MarkerOption>) -> Unit,
    setShowMarkerList: (Boolean) -> Unit,
    setShowMarkerPicker: (Boolean) -> Unit,
    setShowMedevacList: (Boolean) -> Unit,
    setShowPreferences: (Boolean) -> Unit,
    setShowServer: (Boolean) -> Unit,
    setShowTutelaList: (Boolean) -> Unit,
    showMarkerList: Boolean,
    showMarkerPicker: Boolean,
    showMedevacList: Boolean,
    showPreferences: Boolean,
    showServer: Boolean,
    showTutelaList: Boolean,
    submenu: String?,
    tutelaList: SnapshotStateList<TutelaData?> = mutableStateListOf(),
) {

    // Selección de la opción de los submenús
    when (submenu) {
        "Mapas" -> {
            val scroll = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().verticalScroll(scroll)
            ) {
                // Estilos de mapas
                SubmenuCard("Normal") { onStyleSelected(MapInit.NORMAL) }
                SubmenuCard("Topográfico") { onStyleSelected(MapInit.TOPO) }
                SubmenuCard("Satélite") { onStyleSelected(MapInit.SATELLITE) }
                SubmenuCard("Claro") { onStyleSelected(MapInit.LIGHT) }
                SubmenuCard("Oscuro") { onStyleSelected(MapInit.DARK) }
                SubmenuCard("Navegación diurna") { onStyleSelected(MapInit.NAV_DAY) }
                SubmenuCard("Navegación nocturna") { onStyleSelected(MapInit.NAV_NIGHT) }
            }
        }

        "Marcadores" -> {
            val initialized = remember(submenu) { mutableStateOf(false) }
            LaunchedEffect(submenu) {
                if (!initialized.value) {
                    setPickerOptions(defaultMarkerOptions())
                    setShowMarkerPicker(true)
                    initialized.value = true
                }
            }

            if (showMarkerPicker) {
                MarkerPicker(
                    onSelect = { selected ->
                        onMarkerSelected(selected)
                        setShowMarkerPicker(false)
                    }
                )
            }
        }

        "Medevac" -> {

            if (showMedevacList) {
                pushBack(backStack) { setShowMedevacList(false) }
                if (medevacList.isEmpty()) {
                    Text(
                        "No hay solicitudes",
                        modifier = Modifier.padding(16.dp)
                    )

                } else {
                    MedevacList (
                        medevacs = medevacList,
                        mapView = mapView,
                        markers = markerList,
                        polylineManager = polylineManager,
                        measuringMarker = measuringMarker,
                        isMeasuringMode = isMeasuringMode,
                        pointAnnotationManager = annotationManager,
                        onDismissRequest = {
                            onOptionSelected("Cerrar")
                            clearBack(backStack)
                        },
                        distantRequest = { distantRequest -> distantRequest(distantRequest) },
                    )
                }

            } else {
                clearBack(backStack)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubmenuCard("Solicitar evacuación") {
                        onOptionSelected("Solicitar")
                    }
                    SubmenuCard("Historial de evacuaciones") {
                        pushBack(backStack) { setShowMedevacList(false) }
                        setShowMedevacList(true)
                    }
                }
            }
        }

        "Tutela" -> {
            if (showTutelaList) {
                pushBack(backStack) { setShowTutelaList(false) }
                if (tutelaList.isEmpty()) {
                    Text(
                        "No hay informes",
                        modifier = Modifier.padding(16.dp)
                    )

                } else {
                    TutelaList (
                        tutelas = tutelaList,
                        mapView = mapView,
                        markers = markerList,
                        polylineManager = polylineManager,
                        measuringMarker = measuringMarker,
                        isMeasuringMode = isMeasuringMode,
                        pointAnnotationManager = annotationManager,
                        onDismissRequest = {
                            onOptionSelected("Cerrar")
                            clearBack(backStack)
                        },
                        distantRequest = { distantRequest -> distantRequest(distantRequest) },
                    )
                }

            } else {
                clearBack(backStack)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubmenuCard("Nuevo Informe") {
                        onOptionSelected("Informar")
                    }
                    SubmenuCard("Historial de informes") {
                        pushBack(backStack) { setShowTutelaList(false) }
                        setShowTutelaList(true)
                    }
                }
            }
        }

        "Topografía" -> {
            if (showMarkerList) {
                pushBack(backStack) { setShowMarkerList(false) }
                if (markerList.isEmpty()) {
                    Text(
                        "No hay marcadores aún",
                        modifier = Modifier.padding(16.dp)
                    )

                } else {
                    MarkerList(
                        markers = markerList,
                        mapView = mapView,
                        polylineManager = polylineManager,
                        isMeasuringMode = isMeasuringMode,
                        measuringMarker = measuringMarker,
                        annotationManager = annotationManager, // 👈 pásale el manager
                        onDismissRequest = {
                            onOptionSelected("Cerrar")
                            clearBack(backStack)
                        }
                    )
                }

            } else {
                clearBack(backStack)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubmenuCard("Medir entre puntos") {
                        onOptionSelected("Medir")
                    }
                    SubmenuCard("Listado de marcadores") {
                        pushBack(backStack) { setShowMarkerList(false) }
                        setShowMarkerList(true)
                    }
                }
            }
        }

        "Opciones" -> {

            if (showPreferences) {
                pushBack(backStack) { setShowPreferences(false) }
                UserConfigurationScreen()

            } else if (showServer){
                pushBack(backStack) { setShowServer(false) }
                ServerConfigurationScreen()

            } else {
                clearBack(backStack)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubmenuCard("Ajustes de usuario") {
                        pushBack(backStack) { setShowPreferences(false) }
                        setShowPreferences(true)
                    }
                    SubmenuCard("Ajustes de servidor") {
                        pushBack(backStack) { setShowServer(false) }
                        setShowServer(true)
                    }

                }
            }
        }

        else -> {
            Text("Submenú no disponible.")
        }

    }
}

// Menus Finales ===================================================================================
// Menu Final Marcadores ===========================================================================
@Composable
fun MarkerList(
    markers: SnapshotStateList<MarkerData>,
    mapView: MapView?,
    annotationManager: PointAnnotationManager,
    onDismissRequest: () -> Unit,
    isMeasuringMode: MutableState<Boolean>,
    measuringMarker: MutableState<Point?>,
    polylineManager: MutableState<PolylineAnnotationManager?>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(markers.size) { idx ->
            MarkerListItem(
                marker = markers[idx],
                mapView = mapView,
                polylineManager = polylineManager,
                measuringMarker = measuringMarker,
                isMeasuringMode = isMeasuringMode,
                onDismissRequest = onDismissRequest,
                markerList = markers,
                annotationManager = annotationManager
            )
        }
    }
}

// Lista de marcadores =============================================================================
@Composable
fun MarkerListItem(
    marker: MarkerData,
    mapView: MapView?,
    onDismissRequest: () -> Unit,
    markerList: SnapshotStateList<MarkerData>,
    annotationManager: PointAnnotationManager,
    measuringMarker: MutableState<Point?>,
    isMeasuringMode: MutableState<Boolean>,
    polylineManager: MutableState<PolylineAnnotationManager?>,

    ) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),

        ) {

        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = marker.icon!!.asImageBitmap(),
                        contentDescription = "Icono marcador",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(text = marker.name, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "De: ${marker.createdBy}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Distancia: ${"%.1f".format(marker.distance)} m",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            cameraMove(mapView, marker.point)
                            onDismissRequest()
                        }
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    VerticalDivider(color = Color.Red)

                    IconButton(
                        onClick = {
                            if (marker.type == MarkerType.NORMAL) {
                                removeMarkerAndCancelMeasure(
                                    point = marker.point,
                                    markers = markerList,
                                    annotationManager = annotationManager,
                                    measuringMarker = measuringMarker,
                                    polylineManager = polylineManager,
                                )

                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar marcador",
                            tint = if (marker.type == MarkerType.NORMAL) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Menú Final Medevac ==============================================================================
@Composable
fun MedevacList(
    medevacs: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    markers: SnapshotStateList<MarkerData>,
    pointAnnotationManager: PointAnnotationManager,
    mapView: MapView?,
    distantRequest: (Point) -> Unit,
    onDismissRequest: () -> Unit,
    measuringMarker: MutableState<Point?>,
    isMeasuringMode: MutableState<Boolean>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (medevacs.isEmpty()) {
            item {
                Text(
                    "No hay evacuaciones registradas",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            }
        } else {
            items(medevacs.size) { idx ->
                MedevacItem(
                    medevac = medevacs[idx],
                    medevacs = medevacs,
                    markers = markers,
                    mapView = mapView,
                    polylineManager = polylineManager,
                    measuringMarker = measuringMarker,
                    isMeasuringMode = isMeasuringMode,
                    pointAnnotationManager = pointAnnotationManager,
                    distantRequest = { distantRequest -> distantRequest(distantRequest) },
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

// Estilo Medevacs =================================================================================
@Composable
fun MedevacItem(
    medevac: MedevacData?,
    markers: SnapshotStateList<MarkerData>,
    medevacs: SnapshotStateList<MedevacData?>,
    pointAnnotationManager: PointAnnotationManager,
    mapView: MapView?,
    distantRequest: (Point) -> Unit,
    onDismissRequest: () -> Unit,
    isMeasuringMode: MutableState<Boolean>,
    measuringMarker: MutableState<Point?>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
    ) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable {
                medevac?.line1?.let { distantRequest(it) }
                cameraMove(mapView, medevac?.line1)
                onDismissRequest()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado con coordenadas y distancia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MEDEVAC REQUEST",
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Elimincaión de medevacs
                IconButton(
                    onClick = {
                        medevac?.line1?.let {
                            removeMarkerAndCancelMeasure(
                                point = it,
                                markers = markers,
                                annotationManager = pointAnnotationManager,
                                medevacs = medevacs,
                                measuringMarker = measuringMarker,
                                polylineManager = polylineManager,
                            )
                        }
                    }

                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar marcador",
                        tint = Color.Red
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {

                medevac?.line1?.let {
                    Text(
                        text = "L1 = ${"%.5f".format(medevac.line1.latitude())}," +
                                " ${"%.5f".format(medevac.line1.longitude())}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "L2 = ${medevac?.line2 ?: "-"}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    medevac?.distancia?.let {
                        Text(
                            text = "DIST = ${"%.1f".format(it)} m",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            // Función auxiliar para abreviar
            fun abbrev(value: String?): String {
                return if (!value.isNullOrBlank()) value.trim().first().toString() else "-"
            }

            // Datos
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("L3 = ${abbrev(medevac?.line3)} ${medevac?.line3[2]}",
                        color = Color.White, fontSize = 14.sp)

                    Text("L4 = ${abbrev(medevac?.line4)}",
                        color = Color.White, fontSize = 14.sp)

                    Text("L5 = ${abbrev(medevac?.line5)}",
                        color = Color.White, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("L6 = ${abbrev(medevac?.line6)}",
                        color = Color.White, fontSize = 14.sp)

                    Text("L7 = ${abbrev(medevac?.line7)}",
                        color = Color.White, fontSize = 14.sp)

                    Text("L8 = ${abbrev(medevac?.line8)}",
                        color = Color.White, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "L9 = ${
                            if (medevac?.line9 == "Despejado") "-" 
                            else medevac?.line9.orEmpty()
                        }",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Menú Final Tutela ===============================================================================
@Composable
fun TutelaList(
    tutelas: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    markers: SnapshotStateList<MarkerData>,
    pointAnnotationManager: PointAnnotationManager,
    mapView: MapView?,
    distantRequest: (Point) -> Unit,
    onDismissRequest: () -> Unit,
    measuringMarker: MutableState<Point?>,
    isMeasuringMode: MutableState<Boolean>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (tutelas.isEmpty()) {
            item {
                Text(
                    "No hay informes registrados",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            }

        } else {
            items(tutelas.size) { idx ->
                TutelaItem(
                    tutela = tutelas[idx],
                    tutelas = tutelas,
                    markers = markers,
                    mapView = mapView,
                    polylineManager = polylineManager,
                    measuringMarker = measuringMarker,
                    isMeasuringMode = isMeasuringMode,
                    pointAnnotationManager = pointAnnotationManager,
                    distantRequest = { distantRequest(it) },
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

// Estilo Tutela ===================================================================================
@Composable
fun TutelaItem(
    tutela: TutelaData?,
    markers: SnapshotStateList<MarkerData>,
    tutelas: SnapshotStateList<TutelaData?>,
    pointAnnotationManager: PointAnnotationManager,
    mapView: MapView?,
    distantRequest: (Point) -> Unit,
    onDismissRequest: () -> Unit,
    isMeasuringMode: MutableState<Boolean>,
    measuringMarker: MutableState<Point?>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
) {
    val report = tutela?.tipo == TutelaTipo.REPORT

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable {
                tutela?.puesto?.let { distantRequest(it) }
                cameraMove(mapView, tutela?.puesto)
                onDismissRequest()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (report) {
                    Text(
                        text = "INFORME TUTELA",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = "INFORME OBJETIVO",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Eliminación de informes
                IconButton(
                    onClick = {
                        tutela?.puesto?.let {
                            if (report) {
                                removeMarkerAndCancelMeasure(
                                    point = it,
                                    markers = markers,
                                    annotationManager = pointAnnotationManager,
                                    tutelas = tutelas,
                                    measuringMarker = measuringMarker,
                                    polylineManager = polylineManager,

                                )

                            } else {
                                removeMarkerAndCancelMeasure(
                                    point = it,
                                    markers = markers,
                                    annotationManager = pointAnnotationManager,
                                    tutelas = tutelas,
                                    measuringMarker = measuringMarker,
                                    polylineManager = polylineManager,

                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar informe",
                        tint = Color.Red
                    )
                }
            }

            // Coordenada de puesto
            tutela?.puesto?.let {
                Text(
                    text = "PUESTO = ${"%.5f".format(it.latitude())}," +
                            " ${"%.5f".format(it.longitude())}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            // Datos
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("T = ${tutela?.tiempo}", color = Color.White, fontSize = 14.sp)
                Text("U = ${tutela?.unidad}", color = Color.White, fontSize = 14.sp)
                Text("T = ${tutela?.tamanio}", color = Color.White, fontSize = 14.sp)
                Text("E = ${tutela?.equipo}", color = Color.White, fontSize = 14.sp)
                tutela?.localizacion?.let { loc ->
                    Text(
                        "L = ${"%.5f".format(loc.latitude())}," +
                                " ${"%.5f".format(loc.longitude())}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Text("A = ${tutela?.actitud}", color = Color.White, fontSize = 14.sp)
                tutela?.distancia?.let {
                    Text(
                        text = "DIST = ${"%.1f".format(it)} m",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

        }
    }
}
// FIN Componentes =================================================================================

// Funciones Auxiliares ============================================================================
// Son usadas para poder volver atrás en los menús sin que se cierre completo
private fun pushBack(backStack: MutableList<() -> Unit>, handler: () -> Unit) {
    backStack.add(handler)
}

private fun popBack(backStack: MutableList<() -> Unit>): Boolean {
    val lastIndex = backStack.lastIndex
    if (lastIndex < 0) return false
    val handler = backStack[lastIndex]
    backStack.removeAt(lastIndex)
    return try {
        handler.invoke()
        true
    } catch (_: Throwable) {
        true
    }
}

private fun clearBack(backStack: MutableList<() -> Unit>) {
    backStack.clear()
}