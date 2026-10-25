package com.pedrocorcaque.flashy.ui.screens.home

import com.pedrocorcaque.flashy.ui.components.rememberReorderState
import com.pedrocorcaque.flashy.ui.components.dragToReorder
import com.pedrocorcaque.flashy.ui.util.SortOrder
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.toMutableStateList
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.ui.components.ListItemCard
import com.pedrocorcaque.flashy.ui.components.OptionSwitchRow
import com.pedrocorcaque.flashy.ui.components.SortSelector
import com.pedrocorcaque.flashy.ui.screens.flashcard.ALL_TOPICS_ID
import com.pedrocorcaque.flashy.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTopicClick: (String) -> Unit,
    onAddTopicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onStudyClick: (String, Boolean) -> Unit
) {
    val topics by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val countdownFinished by viewModel.countdownFinished.collectAsState()
    val context = LocalContext.current
    val todayStudyMs by viewModel.todayStudyMs.collectAsState()
    var showStudyPicker by remember { mutableStateOf(false) }

    // Reordenação por arrastar (só com a ordenação "Padrão", em que a lista é a própria ordem manual).
    val canReorder = sortOrder == SortOrder.DEFAULT
    val listState = rememberLazyListState()
    val localTopics = remember(topics) { topics.toMutableStateList() }
    val reorderState = rememberReorderState(
        listState = listState,
        items = localTopics,
        keyOf = { it.id },
        onReordered = { ordered -> viewModel.reorderTopics(ordered.map { it.id }) }
    )
    val edgePx = with(LocalDensity.current) { 72.dp.toPx() }
    val maxScrollStepPx = with(LocalDensity.current) { 14.dp.toPx() }
    LaunchedEffect(reorderState.draggedKey) {
        if (reorderState.draggedKey != null) reorderState.autoScroll(edgePx, maxScrollStepPx)
    }

    var showTimerConfig by remember { mutableStateOf(false) }

    // Timer chegou a zero: toca o aviso (o diálogo aparece logo abaixo).
    LaunchedEffect(countdownFinished) {
        if (countdownFinished) playTimerSound(context)
    }

    Scaffold(
        bottomBar = {
            FlashyBottomBar(
                onStudyClick = { showStudyPicker = true },
                onSummaryClick = onSummaryClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTopicClick,
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.offset(y = 10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Topic")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Header(onSettingsClick)
            Spacer(modifier = Modifier.height(24.dp))
            WelcomeText()
            Spacer(modifier = Modifier.height(20.dp))
            StudyTimerCard(
                state = timerState,
                todayMs = todayStudyMs,
                onModeChange = viewModel::onModeChange,
                onToggle = viewModel::toggleTimer,
                onReset = viewModel::resetTimer,
                onConfigure = { showTimerConfig = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tópicos",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SortSelector(current = sortOrder, onSelected = viewModel::onSortOrderChange)
            }
            if (topics.size > 1) {
                Text(
                    text = if (canReorder) {
                        "Segure e arraste um tópico para reordenar"
                    } else {
                        "Para reordenar à mão, escolha a ordenação Padrão"
                    },
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(localTopics, key = { it.id }) { topic ->
                    val isDragging = reorderState.draggedKey == topic.id
                    // O gesto vem primeiro na cadeia: assim ele não "anda" junto com o item arrastado.
                    val dragModifier = if (canReorder) {
                        Modifier
                            .dragToReorder(reorderState, topic.id)
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
                    } else {
                        Modifier
                    }
                    TopicItem(
                        topic = topic,
                        onClick = { onTopicClick(topic.id) },
                        modifier = dragModifier,
                        reorderable = canReorder
                    )
                }
            }

            if (showTimerConfig) {
                TimerDurationDialog(
                    currentMinutes = (timerState.countdownTotalMs / 60_000L).toInt(),
                    onDismiss = { showTimerConfig = false },
                    onConfirm = { minutes ->
                        viewModel.setCountdownMinutes(minutes)
                        showTimerConfig = false
                    }
                )
            }

            if (countdownFinished) {
                TimerFinishedDialog(onDismiss = viewModel::dismissCountdownFinished)
            }

            if (showStudyPicker) {
                StudyPickerDialog(
                    topics = topics,
                    onDismiss = { showStudyPicker = false },
                    onSelect = { id, shuffle ->
                        showStudyPicker = false
                        onStudyClick(id, shuffle)
                    }
                )
            }
        }
    }
}

@Composable
fun Header(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Flashy",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextGray
            )
        }
    }
}

@Composable
fun WelcomeText() {
    Column {
        Text(
            text = "Oi, amor. 💜",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "O que vamos estudar hoje?",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextGray
            )
        )
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    reorderable: Boolean = false
) {
    ListItemCard(
        title = topic.name,
        subtitle = "${topic.cardCount} cards",
        icon = TopicIcons.resolve(topic.iconName, topic.name),
        color = Color(topic.colorArgb),
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Icon(
                imageVector = if (reorderable) Icons.Default.DragHandle else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (reorderable) "Segure e arraste para reordenar" else null,
                tint = TextGray
            )
        }
    )
}

@Composable
fun FlashyBottomBar(onStudyClick: () -> Unit, onSummaryClick: () -> Unit) {
    NavigationBar(
        containerColor = DarkBackground,
        contentColor = TextGray,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onStudyClick,
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Estudar") },
            label = { Text("Estudar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onSummaryClick,
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Resumo") },
            label = { Text("Resumo") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
    }
}


@Composable
private fun StudyPickerDialog(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onSelect: (String, Boolean) -> Unit
) {
    val totalCards = topics.sumOf { it.cardCount }
    var shuffle by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = {
            Text("O que vamos estudar?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (topics.isEmpty()) {
                Text("Crie um tópico e adicione cards para começar.", color = TextGray)
            } else {
                Column {
                    OptionSwitchRow(
                        text = "Modo aleatório",
                        checked = shuffle,
                        onCheckedChange = { shuffle = it }
                    )
                    LazyColumn {
                        item {
                            StudyPickerRow(
                                name = "Todos os tópicos",
                                cardCount = totalCards,
                                color = PrimaryPurple,
                                onClick = { onSelect(ALL_TOPICS_ID, shuffle) }
                            )
                        }
                        items(topics) { topic ->
                            StudyPickerRow(
                                name = topic.name,
                                cardCount = topic.cardCount,
                                color = Color(topic.colorArgb),
                                onClick = { onSelect(topic.id, shuffle) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
        }
    )
}

@Composable
private fun StudyPickerRow(name: String, cardCount: Int, color: Color, onClick: () -> Unit) {
    val enabled = cardCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color.copy(alpha = if (enabled) 1f else 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            color = if (enabled) Color.White else TextGray,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = if (enabled) "$cardCount cards" else "sem cards",
            color = TextGray,
            fontSize = 12.sp
        )
    }
}
