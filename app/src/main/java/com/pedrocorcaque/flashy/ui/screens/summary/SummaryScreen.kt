package com.pedrocorcaque.flashy.ui.screens.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.data.util.formatEpochDay
import com.pedrocorcaque.flashy.ui.theme.*
import com.pedrocorcaque.flashy.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Resumo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Período
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeChip("7 dias", state.rangeDays == 7) { viewModel.onRangeChange(7) }
                RangeChip("30 dias", state.rangeDays == 30) { viewModel.onRangeChange(30) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Números do período
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Acertos", state.totalCorrect.toString(), SuccessGreen, Modifier.weight(1f))
                StatCard("Erros", state.totalWrong.toString(), ErrorRed, Modifier.weight(1f))
                StatCard(
                    "Taxa de acerto",
                    if (state.totalAnswers == 0) "–" else "${state.accuracyPercent}%",
                    PrimaryPurple,
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gráfico
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceColor
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Evolução por dia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(SuccessGreen, "Acertos")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendDot(ErrorRed, "Erros")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.totalAnswers == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ainda não há respostas neste período.\nEstude alguns cards para ver o gráfico!",
                                color = TextGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        DailyLineChart(points = state.points)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tempo do cronômetro geral
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceColor
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tempo de estudo", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Somado pelo cronômetro da tela inicial",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = formatDuration(state.studyMs),
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}

/** Gráfico de linhas: acertos (verde) e erros (vermelho) por dia, desenhado no Canvas. */
@Composable
private fun DailyLineChart(points: List<DayPoint>, modifier: Modifier = Modifier) {
    val maxValue = maxOf(1, points.maxOfOrNull { maxOf(it.correct, it.wrong) } ?: 1)
    val gridColor = TextGray.copy(alpha = 0.2f)
    val yAxisWidth = 32.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Escala vertical: máximo no topo, zero embaixo
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .fillMaxHeight()
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = maxValue.toString(), color = TextGray, fontSize = 10.sp)
                Text(text = "0", color = TextGray, fontSize = 10.sp)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val w = size.width
                val h = size.height

                // Linhas de grade
                for (i in 0..4) {
                    val y = h * i / 4f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                }

                fun xAt(index: Int): Float =
                    if (points.size <= 1) w / 2f else w * index / (points.size - 1f)

                fun yAt(value: Int): Float = h - (value / maxValue.toFloat()) * h

                fun drawSeries(color: Color, valueOf: (DayPoint) -> Int) {
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = xAt(index)
                        val y = yAt(valueOf(point))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Poucos dias: marca cada dia com um ponto
                    if (points.size <= 14) {
                        points.forEachIndexed { index, point ->
                            drawCircle(color, radius = 4.dp.toPx(), center = Offset(xAt(index), yAt(valueOf(point))))
                        }
                    }
                }

                drawSeries(ErrorRed) { it.wrong }
                drawSeries(SuccessGreen) { it.correct }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Datas: primeiro, meio e último dia
        if (points.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = yAxisWidth),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatEpochDay(points.first().day), color = TextGray, fontSize = 10.sp)
                if (points.size >= 3) {
                    Text(formatEpochDay(points[points.size / 2].day), color = TextGray, fontSize = 10.sp)
                }
                if (points.size >= 2) {
                    Text(formatEpochDay(points.last().day), color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}
