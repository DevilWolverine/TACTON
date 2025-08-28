package com.example.tactonprueba.utils

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tactonprueba.R
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import java.util.Calendar
import kotlin.String


// Modelos De Datos ================================================================================
// Tipos de marcadores y filtros
enum class MarkerType { NORMAL, MEDEVAC, TUTELA }
enum class MarkerFilter { TODOS, GENERICOS, ENTIDADES }

// Composición de un marcador en pantalla
data class MarkerOption(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

// Datos de un marcador normal
data class MarkerData(
    val id: Int,
    val name: String,
    val point: Point,
    val icon: Bitmap?,
    val iconRes: Int? = null,
    val createdBy: String,
    val distance: Double?,
    val type: MarkerType,
    val medevac: MedevacData? = null,
    val tutela: TutelaData? = null
)

// Catálogo de marcadores ==========================================================================
fun defaultMarkerOptions(): List<MarkerOption> = listOf(
    MarkerOption("01", "Marcador", R.drawable.red),
    MarkerOption("02", "Marcador blanco", R.drawable.wht_circle),
    MarkerOption("03", "Marcador negro", R.drawable.wht_circle_invert),
    MarkerOption("04", "Objetivo", R.drawable.targeted_search),
    MarkerOption("05", "Campamento", R.drawable.camping),
    MarkerOption("06", "Estructura", R.drawable.structure_no_damage),
    MarkerOption("07", "Estructura dañada", R.drawable.structure_damaged),
    MarkerOption("08", "Estructura destruida", R.drawable.structure_destroyed),
    MarkerOption("09", "Estructura perdida", R.drawable.structure_failed),
    MarkerOption("10", "Peligro", R.drawable.danger),
    MarkerOption("11", "Herido", R.drawable.health),
    MarkerOption("12", "Prohibido", R.drawable.road_blocked),
    MarkerOption("13", "Esc", R.drawable._01_escuadra),
    MarkerOption("14", "Esc Al", R.drawable._02_escuadra_al),
    MarkerOption("15", "Esc Des", R.drawable._03_escuadra_des),
    MarkerOption("16", "Esc En", R.drawable._04_escuadra_en),
    MarkerOption("17", "Pn", R.drawable._05_pn),
    MarkerOption("18", "Pn Al", R.drawable._06_pn_al),
    MarkerOption("19", "Pn Des", R.drawable._07_pn_des),
    MarkerOption("20", "Pn En", R.drawable._08_pn_en),
    MarkerOption("21", "Scc", R.drawable._09_scc),
    MarkerOption("22", "Scc Al", R.drawable._10_scc_al),
    MarkerOption("23", "Scc Des", R.drawable._11_scc_des),
    MarkerOption("24", "Scc En", R.drawable._12_scc_en),
    MarkerOption("25", "Cía", R.drawable._13_cia),
    MarkerOption("26", "Cía Al", R.drawable._14_cia_al),
    MarkerOption("27", "Cía Des", R.drawable._15_cia_des),
    MarkerOption("28", "Cía En", R.drawable._16_cia_en),
    MarkerOption("29", "Bón", R.drawable._17_bon),
    MarkerOption("30", "Bón Al", R.drawable._18_bon_al),
    MarkerOption("31", "Bón Des", R.drawable._19_bon_des),
    MarkerOption("32", "Bón En", R.drawable._20_bon_en),
    MarkerOption("33", "Rg", R.drawable._21_rg),
    MarkerOption("34", "Rg Al", R.drawable._22_rg_al),
    MarkerOption("35", "Rg Des", R.drawable._23_rg_des),
    MarkerOption("36", "Rg En", R.drawable._24_rg_en),
    MarkerOption("37", "Pat", R.drawable._25_patrulla),
    MarkerOption("38", "Pat Al", R.drawable._26_patrulla_al),
    MarkerOption("39", "Pat Des", R.drawable._27_patrulla_des),
    MarkerOption("40", "Pat En", R.drawable._28_patrulla_en),
    MarkerOption("41", "S/GT", R.drawable._29_subtac),
    MarkerOption("42", "S/GT Al", R.drawable._30_subtac_al),
    MarkerOption("43", "S/GT Des", R.drawable._31_subtac_des),
    MarkerOption("44", "S/GT En", R.drawable._32_subtac_en),
    MarkerOption("45", "GT", R.drawable._33_grtac),
    MarkerOption("46", "GT Al", R.drawable._34_grtac_al),
    MarkerOption("47", "GT Des", R.drawable._35_grtac_des),
    MarkerOption("48", "GT En", R.drawable._36_grtac_en),
    MarkerOption("49", "A/GT", R.drawable._37_agtac),
    MarkerOption("50", "A/GT Al", R.drawable._38_agtac_al),
    MarkerOption("51", "A/GT Des", R.drawable._39_agtac_des),
    MarkerOption("52", "A/GT En", R.drawable._40_agtac_en),

)

// Componentes =====================================================================================
// Contenedor Marcador ===============================================================================
@Composable
private fun MarkerCard(option: MarkerOption, onClick: (MarkerOption) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clickable { onClick(option) },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(option.iconRes),
                    contentDescription = option.label,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = option.label,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// Selección Marcador Submenú ======================================================================
@Composable
fun MarkerPicker(
    onSelect: (MarkerOption) -> Unit,
    editor: Boolean = true,
    onDismissRequest: () -> Unit = {},
) {

    val gridState = rememberLazyGridState()
    var expanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(MarkerFilter.TODOS) }

    LaunchedEffect(filter) {
        if (filter == MarkerFilter.TODOS) {
            gridState.scrollToItem(0)
        }
    }

    // Filtros
    val options = remember(filter) {
        when (filter) {
            MarkerFilter.TODOS      -> defaultMarkerOptions()
            MarkerFilter.GENERICOS  -> genericMarker()
            MarkerFilter.ENTIDADES  -> entityMarker()
        }
    }

    if (editor) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Selecciona un marcador", style = MaterialTheme.typography.titleSmall)

            Box(Modifier.padding(end = 20.dp)) {
                val label = when (filter) {
                    MarkerFilter.TODOS -> "TODOS"
                    MarkerFilter.GENERICOS -> "GENÉRICOS"
                    MarkerFilter.ENTIDADES -> "ENTIDADES"
                }

                Button(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label)
                        Text("▾", fontSize = 24.sp)
                    }
                }

                // Desplegable
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .background(Color.Black)
                ) {
                    val itemModifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)

                    DropdownMenuItem(
                        text = { Text("TODOS", color = Color.White) },
                        onClick = { filter = MarkerFilter.TODOS; expanded = false },
                        modifier = itemModifier
                    )
                    DropdownMenuItem(
                        text = { Text("GENÉRICOS", color = Color.White) },
                        onClick = { filter = MarkerFilter.GENERICOS; expanded = false },
                        modifier = itemModifier
                    )
                    DropdownMenuItem(
                        text = { Text("ENTIDADES", color = Color.White) },
                        onClick = { filter = MarkerFilter.ENTIDADES; expanded = false },
                        modifier = itemModifier
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(options, key = { it.id }) { opt ->
                MarkerCard(opt, onClick = onSelect)
            }

        }

    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismissRequest()
                },
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight(0.5f).background(
                    Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ).padding(20.dp)
            ) {
                items(options, key = { it.id }) { opt ->
                    MarkerCard(opt, onClick = onSelect)
                }
            }
        }
    }
}

