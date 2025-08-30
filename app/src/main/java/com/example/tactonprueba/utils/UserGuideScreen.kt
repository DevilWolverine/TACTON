package com.example.tactonprueba.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.tactonprueba.R

@Composable
fun UserGuideScreen(onDismissRequest: () -> Unit) {
    var darkTheme by remember { mutableStateOf(true) } // 👈 estado tema

    var visible by remember { mutableStateOf(false) }
    // 🎨 Definición de colores según tema
    val backgroundColor = if (darkTheme) Color(0xFF121212) else Color(0xFFFDFDFD)
    val primaryColor = if (darkTheme) Color(0xFF00E5FF) else Color(0xFF1976D2)
    val secondaryColor = if (darkTheme) Color(0xFF69F0AE) else Color(0xFF388E3C)
    val cardColor = if (darkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val textPrimary = if (darkTheme) Color.White else Color.Black
    val textSecondary = if (darkTheme) Color(0xFFB0B0B0) else Color.DarkGray

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it } // empieza desde abajo
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it } // se va hacia abajo
        ) + fadeOut(),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .zIndex(3f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 🔹 Cabecera con botón de intercambio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Text(
                        "TACTON - Guía",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { darkTheme = !darkTheme }) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = primaryColor
                        )
                    }

                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar guía",
                            tint = primaryColor
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = secondaryColor.copy(alpha = 0.3f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ======================
                // PANTALLA PRINCIPAL
                // ======================
                SectionHeader("Pantalla Principal", secondaryColor, textPrimary)

                FeatureItem(
                    icon = Icons.Default.Map,
                    title = "Mapa",
                    text = "Muestra tu ubicación, marcadores y usuarios en tiempo real. " +
                            "Contiene los componentes que interactúan con el mapa.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Explore,
                    title = "Brújula",
                    text = "Tiene dos funciones principales:\n" +
                            "    - Pulsar: La brújula centrará el mapa\n" +
                            "      en nuestra posición.\n" +
                            "    - Mantener pulsado: Cambiaremos entre\n" +
                            "      los modos de vista de la brújula,\n" +
                            "      estático o dinámico."
                    , primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.PinDrop,
                    title = "Caja de coordenadas",
                    text = "Muestra el indicativo del usuario y las coordenadas exactas de su " +
                            "posición en todo momento. Si pulsamos cambiaremos entre sus " +
                            "modos UTM y Latitud/Longitud.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.AddLocationAlt,
                    title = "Barra de herramientas lateral",
                    text = "Compuesta por tres secciones: \n" +
                            "    - Menú: Abrirá o cerrará el menú. \n" +
                            "    - Lupa: Abre el panel de introducción de\n" +
                            "      coordenadas. Al ir a las coordenadas\n" +
                            "      seleccionadas colocará un marcador\n" +
                            "      en el punto.\n" +
                            "    - Lock: Alternará entre camara fija o libre.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Straighten,
                    title = "Cancelar marcador",
                    text = "Aparecerá en pantalla cuando entremos en modo colocar marcador" +
                            "permitiendonos cancelar la acción.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Straighten,
                    title = "Cajón de distancia",
                    text = "Aparecerá en pantalla cuando entremos en modo medición.\n" +
                            "Este modo nos mostrará una línea que nos unirá con " +
                            "el punto seleccionado mostrando la distancia en metros." +
                            "Está compuesto por: \n" +
                            "   - Distancia: Muestra la distancia restante\n" +
                            "     en metros.\n" +
                            "   - Flecha: Indica a la dirección del marcador.\n" +
                            "     Si la pulsamos podemos cambiar el modo,\n" +
                            "     permitiendo indicar la dirección del\n" +
                            "     marcador según la orientación\n" +
                            "     del mapa o la dirección del usuario.\n" +
                            "   - Cruz: Cancelaremos la medición.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Straighten,
                    title = "Marcadores",
                    text = "Iconos seleccionables en el mapa.",
                    primaryColor, textPrimary, textSecondary
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = secondaryColor.copy(alpha = 0.3f)
                )
                // ======================
                // MENÚ
                // ======================
                SectionHeader("Menú", secondaryColor, textPrimary)

                MenuItem(
                    Icons.Default.AutoAwesomeMotion,
                    "Estilo de mapa: ",
                    "Permite cambiar entre los estilos implementados.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.AddLocationAlt,
                    "Marcadores: ",
                    "Mostrará una lista de marcadores que podremos seleccionar para " +
                    "colocarlos en pantalla.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Landscape,
                    "Topografía: ",
                    "Submenú con dos opciones:\n" +
                    "   - Medir entre puntos: Permitirá seleccionar\n" +
                    "     un marcador en pantalla o un punto en el\n" +
                    "     mapa para iniciar la medición hasta la\n" +
                    "     opción seleccionada.\n" +
                    "   - Listado de marcadores: Mostrará una lista\n" +
                    "     con los marcadores creados en el mapa.\n" +
                    "     Tiene dos opciones:\n" +
                    "     ir al marcador o eliminar.\n" +
                    "     Los medevacs y tutelas no se podrán\n" +
                    "     eliminar desde este menú.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.DataExploration,
                    "Ir: ",
                    "Introduce coordenadas UTM o Lat/Lon para moverte a una ubicación específica.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.BusAlert,
                    "Medevac: ",
                    "Solicita evacuaciones médicas y revisa el historial de solicitudes. En el " +
                    "historial podremos eliminar o iniciar una medición hasta el punto.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Style,
                    "Tutela: ",
                    "Crea informes de observación y accede a informes previos. En los informes" +
                    "podremos eliminar o iniciar una medición hasta el punto.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Construction,
                    "Opciones: ",
                    "Configura tu usuario y la IP del servidor. Incluye una lista de los " +
                    "usuarios conectados y nos permite conectarnos o desconectarnos" +
                    "del servidor.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.ExitToApp,
                    "Salir",
                    "Cierra la aplicación de forma segura.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                SectionHeader("Marcadores", secondaryColor, textPrimary)

                OutlinedItem(
                    icon = painterResource(id = R.drawable.nav),
                    title = "Flecha de navegación",
                    text = "Visualiza tu ubicación y la de otros usuarios en tiempo real.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.pin),
                    title = "Marcadores básicos",
                    text = "Marcadores que nos informarán de eventos o puntos de interés." +
                            "Al interactuar con ellos nos aparecerá un menú con opciones.\n" +
                            "    - Visualización: Mostrará u ocultará la\n" +
                            "      información del marcador.\n" +
                            "    - Editar: Permite cambiar el marcador.\n" +
                            "    - Medir: Iniciará el modo medición.\n" +
                            "    - Eliminar: Eliminará el marcador.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.hospital),
                    title = "Marcador medevac",
                    text = "Si lo seleccionamos podremos ver la información de la medevac.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.style),
                    title = "Marcador tutela puesto",
                    text = "Mostrará la información del tutela.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.warning),
                    title = "Marcador tutela observado",
                    text = "Mostrará la información del tutela ubicado en el punto observado.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                // Botón cerrar
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Cerrar Guía", color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, color: Color, textColor: Color) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = color
    )
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    text: String,
    primaryColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Círculo con icono
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor)
        }

        Spacer(Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
            Text(text, fontSize = 14.sp, color = textSecondary)
        }
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    text: String,
    cardColor: Color,
    primaryColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(28.dp))

            Spacer(Modifier.width(12.dp))

            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textPrimary)
                Text(text, fontSize = 13.sp, color = textSecondary)
            }
        }
    }
}

@Composable
fun OutlinedItem(
    icon: Painter,
    title: String,
    text: String,
    primaryColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, primaryColor), // mismo estilo que los otros
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                Text(text, fontSize = 13.sp, color = textSecondary)
            }
        }
    }
}



