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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import com.example.redcard.model.MusicPlayerManager
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun GeneralSettingScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel
) {
    val scope = rememberCoroutineScope()

    // Observer l'état du thème
    val currentTheme by themeViewModel.theme.collectAsState()

    var notificationsEnabled by remember { mutableStateOf(true) }

    var themeExpanded by remember { mutableStateOf(false) }

    // Initialiser selectedTheme selon le thème actuel
    var selectedTheme by remember { mutableStateOf(
        when (currentTheme) {
            AppTheme.CLAIR -> "Clair"
            AppTheme.SOMBRE -> "Sombre"
            else -> "Système"
        }
    ) }

    val themeOptions = listOf("Clair", "Sombre", "Système")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Son
            val context = LocalContext.current
            var musicEnabled by remember { mutableStateOf(MusicPlayerManager.isPlaying) }

            SettingItem(
                icon = Icons.Default.VolumeUp,
                title = "Son",
                trailing = {
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = {
                            musicEnabled = it
                            scope.launch {
                                dataStoreManager.setMusicEnabled(it)
                                MusicPlayerManager.toggleMusic(
                                    enabled = it,
                                    context = context
                                ) {
                                    if (it) MusicPlayerManager.resumeMusic() else MusicPlayerManager.stopMusic()
                                }
                            }
                        }
                    )
                }
            )
            SettingDivider()

            SettingItem(
                icon = Icons.Default.DarkMode,
                title = "Apparence",
                trailing = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { themeExpanded = true }
                        ) {
                            Text(text = selectedTheme)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Choisir thème"
                            )
                        }
                        DropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            themeOptions.forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme) },
                                    onClick = {
                                        selectedTheme = theme
                                        themeExpanded = false

                                        when (theme) {
                                            "Clair" -> themeViewModel.setTheme(AppTheme.CLAIR)
                                            "Sombre" -> themeViewModel.setTheme(AppTheme.SOMBRE)
                                            "Système" -> themeViewModel.setTheme(AppTheme.SYSTEME)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
            // SettingDivider()

            // // Notifications
            // SettingItem(
            //     icon = Icons.Default.Notifications,
            //     title = "Notifications",
            //     trailing = {
            //         Switch(
            //             checked = notificationsEnabled,
            //             onCheckedChange = { notificationsEnabled = it }
            //         )
            //     }
            // )
            // SettingDivider()

            // // Évaluer l'application
            // SettingItem(
            //     icon = Icons.Default.Favorite,
            //     title = "Évaluer l'application",
            //     titleColor = Color.Red,
            //     onClick = {
            //         // Rediriger vers Play Store
            //     }
            // )
        }
    }
}




@Composable
fun SettingDivider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp),
        color = Color(0xFFE0E0E0), // Gris très clair
        thickness = 1.dp
    )
}



@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier.size(28.dp) 
            )

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


