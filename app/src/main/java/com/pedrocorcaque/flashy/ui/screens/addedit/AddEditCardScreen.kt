package com.pedrocorcaque.flashy.ui.screens.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.ui.components.ConfirmDeleteDialog
import com.pedrocorcaque.flashy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: AddEditCardViewModel,
    topicId: String,
    cardId: String?,
    onBackClick: () -> Unit
) {
    val frontText by viewModel.front.collectAsState()
    val backText by viewModel.back.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val subtopics by viewModel.subtopics.collectAsState()
    val selectedTopicId by viewModel.selectedTopicId.collectAsState()
    val selectedSubtopicId by viewModel.selectedSubtopicId.collectAsState()
    val isEditing = cardId != null
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val selectedTopic = topics.find { it.id == selectedTopicId }
    val selectedSubtopic = subtopics.find { it.id == selectedSubtopicId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar card" else "Novo card",
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
                actions = {
                    if (isEditing) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(SurfaceColor)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Excluir card", color = ErrorRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
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
                .padding(horizontal = 20.dp)
        ) {
            // Os campos rolam; o botão Salvar fica sempre visível embaixo.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Label(text = "Frente")
                Spacer(modifier = Modifier.height(8.dp))
                FlashyTextField(
                    value = frontText,
                    onValueChange = { viewModel.onFrontChange(it) },
                    placeholder = "Digite a pergunta...",
                    modifier = Modifier.height(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Label(text = "Verso")
                Spacer(modifier = Modifier.height(8.dp))
                FlashyTextField(
                    value = backText,
                    onValueChange = { viewModel.onBackChange(it) },
                    placeholder = "Digite a resposta...",
                    modifier = Modifier.height(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Label(text = "Tópico")
                Spacer(modifier = Modifier.height(8.dp))
                FlashyDropdown(
                    selectedLabel = selectedTopic?.name ?: "Carregando...",
                    options = topics.map { DropdownOption(it.id, it.name) },
                    onOptionSelected = { id -> id?.let { viewModel.onTopicSelected(it) } },
                    accentColor = selectedTopic?.let { Color(it.colorArgb) } ?: PrimaryPurple,
                    leadingIcon = Icons.Default.AccountTree
                )

                Spacer(modifier = Modifier.height(24.dp))

                Label(text = "Subtópico (opcional)")
                Spacer(modifier = Modifier.height(8.dp))
                FlashyDropdown(
                    selectedLabel = selectedSubtopic?.name ?: "Nenhum",
                    options = listOf(DropdownOption(null, "Nenhum")) +
                        subtopics.map { DropdownOption(it.id, it.name) },
                    onOptionSelected = { viewModel.onSubtopicSelected(it) },
                    accentColor = selectedTopic?.let { Color(it.colorArgb) } ?: PrimaryPurple,
                    leadingIcon = Icons.Default.Bookmark
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    title = "Excluir card?",
                    message = "Este card será apagado permanentemente.",
                    onConfirm = {
                        showDeleteDialog = false
                        viewModel.deleteCard { onBackClick() }
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            Button(
                onClick = {
                    viewModel.saveCard {
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(16.dp),
                enabled = frontText.isNotBlank() && backText.isNotBlank() && selectedTopic != null
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun Label(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun FlashyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    singleLine: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = SurfaceColor,
            focusedContainerColor = SurfaceColor,
            unfocusedContainerColor = SurfaceColor,
            cursorColor = PrimaryPurple,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

data class DropdownOption(val id: String?, val label: String)

/** Seletor com menu suspenso, no mesmo visual do antigo TopicSelector. */
@Composable
fun FlashyDropdown(
    selectedLabel: String,
    options: List<DropdownOption>,
    onOptionSelected: (String?) -> Unit,
    accentColor: Color,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            color = SurfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(leadingIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(selectedLabel, color = Color.White, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, color = Color.White) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option.id)
                    }
                )
            }
        }
    }
}
