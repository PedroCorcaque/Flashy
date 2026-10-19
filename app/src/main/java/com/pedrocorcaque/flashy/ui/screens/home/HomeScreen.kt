package com.pedrocorcaque.flashy.ui.screens.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.ui.screens.flashcard.ALL_TOPICS_ID
import com.pedrocorcaque.flashy.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTopicClick: (String) -> Unit,
    onAddTopicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStudyClick: (String) -> Unit
) {
    val topics by viewModel.uiState.collectAsState()
    var showStudyPicker by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { FlashyBottomBar(onStudyClick = { showStudyPicker = true }) },
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
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(topics) { topic ->
                    TopicItem(topic = topic, onClick = { onTopicClick(topic.id) })
                }
            }

            if (showStudyPicker) {
                StudyPickerDialog(
                    topics = topics,
                    onDismiss = { showStudyPicker = false },
                    onSelect = { id ->
                        showStudyPicker = false
                        onStudyClick(id)
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
fun TopicItem(topic: Topic, onClick: () -> Unit) {
    val topicColor = Color(topic.colorArgb)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(topicColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTopicIcon(topic.name),
                    contentDescription = null,
                    tint = topicColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${topic.cardCount} cards",
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
fun FlashyBottomBar(onStudyClick: () -> Unit) {
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

private fun getTopicIcon(name: String): ImageVector {
    return when (name) {
        "Física" -> Icons.Default.Hub
        "Química" -> Icons.Default.Science
        "Biologia" -> Icons.Default.Eco
        "Matemática" -> Icons.Default.Calculate
        else -> Icons.Default.Book
    }
}

@Composable
private fun StudyPickerDialog(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val totalCards = topics.sumOf { it.cardCount }

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
                LazyColumn {
                    item {
                        StudyPickerRow(
                            name = "Todos os tópicos",
                            cardCount = totalCards,
                            color = PrimaryPurple,
                            onClick = { onSelect(ALL_TOPICS_ID) }
                        )
                    }
                    items(topics) { topic ->
                        StudyPickerRow(
                            name = topic.name,
                            cardCount = topic.cardCount,
                            color = Color(topic.colorArgb),
                            onClick = { onSelect(topic.id) }
                        )
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
