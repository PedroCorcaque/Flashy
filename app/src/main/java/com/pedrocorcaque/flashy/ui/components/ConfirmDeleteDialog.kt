package com.pedrocorcaque.flashy.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.pedrocorcaque.flashy.ui.theme.ErrorRed
import com.pedrocorcaque.flashy.ui.theme.SurfaceColor
import com.pedrocorcaque.flashy.ui.theme.TextGray

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Excluir"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextGray) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = ErrorRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
        }
    )
}
