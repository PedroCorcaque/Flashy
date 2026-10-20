package com.pedrocorcaque.flashy.ui.screens.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.ui.components.ConfirmDeleteDialog
import com.pedrocorcaque.flashy.ui.components.OptionSwitchRow
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
    val selectedSubtopicId by viewModel.selectedSubtopicId.collectAsState()
    val selectedSubtopic = subtopics.find { it.id == selectedSubtopicId }
    val selectedSubtopicName = selectedSubtopic?.name
    
    val topicName = topic?.name ?: "Carregando..."
    val cardCount = topic?.cardCount ?: 0
    
    var isInverted by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var showAddSubtopic by remember { mutableStateOf(false) }
    var showEditSubtopic by remember { mutableStateOf(false) }
    var showDeleteSubtopic by remember { mutableStateOf(false) }
    var showDeleteTopic by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopicHeader(
                name = topicName,
                count = cardCount,
                color = topic?.colorArgb?.let { Color(it) } ?: PrimaryPurple,
                icon = TopicIcons.resolve(topic?.iconName, topicName)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubtopicChips(
                subtopics = subtopics,
                selectedId = selectedSubtopicId,
                onSelect = { viewModel.selectSubtopic(it) },
                onAddClick = { showAddSubtopic = true }
            )

            if (selectedSubtopic != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtópico: ${selectedSubtopic.name}",
                        color = TextGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showEditSubtopic = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar subtópico", tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showDeleteSubtopic = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir subtópico", tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                }
            }

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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Inverter cartas (Verso primeiro)",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Switch(
                    checked = isInverted,
                    onCheckedChange = { isInverted = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryPurple,
                        checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f),
                        uncheckedThumbColor = TextGray,
                        uncheckedTrackColor = SurfaceColor
                    )
                )
            }
            
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cards) { card ->
                    TopicCardItem(
                        card = card,
                        subtitle = subtopics.find { it.id == card.subtopicId }?.name ?: "Ver detalhes",
                        onClick = { onEditCardClick(card.id) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
fun TopicCardItem(card: Flashcard, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
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
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextGray
            )
        }
    }
}

@Composable
fun SubtopicChips(
    subtopics: List<Subtopic>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onAddClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SubtopicChip(label = "Todos", selected = selectedId == null, onClick = { onSelect(null) })
        }
        items(subtopics) { subtopic ->
            SubtopicChip(
                label = subtopic.name,
                selected = selectedId == subtopic.id,
                onClick = { onSelect(subtopic.id) }
            )
        }
        item {
            Surface(
                modifier = Modifier.clickable { onAddClick() },
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Subtópico", color = TextGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SubtopicChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (selected) PrimaryPurple else SurfaceColor
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
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
