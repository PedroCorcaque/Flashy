package com.pedrocorcaque.flashy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.ui.theme.PrimaryPurple
import com.pedrocorcaque.flashy.ui.theme.SurfaceColor
import com.pedrocorcaque.flashy.ui.theme.TextGray
import com.pedrocorcaque.flashy.ui.util.SortOrder

/** Botão "filtro" que mostra a ordenação atual e abre um menu para trocá-la. */
@Composable
fun SortSelector(
    current: SortOrder,
    onSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Ordenar",
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(current.label, color = TextGray, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceColor)
        ) {
            SortOrder.values().forEach { order ->
                val isCurrent = order == current
                DropdownMenuItem(
                    text = { Text(order.label, color = if (isCurrent) PrimaryPurple else Color.White) },
                    trailingIcon = {
                        if (isCurrent) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelected(order)
                    }
                )
            }
        }
    }
}
