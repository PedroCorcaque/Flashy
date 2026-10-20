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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
