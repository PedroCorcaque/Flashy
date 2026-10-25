package com.pedrocorcaque.flashy.ui.screens.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.ui.theme.*
import com.pedrocorcaque.flashy.ui.util.formatElapsed

@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel,
    topicId: String,
    isInverted: Boolean = false,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val elapsedMs by viewModel.elapsedMs.collectAsState()
    val topic by viewModel.topic.collectAsState()
    val subtopic by viewModel.subtopic.collectAsState()
    val subtopicNames by viewModel.subtopicNames.collectAsState()

    val title = if (topicId == ALL_TOPICS_ID) {
        "Todos os tópicos"
    } else {
        listOfNotNull(topic?.name, subtopic?.name).joinToString(" · ")
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        }
        state.queue.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize().background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nenhum card para estudar aqui.", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) { Text("Voltar") }
                }
            }
        }
        state.isFinished -> {
            StudyResultScreen(
                title = title,
                round = state.round,
                correct = state.correctCount,
                total = state.total,
                wrongCount = state.wrongCards.size,
                elapsedMs = elapsedMs,
                onRetryWrong = viewModel::retryWrong,
                onBackClick = onBackClick
            )
        }
        else -> {
            // Estudando "todos" os subtópicos: mostra o subtópico do card que está na tela.
            val cardSubtopicName = if (subtopic == null) {
                state.queue.getOrNull(state.currentIndex)?.subtopicId?.let { subtopicNames[it] }
            } else {
                null
            }
            val studyTitle = listOfNotNull(title.ifEmpty { null }, cardSubtopicName).joinToString(" · ")

            StudyContent(
                state = state,
                title = studyTitle,
                elapsedMs = elapsedMs,
                isInverted = isInverted,
                onAnswer = viewModel::answer,
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyContent(
    state: StudyUiState,
    title: String,
    elapsedMs: Long,
    isInverted: Boolean,
    onAnswer: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }
    // Ao responder, o card volta para a frente sem animação (senão o verso do próximo card aparece por um instante).
    var snapBack by remember { mutableStateOf(false) }

    val currentCard = state.queue[state.currentIndex]

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = if (snapBack) snap() else tween(durationMillis = 400),
        label = "cardFlip"
    )

    val counter = "${state.currentIndex + 1} / ${state.total}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Text(
                        text = if (state.round > 1) "Revisão · $counter" else counter,
                        color = TextGray,
                        modifier = Modifier.padding(end = 16.dp),
                        fontSize = 14.sp
                    )
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
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Cronômetro da sessão
            Surface(
                shape = RoundedCornerShape(50),
                color = SurfaceColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Tempo",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatElapsed(elapsedMs),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable {
                        snapBack = false
                        isFlipped = !isFlipped
                    }
            ) {
                if (rotation <= 90f) {
                    if (!isInverted) CardFront(currentCard.front) else CardBack(currentCard.back, label = "PERGUNTA")
                } else {
                    Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                        if (!isInverted) CardBack(currentCard.back) else CardFront(currentCard.front)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Progress Indicators (dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayCount = minOf(state.total, 7)
                repeat(displayCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == state.currentIndex % displayCount) 8.dp else 6.dp)
                            .background(
                                color = if (index == state.currentIndex % displayCount) PrimaryPurple else TextGray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isFlipped) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            snapBack = true
                            isFlipped = false
                            onAnswer(true)
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                            Text("Acertei", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            snapBack = true
                            isFlipped = false
                            onAnswer(false)
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed)
                            Text("Errei", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                text = "Deslize ou toque para avançar",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun CardFront(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryPurple.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(48.dp))
            Icon(
                Icons.Default.AdsClick,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Toque para virar",
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CardBack(text: String, modifier: Modifier = Modifier, label: String = "RESPOSTA") {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, SecondaryPurple.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = SecondaryPurple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            )
        }
    }
}
