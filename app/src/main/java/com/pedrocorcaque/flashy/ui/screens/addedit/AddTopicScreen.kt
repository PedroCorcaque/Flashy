package com.pedrocorcaque.flashy.ui.screens.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.pedrocorcaque.flashy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTopicScreen(
    viewModel: AddTopicViewModel,
    onBackClick: () -> Unit,
    isEditing: Boolean = false
) {
    val topicName by viewModel.topicName.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedIcon by viewModel.selectedIcon.collectAsState()

    var showCustomColor by remember { mutableStateOf(false) }
    val isCustomColor = selectedColor !in TopicColorPalette
    // Ao editar um tópico que já tem cor personalizada, abre o seletor já com essa cor.
    LaunchedEffect(isCustomColor) {
        if (isCustomColor) showCustomColor = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar tópico" else "Novo tópico",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // O conteúdo rola; o botão Salvar fica sempre visível embaixo.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Prévia: ícone e cor escolhidos
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(selectedColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        TopicIcons.vectorFor(selectedIcon),
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Label(text = "Nome do tópico")
                    Spacer(modifier = Modifier.height(8.dp))
                    FlashyTextField(
                        value = topicName,
                        onValueChange = { viewModel.onTopicNameChange(it) },
                        placeholder = "Ex: História"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Label(text = "Cor do tema")
                    Spacer(modifier = Modifier.height(16.dp))
                    TopicColorPalette.chunked(6).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowColors.forEach { color ->
                                ColorOption(
                                    color = color,
                                    isSelected = selectedColor == color,
                                    onClick = { viewModel.onColorSelect(color) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    CustomColorSection(
                        color = selectedColor,
                        isCustom = isCustomColor,
                        expanded = showCustomColor,
                        onToggle = { showCustomColor = !showCustomColor },
                        onColorChange = { viewModel.onColorSelect(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Label(text = "Ícone")
                    Spacer(modifier = Modifier.height(16.dp))
                    TopicIcons.options.chunked(6).forEach { rowIcons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowIcons.forEach { (key, icon) ->
                                IconOption(
                                    icon = icon,
                                    tint = selectedColor,
                                    isSelected = selectedIcon == key,
                                    onClick = { viewModel.onIconSelect(key) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Button(
                onClick = {
                    viewModel.saveTopic {
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(16.dp),
                enabled = topicName.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ColorOption(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                else Modifier
            )
    )
}

@Composable
fun IconOption(icon: ImageVector, tint: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) tint.copy(alpha = 0.25f) else SurfaceColor)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, tint, RoundedCornerShape(12.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) tint else TextGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

private val RainbowBrush = Brush.sweepGradient(
    listOf(
        Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF84CC16), Color(0xFF10B981),
        Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFFEF4444)
    )
)

/** Linha "Cor personalizada" que expande um seletor RGB (três sliders) para qualquer cor. */
@Composable
private fun CustomColorSection(
    color: Color,
    isCustom: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onColorChange: (Color) -> Unit
) {
    val r = (color.red * 255f).roundToInt()
    val g = (color.green * 255f).roundToInt()
    val b = (color.blue * 255f).roundToInt()
    val hex = String.format("#%02X%02X%02X", r, g, b)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isCustom) Brush.linearGradient(listOf(color, color)) else RainbowBrush)
                .then(
                    if (isCustom) Modifier.border(2.dp, Color.White, CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Cor personalizada",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (isCustom) {
            Text(text = hex, color = TextGray, fontSize = 12.sp)
        }
    }

    if (expanded) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceColor
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = hex, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "RGB($r, $g, $b)", color = TextGray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                RgbSlider("R", r, Color(0xFFEF4444)) { onColorChange(Color(it, g, b)) }
                RgbSlider("G", g, Color(0xFF10B981)) { onColorChange(Color(r, it, b)) }
                RgbSlider("B", b, Color(0xFF3B82F6)) { onColorChange(Color(r, g, it)) }
            }
        }
    }
}

@Composable
private fun RgbSlider(label: String, value: Int, accent: Color, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = DarkBackground
            )
        )
        Text(
            text = value.toString(),
            color = TextGray,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(32.dp)
        )
    }
}
