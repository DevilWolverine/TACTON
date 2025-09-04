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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.tactonprueba.R

// Componente Guía =================================================================================
@Composable
fun UserGuideScreen(onDismissRequest: () -> Unit) {

    // Temas
    var darkTheme by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }

    // Definición de colores según tema
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
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
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

                SectionHeader("Pantalla Principal", secondaryColor, textPrimary)

                FeatureItem(
                    icon = Icons.Default.Map,
                    title = "Mapa",
                    text = "Muestra la ubicación, marcadores y usuarios en tiempo real. " +
                            "Se puede rotar, ampliar, mover y cambiar el eje" +
                            " de la perspectiva.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Explore,
                    title = "Brújula",
                    text = "Tiene dos funciones principales:\n" +
                            "    - Pulsar: Centra el mapa en la\n" +
                            "      posición del dispositivo.\n" +
                            "    - Mantener pulsado: Cambia entre\n" +
                            "      los modos de vista de la brújula,\n" +
                            "      estático o dinámico."
                    , primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.MyLocation,
                    title = "Caja de coordenadas",
                    text = "Muestra el indicativo del usuario y las coordenadas exactas de su " +
                            "posición en todo momento. Al pulsar, cambiará entre sus " +
                            "modos UTM y Latitud/Longitud.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.AppRegistration,
                    title = "Barra de herramientas lateral",
                    text = "Compuesta por tres secciones: \n" +
                            "    - Menú: Abre o cierra el menú. \n" +
                            "    - Lupa: Abre el panel de introducción de\n" +
                            "      coordenadas. Al ir a las coordenadas\n" +
                            "      seleccionadas colocará un marcador\n" +
                            "      en el punto.\n" +
                            "    - Lock: Alterna entre camara fija o libre.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.WrongLocation,
                    title = "Cancelar marcador",
                    text = "Aparece en pantalla al entrar en modo colocar marcador" +
                            "permitiendo cancelar la acción.",
                    primaryColor, textPrimary, textSecondary
                )

                FeatureItem(
                    icon = Icons.Default.Straighten,
                    title = "Cajón de distancia",
                    text = "Aparece en pantalla al iniciar el modo medición.\n" +
                            "Este modo muestra una línea que une el dispositivo con " +
                            "el punto seleccionado mostrando la distancia en metros." +
                            "Está compuesto por: \n" +
                            "   - Distancia: Muestra la distancia restante\n" +
                            "     en metros.\n" +
                            "   - Flecha: Indica a la dirección del marcador.\n" +
                            "     Al pulsarla cambia el modo,\n" +
                            "     permitiendo indicar la dirección del\n" +
                            "     marcador según la orientación\n" +
                            "     del mapa o la orientación del usuario.\n" +
                            "   - Cruz: Cancela la medición.",
                    primaryColor, textPrimary, textSecondary
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = secondaryColor.copy(alpha = 0.3f)
                )

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
                    "Muestra una lista de marcadores para " +
                    "colocarlos en el mapa.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Landscape,
                    "Topografía: ",
                    "Submenú con dos opciones:\n" +
                    "   - Medir entre puntos: Permite seleccionar\n" +
                    "     un marcador o un punto en el\n" +
                    "     mapa para iniciar la medición hasta la\n" +
                    "     opción seleccionada.\n" +
                    "   - Listado de marcadores: Muestra una lista\n" +
                    "     de los marcadores activos en el mapa.\n" +
                    "     Permite realizar dos acciones:\n" +
                    "     ir al marcador o eliminarlo.\n\n" +
                    "     * Los medevacs y tutelas no se podrán\n" +
                    "     eliminar desde este menú.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.DataExploration,
                    "Ir: ",
                    "Abre el panel de introducción de coordenadas. Al ir a las coordenadas " +
                    "seleccionadas colocará un marcador en el punto.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.BusAlert,
                    "Medevac: ",
                    "Solicita evacuaciones médicas y revisa el historial de solicitudes. El " +
                    "historial permite eliminar o iniciar una medición hasta el punto.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Style,
                    "Tutela: ",
                    "Crea informes de observación y accede a informes previos. El " +
                    "historial permite eliminar o iniciar una medición hasta el punto.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Construction,
                    "Opciones: ",
                    "Configura el usuario y la IP del servidor. Incluye una lista de los " +
                    "usuarios conectados y permite conectarse o desconectarse" +
                    "del servidor.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                MenuItem(
                    Icons.Default.Cancel,
                    "Salir",
                    "Cierra la aplicación.",
                    cardColor, primaryColor, textPrimary, textSecondary
                )

                SectionHeader("Marcadores", secondaryColor, textPrimary)

                OutlinedItem(
                    icon = painterResource(id = R.drawable.nav),
                    title = "Flecha de navegación",
                    text = "Visualiza la ubicación del usuario" +
                            " y la de otros usuarios en tiempo real.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.pin),
                    title = "Marcadores básicos",
                    text = "Marcadores que informan de eventos o puntos de interés." +
                            "Al interactuar con aparece un menú con las siguientes opciones:\n" +
                            "    - Visualización: Muestra u oculta la\n" +
                            "      información del marcador.\n" +
                            "    - Editar: Permite cambiar el marcador.\n" +
                            "    - Medir: Inicia el modo medición.\n" +
                            "    - Eliminar: Elimina el marcador.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.hospital),
                    title = "Marcador medevac",
                    text = "Muestra la información del medevac.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.style),
                    title = "Marcador tutela puesto",
                    text = "Muestra la información del tutela.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

                OutlinedItem(
                    icon = painterResource(id = R.drawable.warning),
                    title = "Marcador tutela observado",
                    text = "Muestra la información del tutela desde el punto observado.",
                    primaryColor, textPrimary, textSecondary, cardColor
                )

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
// FIN Componente Guía =============================================================================

// Funciones Auxiliares ============================================================================
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



