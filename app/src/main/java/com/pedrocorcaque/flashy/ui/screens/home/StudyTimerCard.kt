package com.pedrocorcaque.flashy.ui.screens.home

import android.content.Context
import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.ui.screens.addedit.FlashyTextField
import com.pedrocorcaque.flashy.ui.theme.PrimaryPurple
import com.pedrocorcaque.flashy.ui.theme.SurfaceColor
import com.pedrocorcaque.flashy.ui.theme.TextGray
import com.pedrocorcaque.flashy.ui.util.formatDuration
import com.pedrocorcaque.flashy.ui.util.formatElapsed

/**
 * Cartão de tempo da Home, com dois modos: cronômetro (conta para cima) e timer (conta para baixo,
 * com duração configurável). Os dois somam no tempo diário do Resumo.
 */
@Composable
fun StudyTimerCard(
    state: TimerUiState,
    todayMs: Long,
    onModeChange: (TimerMode) -> Unit,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onConfigure: () -> Unit
) {
    val isCountdown = state.mode == TimerMode.COUNTDOWN

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Seletor de modo (bloqueado enquanto algo está rodando)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimerMode.values().forEach { mode ->
                    ModeChip(
                        label = mode.label,
                        selected = state.mode == mode,
                        enabled = !state.isRunning,
                        onClick = { onModeChange(mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCountdown) {
                            "Timer de ${state.countdownTotalMs / 60_000} min · hoje: ${formatDuration(todayMs)}"
                        } else {
                            "Cronômetro de estudo · hoje: ${formatDuration(todayMs)}"
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatElapsed(state.displayMs),
                            modifier = if (isCountdown && !state.isRunning) {
                                Modifier.clickable { onConfigure() }
                            } else {
                                Modifier
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFeatureSettings = "tnum"
                            )
                        )
                        if (isCountdown && !state.isRunning) {
                            IconButton(onClick = onConfigure, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Configurar tempo do timer",
                                    tint = TextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onReset, enabled = state.canReset) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = if (isCountdown) "Reiniciar timer" else "Zerar cronômetro",
                        tint = if (state.canReset) TextGray else TextGray.copy(alpha = 0.3f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onToggle,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = PrimaryPurple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isRunning) "Pausar" else "Iniciar"
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(50),
        color = if (selected) PrimaryPurple else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            TextGray.copy(alpha = if (enabled) 0.5f else 0.2f)
        )
    ) {
        Text(
            text = label,
            color = if (selected || enabled) Color.White else TextGray.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/** Diálogo para escolher a duração do timer: atalhos (5, 15, 25, 45, 60 min) ou qualquer valor. */
@Composable
fun TimerDurationDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentMinutes.toString()) }
    val minutes = text.toIntOrNull()
    val isValid = minutes != null && minutes in 1..600

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text("Tempo do timer", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 15, 25, 45, 60).forEach { preset ->
                        Surface(
                            modifier = Modifier.clickable { text = preset.toString() },
                            shape = RoundedCornerShape(50),
                            color = if (minutes == preset) PrimaryPurple else Color.Transparent,
                            border = if (minutes == preset) null
                            else androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "$preset",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                FlashyTextField(
                    value = text,
                    onValueChange = { new -> text = new.filter { it.isDigit() }.take(3) },
                    placeholder = "Minutos (1 a 600)",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("minutos", color = TextGray, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(minutes ?: currentMinutes) }, enabled = isValid) {
                Text("Definir", color = if (isValid) PrimaryPurple else TextGray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
        }
    )
}

@Composable
fun TimerFinishedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text("Tempo esgotado! ⏰", color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text("Seu tempo de estudo acabou. Hora de uma pausa?", color = TextGray) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = PrimaryPurple) }
        }
    )
}

/** Toca o som de notificação padrão do aparelho (não precisa de permissão). */
fun playTimerSound(context: Context) {
    try {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, uri)?.play()
    } catch (e: Exception) {
        // Sem som disponível: o diálogo já avisa.
    }
}
