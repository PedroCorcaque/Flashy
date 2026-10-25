package com.pedrocorcaque.flashy.ui.screens.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.ui.components.ConfirmDeleteDialog
import com.pedrocorcaque.flashy.ui.components.ListItemCard
import com.pedrocorcaque.flashy.ui.components.OptionSwitchRow
import com.pedrocorcaque.flashy.ui.components.SortSelector
import com.pedrocorcaque.flashy.ui.components.dragToReorder
import com.pedrocorcaque.flashy.ui.components.rememberReorderState
import com.pedrocorcaque.flashy.ui.screens.addedit.FlashyTextField
import com.pedrocorcaque.flashy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    viewModel: TopicViewModel,
    topicId: String,
    onBackClick: () -> Unit,
    onEditTopicClick: () -> Unit,
    onAddCardClick: (String?) -> Unit,
    onEditCardClick: (String) -> Unit,
    onStudyClick: (Boolean, String?, Boolean) -> Unit
) {
    val topic by viewModel.topic.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val subtopics by viewModel.subtopics.collectAsState()
    val subtopicSort by viewModel.subtopicSort.collectAsState()
    val selectedSubtopicId by viewModel.selectedSubtopicId.collectAsState()
    val selectedSubtopic = subtopics.find { it.subtopic.id == selectedSubtopicId }?.subtopic
    val selectedSubtopicName = selectedSubtopic?.name

    val topicName = topic?.name ?: "Carregando..."
    val cardCount = topic?.cardCount ?: 0
    val topicColor = topic?.colorArgb?.let { Color(it) } ?: PrimaryPurple

    var isInverted by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var showAddSubtopic by remember { mutableStateOf(false) }
    var showEditSubtopic by remember { mutableStateOf(false) }
    var showDeleteSubtopic by remember { mutableStateOf(false) }
    var showDeleteTopic by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Reordenação por arrastar: trabalha numa cópia local; ao soltar, a nova ordem vai para o banco.
    val listState = rememberLazyListState()
    val localCards = remember(cards) { cards.toMutableStateList() }
    val reorderState = rememberReorderState(
        listState = listState,
        items = localCards,
        keyOf = { it.id },
        onReordered = { ordered -> viewModel.reorderCards(ordered.map { it.id }) }
    )
    val edgePx = with(LocalDensity.current) { 72.dp.toPx() }
    val maxScrollStepPx = with(LocalDensity.current) { 14.dp.toPx() }
    LaunchedEffect(reorderState.draggedKey) {
        if (reorderState.draggedKey != null) reorderState.autoScroll(edgePx, maxScrollStepPx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
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
                                text = { Text("Editar tópico", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onEditTopicClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Excluir tópico", color = ErrorRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteTopic = true
                                }
                            )
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
            // Tudo que rola: cabeçalho, subtópicos e cards.
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    TopicHeader(
                        name = topicName,
                        count = cardCount,
                        color = topicColor,
                        icon = TopicIcons.resolve(topic?.iconName, topicName)
                    )
                }

                // ---- Subtópicos (mesmo visual dos tópicos da tela inicial) ----
                item {
                    SectionHeader(title = "Subtópicos") {
                        SortSelector(current = subtopicSort, onSelected = viewModel::onSubtopicSortChange)
                        IconButton(onClick = { showAddSubtopic = true }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Novo subtópico",
                                tint = PrimaryPurple
                            )
                        }
                    }
                }

                if (subtopics.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum subtópico ainda. Toque em + para criar.",
                            color = TextGray,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(subtopics, key = { it.subtopic.id }) { entry ->
                        val isSelected = entry.subtopic.id == selectedSubtopicId
                        ListItemCard(
                            title = entry.subtopic.name,
                            subtitle = "${entry.cardCount} cards",
                            icon = Icons.Default.Bookmark,
                            color = topicColor,
                            selected = isSelected,
                            onClick = { viewModel.toggleSubtopic(entry.subtopic.id) },
                            trailing = {
                                if (isSelected) {
                                    Row {
                                        IconButton(onClick = { showEditSubtopic = true }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar subtópico", tint = TextGray, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { showDeleteSubtopic = true }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir subtópico", tint = TextGray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = TextGray
                                    )
                                }
                            }
                        )
                    }
                }

                // ---- Cards ----
                item {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(
                            title = if (selectedSubtopicName != null) "Cards · $selectedSubtopicName" else "Cards"
                        )
                        if (localCards.size > 1) {
                            Text(
                                text = "Segure e arraste um card para reordenar",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (cards.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum card aqui ainda.",
                            color = TextGray,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(localCards, key = { it.id }) { card ->
                        val isDragging = reorderState.draggedKey == card.id
                        TopicCardItem(
                            card = card,
                            subtitle = subtopics.find { it.subtopic.id == card.subtopicId }?.subtopic?.name ?: "Ver detalhes",
                            onClick = { onEditCardClick(card.id) },
                            // O gesto vem primeiro na cadeia: assim ele não "anda" junto com o item arrastado.
                            modifier = Modifier
                                .dragToReorder(reorderState, card.id)
                                .then(
                                    if (isDragging) {
                                        Modifier
                                            .zIndex(1f)
                                            .graphicsLayer {
                                                translationY = reorderState.draggedOffsetY()
                                                scaleX = 1.03f
                                                scaleY = 1.03f
                                                alpha = 0.95f
                                            }
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }

            // ---- Controles fixos embaixo ----
            OptionSwitchRow(
                text = "Inverter cartas (Verso primeiro)",
                checked = isInverted,
                onCheckedChange = { isInverted = it }
            )

            OptionSwitchRow(
                text = "Modo aleatório",
                checked = isShuffle,
                onCheckedChange = { isShuffle = it }
            )

            Button(
                onClick = { onStudyClick(isInverted, selectedSubtopicId, isShuffle) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                enabled = cards.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedSubtopicName != null) "Estudar: $selectedSubtopicName" else "Estudar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onAddCardClick(selectedSubtopicId) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar card")
            }
        }

        // ---- Diálogos ----
        if (showEditSubtopic && selectedSubtopic != null) {
            SubtopicNameDialog(
                title = "Editar subtópico",
                initialName = selectedSubtopic.name,
                onDismiss = { showEditSubtopic = false },
                onConfirm = { name ->
                    viewModel.renameSubtopic(selectedSubtopic, name)
                    showEditSubtopic = false
                }
            )
        }

        if (showDeleteSubtopic && selectedSubtopic != null) {
            ConfirmDeleteDialog(
                title = "Excluir subtópico?",
                message = "Os cards de \"${selectedSubtopic.name}\" não serão apagados; eles ficarão sem subtópico.",
                onConfirm = {
                    viewModel.deleteSubtopic(selectedSubtopic.id)
                    showDeleteSubtopic = false
                },
                onDismiss = { showDeleteSubtopic = false }
            )
        }

        if (showDeleteTopic) {
            ConfirmDeleteDialog(
                title = "Excluir tópico?",
                message = "O tópico \"$topicName\", com seus $cardCount cards e subtópicos, será apagado permanentemente.",
                onConfirm = {
                    showDeleteTopic = false
                    viewModel.deleteTopic(onDone = onBackClick)
                },
                onDismiss = { showDeleteTopic = false }
            )
        }

        if (showAddSubtopic) {
            SubtopicNameDialog(
                title = "Novo subtópico",
                onDismiss = { showAddSubtopic = false },
                onConfirm = { name ->
                    viewModel.addSubtopic(name)
                    showAddSubtopic = false
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun TopicHeader(name: String, count: Int, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(56.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "$count cards",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextGray
            )
        )
    }
}

@Composable
fun TopicCardItem(
    card: Flashcard,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextGray
                    )
                )
            }
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Segure e arraste para reordenar",
                tint = TextGray
            )
        }
    }
}

@Composable
private fun SubtopicNameDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            FlashyTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Ex: Cinemática"
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Salvar", color = if (name.isNotBlank()) PrimaryPurple else TextGray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
        }
    )
}
