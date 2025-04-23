package com.example.redcard.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import coil.compose.rememberAsyncImagePainter
import com.example.redcard.model.MusicPlayerManager
@Composable
fun HomeScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    onSettingsClick: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(),
) {
    var isTextVisible by remember { mutableStateOf(true) }
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())
    val allPlayersRegistered by dataStoreManager.allPlayersRegisteredFlow.collectAsState(initial = false)
    var showContinueDialog by remember { mutableStateOf(false) }
    val gameCompleted by dataStoreManager.gameCompletedFlow.collectAsState(initial = false)
    val musicEnabled by dataStoreManager.musicEnabledFlow.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (musicEnabled) {
            MusicPlayerManager.playMusicIntro(context)
        }
    }

    // Animation pour alterner la visibilité du texte
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            isTextVisible = !isTextVisible
        }
    }

    if (showContinueDialog) {
        AlertDialog(
            onDismissRequest = { showContinueDialog = false },
            title = { Text("Partie en cours") },
            text = { Text("Voulez-vous continuer la partie en cours ou commencer une nouvelle partie ?") },
            confirmButton = {
                Button(
                    onClick = {
                        showContinueDialog = false
                        scope.launch {
                            try {
                                dataStoreManager.forceRefreshRegisteredPlayers()

                                if (allPlayersRegistered) {
                                    navController.navigate("ChoosePlayerBallScreen?refresh=true")
                                } else {
                                    navController.navigate("PlayerSetupScreen")
                                }
                            } catch (e: Exception) {
                                navController.navigate("ChooseConfigurationScreen") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    }
                ) {
                    Text("Continuer")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showContinueDialog = false
                        scope.launch {
                            dataStoreManager.resetGame()
                            navController.navigate("gameConfiguration") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Nouvelle partie")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clickable {
                if (registeredPlayers.isNotEmpty() && !gameCompleted) {
                    showContinueDialog = true
                } else {
                    navController.navigate("startingPage") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Image de fond
        Image(
            painter = painterResource(id = R.drawable.background_image), 
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop 
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // En-tête avec icônes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showLanguageDialog by remember { mutableStateOf(false) }

                if (showLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text("Langue disponible") },
                        text = { Text("Pour la version actuelle, seul le français est disponible.") },
                        confirmButton = {
                            Button(onClick = { showLanguageDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                Image(
                    painter = rememberAsyncImagePainter(R.drawable.drapeau_francais),
                    contentDescription = "Drapeau Français",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            showLanguageDialog = true
                        }
                )

                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onSettingsClick()
                            navController.navigate("generalSettings") {
                                launchSingleTop = true
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Logo principal
            Image(
                painter = painterResource(id = R.drawable.redcard_logo),
                contentDescription = "Logo Red Card",
                modifier = Modifier
                    .width(400.dp)
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Texte avec animation de fondu progressif
            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(animationSpec = tween(700)),
                exit = fadeOut(animationSpec = tween(700))
            ) {
                Text(
                    text = "APPUYEZ",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.rubik_medium_italic)),
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}