// Menú Flotante ===================================================================================
@Composable
fun MarkerMenu(
    mapView: MapView,
    selectedMarker: PointAnnotation?,
    onDismiss: () -> Unit,
    pointAnnotationManager: PointAnnotationManager,
    distantRequest: (Point) -> Unit,
    userLocation: Point?,
    isInfoVisible: MutableState<Boolean>,
    isEditingMarker: MutableState<PointAnnotation?>,
    polylineManager: MutableState<PolylineAnnotationManager?>,
    markerList: SnapshotStateList<MarkerData>,   // 👈 nuevo
    isMeasuringMode: MutableState<Boolean>,
    measuringMarker: MutableState<Point?>,
) {

    if (selectedMarker == null) return

    var isUTM by remember { mutableStateOf(false) }
    val markerName = selectedMarker.getData()?.asString ?: "Sin nombre"
    val point =
        Point.fromLngLat(selectedMarker.point.longitude(), selectedMarker.point.latitude())

    val utm = latLonToUTM(point.latitude(), point.longitude())
    val screenCoords = remember(selectedMarker) {
        mapView.mapboxMap.pixelForCoordinate(selectedMarker.point)
    }

    val distance = userLocation?.let {
        TurfMeasurement.distance(it, point, TurfConstants.UNIT_METERS)
    } ?: 0.0

    val formatCoords: () -> String = {
        if (isUTM) {
            "${"%.5f".format(point.latitude())}, ${"%.5f".format(point.longitude())}"
        } else {
            "${utm.zone}${utm.band} ${utm.x.toInt()} ${utm.y.toInt()}"
        }
    }

    cameraMove(mapView, point)

    if(!isMeasuringMode.value) {
        // Contenedor del menú
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismiss()
                }
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)     // 👈 centrado en pantalla
                    .padding(top = 200.dp)       // 👈 desplazado hacia abajo
                    .clickable(enabled = false) {}
                    .width(160.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
            ) {
                // Contenido del menú
                Column(
                    modifier = Modifier.background(Color(0xFF1E1E1E)).padding(12.dp),
                    verticalArrangement = Arrangement.Center,

                    ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clickable { isUTM = !isUTM }
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,

                    ) {
                        Text(
                            formatCoords(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall

                        )

                    }

                    HorizontalDivider(color = Color.Red)

                    // Botones de acción
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()

                    ) {
                        IconButton(onClick = { isInfoVisible.value = !isInfoVisible.value }) {
                            Icon(
                                if (isInfoVisible.value) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (isInfoVisible.value) "Ocultar info"
                                else "Mostrar info",
                                tint = Color.White

                            )
                        }

                        IconButton(
                            onClick = { isEditingMarker.value = selectedMarker }
                        ) {
                            Icon(
                                Icons.Default.EditLocationAlt,
                                contentDescription = "Cambiar marcador",
                                tint = Color.White
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                distantRequest(selectedMarker.point)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                Icons.Default.Architecture,
                                contentDescription = "Medir distancia",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                removeMarkerAndCancelMeasure(
                                    point = selectedMarker.point,
                                    markers = markerList,
                                    annotationManager = pointAnnotationManager,
                                    measuringMarker = measuringMarker,
                                    polylineManager = polylineManager,
                                )
                                onDismiss()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar marcador",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            if (isInfoVisible.value) {
                MarkerInfo(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    owner = "Devil", // 👈 owner Cambiar y reutilizar
                    markerName = markerName,
                    distance = distance,
                    coords = formatCoords(),
                    timestamp = formatFecHor(),

                )
            }
        }
        // Fin del contenedor

    } else {
        distantRequest(selectedMarker.point)
        onDismiss()
        isMeasuringMode.value = false

    }

}

