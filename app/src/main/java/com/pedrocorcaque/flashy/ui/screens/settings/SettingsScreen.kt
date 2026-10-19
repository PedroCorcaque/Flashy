package com.pedrocorcaque.flashy.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrocorcaque.flashy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configurações",
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
            Spacer(modifier = Modifier.height(24.dp))

            SettingsItem(
                icon = Icons.Default.SettingsBrightness,
                title = "Aparência",
                subtitle = "Tema escuro"
            )
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notificações",
                subtitle = "Lembretes de estudo"
            )
            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = "Backup local",
                subtitle = "Exportar / Importar"
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Sobre",
                subtitle = "Flashy v1.0"
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pequenos passos\ntambém são grandes\nconquistas. ♡",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = PrimaryPurple,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Simple star indicators like in the design
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("✦", color = PrimaryPurple.copy(alpha = 0.5f))
                    Text("✦", color = PrimaryPurple.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        onClick = { },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp
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
