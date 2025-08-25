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

@Composable
fun MenuCard(pic: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Para que sea cuadrado
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
    var showMarkerPicker  by remember { mutableStateOf(false) }
    var showMarkerList  by remember { mutableStateOf(false) }
    var showMedevacList  by remember { mutableStateOf(false) }
    var showTutelaList  by remember { mutableStateOf(false) }
    var pickerOptions by remember { mutableStateOf<List<MarkerOption>>(emptyList()) }
    val backStack = remember { mutableStateListOf<() -> Unit>() }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            // 🔄 reset total del estado interno del menú
            activeSubmenu.value = null
            showMarkerPicker = false
            showMarkerList = false
            showMedevacList = false
            showTutelaList = false
            pickerOptions = emptyList()
            clearBack(backStack)
        }
    }

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

            // Panel del menú (3/4 inferior)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) //ESTO ESTABA A 0.75 !!!!!!!!!!!
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .clickable(enabled = false) {} // Evita que se cierre al hacer clic dentro
            ) {
                if (activeSubmenu.value == null) {
                    RenderMainMenu(
                        onSelect = { activeSubmenu.value = it },
                        onOptionSelected = {
                            onOptionSelected(it)
                            onDismissRequest()
                        }
                    )
                } else {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${activeSubmenu.value}", style = MaterialTheme.typography.titleMedium)

                        IconButton(
                            onClick = {
                                if (!popBack(backStack)) {
                                    clearBack(backStack)
                                    activeSubmenu.value = null
                                    showMarkerPicker = false
                                    showMarkerList = false
                                    showMedevacList = false
                                    showTutelaList = false
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
                        setShowTutelaList = { showTutelaList = it },
                        setShowMarkerPicker = { showMarkerPicker = it },
                        setShowMarkerList = { showMarkerList = it },
                        setShowMedevacList = { showMedevacList = it },
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

@Composable
private fun RenderMainMenu(
    onSelect: (String) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        Text("Herramientas", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

        IconButton(onClick = { onOptionSelected("Cerrar") } ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                modifier = Modifier.size(20.dp)
            )

        }
    }
    Spacer(Modifier.height(12.dp))

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
        item { MenuCard(Icons.Default.AdsClick,"Guía de usuario") { onSelect("Guia") } }
        item { MenuCard(Icons.Default.Cancel,"Salir") { onSelect("Salir") } }
    }
}

@Composable
fun RenderSubMenu(
    submenu: String?,
    onStyleSelected: (String) -> Unit,
    onOptionSelected: (String) -> Unit,
    annotationManager: PointAnnotationManager,
    showMarkerPicker: Boolean,
    setShowMarkerPicker: (Boolean) -> Unit,
    onMarkerSelected: (MarkerOption) -> Unit,
    setPickerOptions: (List<MarkerOption>) -> Unit,
    markerList: SnapshotStateList<MarkerData>,
    showMarkerList: Boolean,
    setShowMarkerList: (Boolean) -> Unit,
    backStack: MutableList<() -> Unit>,
    mapView: MapView?,
    showMedevacList: Boolean,
    setShowMedevacList: (Boolean) -> Unit,
    medevacList: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    showTutelaList: Boolean,
    setShowTutelaList: (Boolean) -> Unit,
    tutelaList: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    distantRequest: (Point) -> Unit,
    measuringMarker: MutableState<Point?>,
    isMeasuringMode: MutableState<Boolean>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
) {

    when (submenu) {
        "Mapas" -> {
            val scroll = rememberScrollState()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
                SubmenuCard("Normal") { onStyleSelected(MapInit.NORMAL) }
                SubmenuCard("Satélite") { onStyleSelected(MapInit.SATELLITE) }
                SubmenuCard("Claro") { onStyleSelected(MapInit.LIGHT) }
                SubmenuCard("Oscuro") { onStyleSelected(MapInit.DARK) }
                SubmenuCard("Nav_Dia") { onStyleSelected(MapInit.NAV_DAY) }
                SubmenuCard("Nav_Noche") { onStyleSelected(MapInit.NAV_NIGHT) }
            }
        }

        "Marcadores" -> {
            // 1) Al entrar en "Marcadores" por primera vez, abrir directamente el picker con TODOS
            val initialized = remember(submenu) { mutableStateOf(false) }
            LaunchedEffect(submenu) {
                if (!initialized.value) {
                    setPickerOptions(defaultMarkerOptions())
                    setShowMarkerPicker(true)
                    initialized.value = true
                }
            }

            // 3) UI: mostramos el picker cuando toque (sin submenú intermedio)
            if (showMarkerPicker) {
                MarkerPicker(
                    onSelect = { selected ->
                        onMarkerSelected(selected)
                        setShowMarkerPicker(false) // al cerrar, gracias al back, vuelves al principal (si así lo gestionas)
                    }
                )
            }
        }

        // Puedes seguir añadiendo más submenús:
        "Medevac" -> {

            if (showMedevacList) {
                pushBack(backStack) { setShowMedevacList(false) }
                if (medevacList.isEmpty()) {
                    Text(
                        "No hay marcadores aún",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {

                    MedevacList (
                        medevacs = medevacList,              // 👈 tu SnapshotStateList
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
                        distantRequest = { distantRequest -> distantRequest(distantRequest) },// 👈 pásale el manager


                    )  // 👈 composable que muestra icono, nombre, distancia, etc
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
                        "No hay marcadores aún",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {

                    TutelaList (
                        tutelas = tutelaList,              // 👈 tu SnapshotStateList
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
                        distantRequest = { distantRequest -> distantRequest(distantRequest) },// 👈 pásale el manager


                    )  // 👈 composable que muestra icono, nombre, distancia, etc
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
                        markers = markerList,              // 👈 tu SnapshotStateList
                        mapView = mapView,
                        polylineManager = polylineManager,
                        isMeasuringMode = isMeasuringMode,
                        measuringMarker = measuringMarker,
                        annotationManager = annotationManager, // 👈 pásale el manager
                        onDismissRequest = {
                            onOptionSelected("Cerrar")
                            clearBack(backStack)
                        }
                    )  // 👈 composable que muestra icono, nombre, distancia, etc
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubmenuCard("Configuración general") { }
                SubmenuCard("Ajustes de usuario") {  }
            }
        }

        else -> {
            Text("Submenú no disponible.")
        }
    }
}


// ==================== Menus Finales ====================
// ==================== Menu Final Marcadores ====================
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
            // 👁️ Lo básico visible siempre

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = marker.icon.asImageBitmap(),
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
                                    isMeasuringMode = isMeasuringMode,
                                    measuringMarker = measuringMarker,
                                    polylineManager = polylineManager,
                                    //mapView = mapView
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

// ==================== Menu Final Medevac ====================
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
    val isMedevacMode = remember { mutableStateOf(true) }
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

                IconButton(
                    onClick = {
                        medevac?.line1?.let {
                            removeMarkerAndCancelMeasure(
                                point = it,
                                markers = markers,
                                annotationManager = pointAnnotationManager,
                                medevacs = medevacs,
                                isMedevacMode = isMedevacMode,
                                isMeasuringMode = isMeasuringMode,
                                measuringMarker = measuringMarker,
                                polylineManager = polylineManager,
                                //mapView = mapView
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
                        text = "L1 = ${"%.5f".format(medevac.line1.latitude())}, ${"%.5f".format(medevac.line1.longitude())}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween){
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

            // Resto de líneas (agrupadas en filas)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("L3 = ${abbrev(medevac?.line3)}",
                        color = Color.White, fontSize = 14.sp)
                    Text("L4 = ${abbrev(medevac?.line4)}",
                        color = Color.White, fontSize = 14.sp)
                    Text("L5 = ${abbrev(medevac?.line5)}",
                        color = Color.White, fontSize = 14.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(),
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
                            if (medevac?.line9 == "Clear terrain") "-" else medevac?.line9.orEmpty()
                        }",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                }

            }
        }
    }
}

// ==================== Menu Final Tutela ====================
// ==================== Menu Final Tutela ====================
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
    val isTutelaMode = remember { mutableStateOf(true) }
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
            // === Encabezado ===
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

                IconButton(
                    onClick = {
                        tutela?.puesto?.let {
                            if (report) {
                                removeMarkerAndCancelMeasure(
                                    point = it,
                                    markers = markers,
                                    tutelas = tutelas,
                                    polylineManager = polylineManager,
                                    annotationManager = pointAnnotationManager,
                                    isTutelaMode = isTutelaMode,
                                    isMeasuringMode = isMeasuringMode,
                                    measuringMarker = measuringMarker,

                                )
                            } else {
                                removeMarkerAndCancelMeasure(
                                    point = it,
                                    markers = markers,
                                    tutelas = tutelas,
                                    polylineManager = polylineManager,
                                    annotationManager = pointAnnotationManager,
                                    isTutelaMode = isTutelaMode,
                                    isMeasuringMode = isMeasuringMode,
                                    measuringMarker = measuringMarker,
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

            // === Coordenadas del puesto principal ===
            tutela?.puesto?.let {
                Text(
                    text = "PUESTO = ${"%.5f".format(it.latitude())}, ${"%.5f".format(it.longitude())}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            // === Tiempo + Unidad + Tamaño ===
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
                        "L = ${"%.5f".format(loc.latitude())}, ${"%.5f".format(loc.longitude())}",
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

/*@Composable
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
                    distantRequest = { distantRequest -> distantRequest(distantRequest) },
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

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
    val isTutelaMode = remember { mutableStateOf(true) }
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
            // Encabezado con coordenadas y distancia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INFORME TUTELA",
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = {
                        tutela?.puesto?.let {
                            removeMarkerAndCancelMeasure(
                                point = it,
                                markers = markers,
                                tutelas = tutelas,
                                polylineManager = polylineManager,
                                annotationManager = pointAnnotationManager,
                                isTutelaMode = isTutelaMode,
                                isMeasuringMode = isMeasuringMode,
                                measuringMarker = measuringMarker,

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
                tutela?.tiempo?.let {
                    Text(
                        text = "T = ${tutela?.tiempo}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "U = ${tutela?.unidad}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    tutela?.distancia?.let {
                        Text(
                            text = "T = ${tutela?.tamanio}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                }

            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            // Resto de líneas (agrupadas en filas)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        "E = ${tutela?.equipo}",
                        color = Color.White, fontSize = 14.sp
                    )
                    Text(
                        "L = ${tutela?.localizacion}",
                        color = Color.White, fontSize = 14.sp
                    )
                    Text(
                        "A = ${tutela?.actitud}",
                        color = Color.White, fontSize = 14.sp
                    )
                }

            }
        }
    }
}*/


//Interacción "volver" del submenu.
private fun pushBack(backStack: MutableList<() -> Unit>, handler: () -> Unit) {
    backStack.add(handler)
}

private fun popBack(backStack: MutableList<() -> Unit>): Boolean {
    val lastIndex = backStack.lastIndex
    if (lastIndex < 0) return false
    // copiar referencia ANTES de mutar la lista
    val handler = backStack[lastIndex]
    backStack.removeAt(lastIndex)
    return try {
        handler.invoke()
        true
    } catch (_: Throwable) {
        // si el handler falla, no tumbamos la app
        true // lo consideramos consumido igualmente
    }
}

private fun clearBack(backStack: MutableList<() -> Unit>) {
    backStack.clear()
}