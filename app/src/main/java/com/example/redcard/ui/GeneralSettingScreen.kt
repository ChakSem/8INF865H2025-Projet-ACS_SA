package com.example.redcard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager

@Composable
fun GeneralSettingScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager
) {
    val scope = rememberCoroutineScope()
    var soundEnabled by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf("Français") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Son
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Son",
                trailing = {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            )

            // Langue
            SettingItem(
                iconPainter = painterResource(id = R.drawable.ic_flag_fr),
                title = "Langue",
                trailing = {
                    Text(
                        text = currentLanguage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    // Ouvrir le sélecteur de langue
                }
            )

            // Apparence
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Apparence",
                onClick = {
                    // Ouvrir les paramètres d'apparence
                }
            )

            // Système
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Système",
                onClick = {
                    // Ouvrir les paramètres système
                }
            )

            // Notifications
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Notifications",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )

            // Musique
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Musique",
                trailing = {
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = { musicEnabled = it }
                    )
                }
            )

            // Évaluer l'application
            SettingItem(
                icon = painterResource(id = R.drawable.ic_logo),
                title = "Évaluer l'application",
                titleColor = Color.Red,
                onClick = {
                    // Ouvrir la page d'évaluation
                }
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: Painter? = null,
    iconPainter: Painter? = null,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = titleColor
                )
            } else if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            trailing?.invoke()
        }
    }
}

