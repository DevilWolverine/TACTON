package com.example.tactonprueba.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation


data class MedevacData(
    val line1: Point?,
    val line2: String,
    val line3: String,
    val line4: String,
    val line5: String,
    val line6: String,
    val line7: String,
    val line8: String,
    val line9: String,
    val distancia: Double?
)

enum class TutelaTipo { REPORT, OBJETIVO }

data class TutelaData(
    val puesto: Point?,
    val tiempo: String = formatFecHor(),
    val unidad: String,
    val tamanio: String,
    val equipo: String,
    val localizacion: Point?,
    val actitud: String,
    val distancia: Double?,
    val tipo: TutelaTipo,
)

// ========================= Funciones Tutela ==============
// ========================== Panel Principal ======================
@Composable
fun TutelaReportPanel(
    tutela: TutelaData,
    onDismiss: () -> Unit,
    selectedMarker: PointAnnotation,
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // oscurece el fondo
            .zIndex(3f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clickable(enabled = false) {} // Evita cierre al pulsar dentro
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()), // scroll automático pantallas pequeñas
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado
                Row (modifier = Modifier.fillMaxWidth().padding(start = 50.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically){

                    when (selectedMarker.point) {
                        tutela.puesto -> {
                            // Marcador de puesto (aliado)
                            Icon(
                                Icons.Default.Style,
                                contentDescription = "Tutela Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)

                            )
                            Text(
                                "Informe TUTELA",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        tutela.localizacion -> {
                            // Marcador de localización (enemigo/secundario)
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Objetivo Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)

                            )
                            Text(
                                "Objetivo TUTELA",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row (modifier = Modifier.fillMaxWidth().padding(start = 50.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    when (selectedMarker.point) {
                        tutela.puesto -> {
                            // Marcador de puesto (aliado)
                            Icon(
                                Icons.Default.PinDrop,
                                contentDescription = "Puesto Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                "${"%.5f".format(tutela.puesto?.latitude() ?: 0.0)}, " +
                                        "${"%.5f".format(tutela.puesto?.longitude() ?: 0.0)}",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }

                        tutela.localizacion -> {
                            // Marcador de localización (enemigo/secundario)
                            Icon(
                                Icons.Default.PinDrop,
                                contentDescription = "Localización Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                "${"%.5f".format(tutela?.localizacion?.latitude() ?: 0.0)}, " +
                                        "${"%.5f".format(tutela?.localizacion?.longitude() ?: 0.0)}",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

                // Auxiliar para mostrar cada línea
                @Composable
                fun LineItem(label: String, value: String) {
                    Row {
                        Text(label, color = Color.Gray, fontSize = 15.sp)
                        Text(
                            text = if (value.isNotBlank()) value else " ",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }

                // Contenido del informe
                LineItem("1. T: ", tutela.tiempo)
                LineItem("2. U: ", tutela.unidad)
                LineItem("3. T: ", tutela.tamanio)
                LineItem("4. E: ", tutela.equipo)

                when (selectedMarker.point) {
                    tutela.puesto -> {
                        // Marcador de puesto (aliado)
                        LineItem(
                            "5. L: ",
                            if (tutela.localizacion != null)
                                "${"%.5f".format(tutela.localizacion.latitude())}, " +
                                        "${"%.5f".format(tutela.localizacion.longitude())}"
                            else "-"
                        )
                    }

                    tutela.localizacion -> {
                        // Marcador de localización (enemigo/secundario)
                        LineItem(
                            "5. Siendo Observado desde: \n${"%.5f".format(tutela.puesto?.latitude())}, ${"%.5f".format(tutela.puesto?.longitude())}",""
                        )
                    }
                }

                LineItem("6. A: ", tutela.actitud)

                Spacer(Modifier.height(12.dp))

                // Botón cerrar
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", color = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}


// ========================== Panel de edición ======================
@Composable
fun TutelaFormPanel(
    puestoPoint: Point,                 // 👈 posición principal (puesto)
    onDismissRequest: () -> Unit,
    onSubmit: (TutelaData) -> Unit,
    onPickLocation: () -> Unit,         // 👈 callback para activar selección en mapa
    selectedLocalizacion: MutableState<Point?>,       // 👈 coordenada elegida en mapa
    modifier: Modifier = Modifier,
    isPickingLocalizacion: MutableState<Boolean>
) {
    val tiempo = formatFecHor()
    var unidad by remember { mutableStateOf("Infantería") }
    var tamanio by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("Fusil") }
    var actitud by remember { mutableStateOf("Desconocida") }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier.fillMaxSize().zIndex(3f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column ( modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    // L1 - Tiempo
                    Text(
                        "Informe TUTELA",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Puesto: ${"%.5f".format(puestoPoint.latitude())}, ${"%.5f".format(puestoPoint.longitude())}",
                        color = Color.LightGray
                    )
                }
                HorizontalDivider(color = Color.Cyan.copy(alpha = 0.6f), thickness = 1.dp)

                // L1 - Tiempo
                Text("1. Tiempo: $tiempo", color = Color.LightGray)

                Column {
                    Text(
                        "2. Localización",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()              // 👈 cerrar panel
                            onPickLocation()                // 👈 activar selección en mapa (cierra + activa listener)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.DarkGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    )
                    {
                        if (selectedLocalizacion.value != null) {
                            Text(
                                "${"%.5f".format(selectedLocalizacion.value?.latitude())}, " +
                                        "${"%.5f".format(selectedLocalizacion.value?.longitude())}",
                                color = Color.White
                            )
                        } else {
                            Text("Seleccionar en el mapa", color = Color.LightGray)
                        }
                    }
                }

                // L2 - Unidad
                Column {
                    Text("3. Unidad", color = Color.Gray, fontSize = 12.sp)
                    StyledTextField(
                        value = unidad,
                        onValueChange = { unidad = it },
                        label = "",
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                // L3 - Tamaño
                Column {
                    Text("4. Tamaño", color = Color.Gray, fontSize = 12.sp)
                    StyledTextField(
                        value = tamanio,
                        onValueChange = { tamanio = it },
                        label = "Tamaño",
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                // L4 - Equipo
                Column {
                    Text("5. Equipo", color = Color.Gray, fontSize = 12.sp)
                    StyledTextField(
                        value = equipo,
                        onValueChange = { equipo = it },
                        label = "",
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                Column {
                    Text("7. Actitud", color = Color.Gray, fontSize = 12.sp)
                    StyledTextField(
                        value = actitud,
                        onValueChange = { actitud = it },
                        label = "",
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Botones
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        // ✅ Resetear coordenadas al cancelar
                        selectedLocalizacion.value = null
                        onDismissRequest()
                    }) {
                        Text("Cancelar", color = Color.LightGray)
                    }
                    Button(onClick = {
                        val tutela = TutelaData(
                            puesto = puestoPoint,
                            tiempo = tiempo,
                            unidad = unidad,
                            tamanio = tamanio,
                            equipo = equipo,
                            localizacion = selectedLocalizacion.value,
                            actitud = actitud,
                            distancia = null,
                            tipo = TutelaTipo.REPORT
                        )
                        onSubmit(tutela)  // 👈 solo lo envías
                    }) {
                        Text("Enviar")
                    }

                }
            }
        }
    }
}



// ========================== Funciones auxiliares ======================
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    var textValue by remember { mutableStateOf(TextFieldValue(value)) }
    var shouldSelectAll by remember { mutableStateOf(false) }

    val customTextColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.LightGray,
        cursorColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            onValueChange(it.text)
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = customTextColors,
        modifier = modifier.fillMaxWidth().onFocusEvent { focusState ->
            if (focusState.isFocused) {
                shouldSelectAll = true
            }
        }
    )

    // Selección automática de todo el texto
    LaunchedEffect(shouldSelectAll) {
        if (shouldSelectAll) {
            textValue = textValue.copy(selection = TextRange(0, textValue.text.length))
            shouldSelectAll = false
        }
    }
}
// ========================== Fin Funciones Tutela ==========================


// ========================= Funciones Medevac ==============================
// ========================== Panel Principal ===============================
@Composable
fun MedevacReportPanel(
    medevac: MedevacData,
    onDismiss: () -> Unit,
    selectedMarker: PointAnnotation
) {
    // Cerrar al hacer clic fuera
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // oscurece el fondo
            .zIndex(3f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clickable(enabled = false) {} // evitar que el Card cierre al pulsar dentro
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado
                Row (modifier = Modifier.fillMaxWidth().padding(start = 50.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        Icons.Default.DataSaverOn,
                        contentDescription = "Hospital Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)

                    )
                    Text(
                        "Informe MEDEVAC",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                HorizontalDivider(color = Color.Red.copy(alpha = 0.6f), thickness = 1.dp)

                // Función auxiliar para mostrar cada línea
                @Composable
                fun LineItem(label: String, value: String) {
                    Column {
                        Text(label, color = Color.Gray, fontSize = 12.sp)
                        Text(value, color = Color.White, fontSize = 15.sp)
                    }
                }

                // Contenido del informe
                LineItem(
                    "1. Zona De Recogida",
                    "${"%.5f".format(medevac.line1?.latitude())}, ${"%.5f".format(medevac.line1?.longitude())}"
                )
                LineItem("2. Indicativo / Frecuencia", medevac.line2)
                LineItem("3. Número De Pacientes", medevac.line3)
                LineItem("4. Equipo Especial Requerido", medevac.line4)
                LineItem("5. Estado De Paciente", medevac.line5)
                LineItem("6. Seguridad En Zona", medevac.line6)
                LineItem("7. Método De Marcaje", medevac.line7)
                LineItem("8. Tipo De Paciente", medevac.line8)
                LineItem("9. Terreno / Obstáculos En Zona De Recogida", medevac.line9)

                Spacer(modifier = Modifier.height(12.dp))

                // Botón cerrar
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", color = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

// ========================== Panel de edición ======================
@Composable
fun MedevacFormPanel(
    point: Point,
    onDismissRequest: () -> Unit,
    onSubmit: (MedevacData) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    var l2usuario by remember { mutableStateOf("Usuario") }
    var l2freq by remember { mutableStateOf("300.00") }
    var l3cantidad by remember { mutableStateOf("1") }
    var l3precedence by remember { mutableStateOf("A - Urgente") }
    var l4 by remember { mutableStateOf("A - Ninguno") }
    var l5 by remember { mutableStateOf("L - Incapacitado") }
    var l6 by remember { mutableStateOf("N - No Enemigo") }
    var l7 by remember { mutableStateOf("A - Paineles") }
    var l8 by remember { mutableStateOf("A - Fuerzas Militares Aliadas") }
    var l9 by remember { mutableStateOf("Despejado") }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier.fillMaxSize().zIndex(3f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Solicitud MEDEVAC (9-Líneas)", color = Color.White)

                Text(
                    "1. Zona de recogida: ${
                        "%.5f".format(point.latitude())
                    }, ${"%.5f".format(point.longitude())}",
                    color = Color.LightGray
                )

                // L2
                Column {
                    Text(
                        "2. Indicativo / Radio Frecuencia",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        StyledTextField(
                            value = l2usuario,
                            onValueChange = { l2usuario = it },
                            label = "Indicativo",
                            modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        StyledTextField(
                            value = l2freq,
                            onValueChange = { l2freq = it },
                            label = "Frecuencia",
                            modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                    }
                }

                // L3
                Column {
                    Text(
                        "3. Pacientes por prioridad",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StyledTextField(
                            value = l3cantidad,
                            onValueChange = { l3cantidad = it },
                            label = "Cantidad",
                            modifier = Modifier.weight(0.25f),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        l3precedence = DropdownInline(
                            opciones = listOf("A - Urgente", "B - Prioritario", "R - Rutina"),
                            modifier = Modifier.weight(0.75f)
                        )
                    }
                }

                // L4
                l4 = DropdownLine("4. Equipo Especial Requerido", listOf("A - Ninguno", "B - Grua", "C - Extractor", "D - Ventilador"))

                // L5
                l5 = DropdownLine("5. Estado De Paciente", listOf("L - Incapacitado (Camilla)"," A - Ambulatorio (Caminando)", "E - Acompañante (ej. niños)"))

                // L6
                l6 = DropdownLine("6. Seguridad En Zona", listOf("N - No Enemigo", "P - Posible Enemigo", "E - Enemigo En Area", "X - Zona Recogida Caliente - Escolta Requerida"))

                // L7
                l7 = DropdownLine("7. Método De Marcaje", listOf("A - Paineles", "B - Pyro", "C - Humo", "D - Ninguno"))

                // L8
                l8 = DropdownLine(
                    "8. Tipo de Paciente",
                    listOf(
                        "A - Fuerzas Militares Aliadas",
                        "B - Fuerzas Civiles Aliadas",
                        "C - Fuerzas Seguridad No Aliadas",
                        "D - Civiles No Aliados",
                        "E - Fuerzas Enemigas / Prisioneros Guerra / Detenidos",
                        "F - Niños"
                    )
                )

                // L9
                StyledTextField(
                    value = l9,
                    onValueChange = { l9 = it },
                    label = " 9.Terreno / Obstáculos En Zona De Recogida",
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Botones
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar", color = Color.LightGray)
                    }
                    Button(onClick = {
                        val medevac = MedevacData(
                            line1 = point,
                            line2 = "$l2usuario / $l2freq",
                            line3 = "$l3cantidad $l3precedence",
                            line4 = l4,
                            line5 = l5,
                            line6 = l6,
                            line7 = l7,
                            line8 = l8,
                            line9 = l9,
                            distancia = null
                        )
                        onSubmit(medevac)
                    }) {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

// ========================== Funciones auxiliares ======================
@Composable
fun DropdownLine(
    label: String,
    opciones: List<String>,
    modifier: Modifier = Modifier
): String {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(opciones.first()) }
    var buttonWidth by remember { mutableStateOf(0) }

    Column(modifier = modifier) {   // 👈 aquí usamos el modifier que venga de fuera
        // Etiqueta
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box {
            // Caja de selección
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()   // 👈 ahora sí ocupa solo lo que diga el padre
                    .height(48.dp)
                    .onGloballyPositioned { coords ->
                        buttonWidth = coords.size.width
                    },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2C2C2C),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selected, color = Color.White)
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }

            // Menú desplegable
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { buttonWidth.toDp() })
                    .background(Color(0xFF2C2C2C))
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                opcion,
                                color = if (opcion == selected) Color.Cyan else Color.White
                            )
                        },
                        onClick = {
                            selected = opcion
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    return selected
}

@Composable
fun DropdownInline(
    opciones: List<String>,
    modifier: Modifier = Modifier
): String {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(opciones.first()) }
    var buttonWidth by remember { mutableStateOf(0) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
                .padding(top = 8.dp)
                .onGloballyPositioned { coords -> buttonWidth = coords.size.width },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.DarkGray),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF2C2C2C),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, color = Color.White)
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { buttonWidth.toDp() })
                .background(Color(0xFF2C2C2C))
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion,
                            color = if (opcion == selected) Color.Cyan else Color.White
                        )
                    },
                    onClick = {
                        selected = opcion
                        expanded = false
                    }
                )
            }
        }
    }
    return selected
}
// ========================== Fin Funciones Medevac ==========================