// Información del marcador ========================================================================
@Composable
fun MarkerInfo(
    owner: String,
    markerName: String,
    distance: Double,
    timestamp: String,
    modifier: Modifier = Modifier,
    coords: String
) {
    Card(
        modifier = modifier.width(200.dp).padding(top = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Marcador de: $owner",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                "Tipo: $markerName",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                "Coordenadas:\n $coords",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                "Distancia: ${"%.1f".format(distance)} m",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                "Colocado: $timestamp",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}


// Botón Cancelar Colocar Marcador =================================================================
@Composable
fun MarkerCancel(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onCancel()
                onDismissRequest()
            }
            .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.WrongLocation,
            contentDescription = "Cancelar marcador",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Cancelar",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
// FIN Componentes =================================================================================

// Funciones Auxiliares ============================================================================
// Filtrado Marcadores =============================================================================
// Genéricos
fun genericMarker(): List<MarkerOption> =
    defaultMarkerOptions().filter { option ->
        val num = option.id.toIntOrNull()
        num != null && num in 1..12
    }

// Entidades
fun entityMarker(): List<MarkerOption> =
    defaultMarkerOptions().filter { option ->
        val num = option.id.toIntOrNull()
        num != null && num in 13..52
    }
// FIN Filtrado Marcadores =========================================================================

// Creación Composición Marcador ===================================================================
fun placeMarker(
    mgr: PointAnnotationManager,
    bmp: Bitmap,
    point: Point,
    id: Int,
    currentLocation: MutableState<Point?>,
    option: MarkerOption = defaultMarkerOptions()[0],
    markerList: SnapshotStateList<MarkerData>,
    isMedevacMode: MutableState<Boolean> = mutableStateOf(false),
    onMarkerClicked: (annotation: PointAnnotation) -> Unit,
    medevacList: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    medevacData: MedevacData? = null,
    isTutelaMode: MutableState<Boolean> = mutableStateOf(false),
    tutelaList: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    tutelaData: TutelaData? = null,

    ) {

    var distance = TurfMeasurement.distance(
        currentLocation.value!!,
        point,
        TurfConstants.UNIT_METERS
    )

    // Crea el marcador
    val annotation = mgr.create(
        PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bmp)
            .withTextField("M. $id. " + formatFecHor())
            .withTextAnchor(TextAnchor.TOP)
            .withTextOffset(listOf(0.0, 1.0))
            .withTextSize(10.0)
            .withTextColor("#FFFFFF")
            .withTextHaloColor("#000000")
            .withTextHaloWidth(8.0)
            .withData(JsonPrimitive(option.label))

    )

    // Guarda el marcador
    saveMarker(
        markerList = markerList,
        id = id,
        label = annotation.textField.toString(),
        iconRes = bmp,
        distance = distance,
        point = point,
        medevacList = medevacList,
        medevacData = medevacData,
        tutelaList = tutelaList,
        tutelaData = tutelaData,
    )

    // Listener de click en el marcador
    mgr.addClickListener { clicked ->
        if (clicked.id == annotation.id) {
            onMarkerClicked(clicked)
            true
        } else false
    }
}

// Guardar Marcador ================================================================================
private fun saveMarker(
    markerList: SnapshotStateList<MarkerData>,
    id: Int,
    label: String,
    iconRes: Bitmap,
    createdBy: String = "Operador",
    distance: Double = 0.0,
    point: Point,
    medevacList: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    medevacData: MedevacData? = null,
    tutelaList: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    tutelaData: TutelaData? = null
) {

    // Guarda el marcador según el tipo
    if (medevacData != null) {

        val medevacData = MedevacData(
            line1 = medevacData?.line1,
            line2 = medevacData?.line2.toString(),
            line3 = medevacData?.line3.toString(),
            line4 = medevacData?.line4.toString(),
            line5 = medevacData?.line5.toString(),
            line6 = medevacData?.line6.toString(),
            line7 = medevacData?.line7.toString(),
            line8 = medevacData?.line8.toString(),
            line9 = medevacData?.line9.toString(),
            distancia = distance
        )

        medevacList.add(medevacData)

    } else if (tutelaData != null) {

        val tutelaAux = tutelaList.lastOrNull()

        if (tutelaAux == null || tutelaAux.puesto != tutelaData?.puesto )  {

            val tutelaData = TutelaData(
                tiempo = tutelaData?.tiempo.toString(),
                unidad = tutelaData?.unidad.toString(),
                tamanio = tutelaData?.tamanio.toString(),
                equipo = tutelaData?.equipo.toString(),
                localizacion = tutelaData?.localizacion,
                actitud = tutelaData?.actitud.toString(),
                puesto = tutelaData?.puesto,
                distancia = distance,
                tipo = TutelaTipo.REPORT
            )

            tutelaList.add(tutelaData)

        } else {

            val tutela = TutelaData(
                tiempo = tutelaData?.tiempo.toString(),
                unidad = tutelaData?.unidad.toString(),
                tamanio = tutelaData?.tamanio.toString(),
                equipo = tutelaData?.equipo.toString(),
                localizacion = tutelaData?.puesto,
                actitud = tutelaData?.actitud.toString(),
                puesto = tutelaData?.localizacion,
                distancia = distance,
                tipo = TutelaTipo.OBJETIVO
            )

            tutelaList.add(tutela)
        }

    } else {

        val markerData = MarkerData(
            id = id,
            name = label,
            icon = iconRes,
            createdBy = createdBy,
            distance = distance,
            point = point,
            type = MarkerType.NORMAL

        )

        markerList.add(markerData)
    }
}

// Actualizar Icono Marcador =======================================================================
fun updateMarkerIconByPoint(
    point: Point,
    newIcon: Bitmap,
    markers: SnapshotStateList<MarkerData>,
    annotationManager: PointAnnotationManager?
) {
    val markerIndex = markers.indexOfFirst { it.point == point }
    if (markerIndex != -1 && annotationManager != null) {
        val oldMarker = markers[markerIndex]

        // Lo actualiza en el mapa
        val annotationToUpdate = annotationManager.annotations.find { it.geometry == point }
        annotationToUpdate?.let {
            it.iconImageBitmap = newIcon
            annotationManager.update(it)
        }

        // Actualiza el icono en la lista
        markers[markerIndex] = oldMarker.copy(icon = newIcon)
    }
}

// Eliminar Marcador ===============================================================================
fun removeMarkerByPoint(
    point: Point,
    markers: MutableList<MarkerData>,
    medevacs: SnapshotStateList<MedevacData?> = mutableStateListOf(),
    tutelas: SnapshotStateList<TutelaData?> = mutableStateListOf(),
    annotationManager: PointAnnotationManager,
) {

    // Busca el marcador en las listas
    val markerToRemove = markers.find { it.point == point }
    val medevacToRemove = medevacs.find { it?.line1 == point }
    val tutelaToRemove = tutelas.find { it?.puesto == point }
    val tutela2ToRemove = tutelas.find { it?.localizacion == point }

    // Borra el marcador del mapa y de la lista
    if (markerToRemove != null) {

        val annotationsToRemove = annotationManager.annotations.filter {
            it.geometry == point
        }

        annotationManager.delete(annotationsToRemove)

        markers.remove(markerToRemove)
        if (medevacToRemove != null) {
            medevacs.remove(medevacToRemove)
        }

        if (tutelaToRemove != null || tutela2ToRemove != null) {
            when {
                tutelaToRemove != null -> tutelas.remove(tutelaToRemove)
                tutela2ToRemove != null -> tutelas.remove(tutela2ToRemove)
            }
        }

    }
}

// Formato Fecha/Hora ==============================================================================
fun formatFecHor(): String{

    val cal = Calendar.getInstance()

    val anio = cal.get(Calendar.YEAR).toString().takeLast(2)
    val mes = cal.get(Calendar.MONTH) + 1
    val dia = cal.get(Calendar.DAY_OF_MONTH)
    val hora =  if (cal.get(Calendar.HOUR_OF_DAY) < 10) "0${cal.get(Calendar.HOUR_OF_DAY)}"
                else cal.get(Calendar.HOUR_OF_DAY)
    val min =   if (cal.get(Calendar.MINUTE) < 10) "0${cal.get(Calendar.MINUTE)}"
                else cal.get(Calendar.MINUTE)

    val mesStr = when (mes) {
        1 -> "JAN"
        2 -> "FEB"
        3 -> "MAR"
        4 -> "APR"
        5 -> "MAY"
        6 -> "JUN"
        7 -> "JUL"
        8 -> "AGO"
        9 -> "SEP"
        10 -> "OCT"
        11 -> "NOV"
        12 -> "DIC"
        else -> ""
    }

    return "$dia$hora$min$mesStr$anio"

}