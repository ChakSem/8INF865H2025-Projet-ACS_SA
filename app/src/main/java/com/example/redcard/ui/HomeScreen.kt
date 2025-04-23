package com.example.redcard.ui

import android.media.MediaPlayer
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
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(),
) {
    var isTextVisible by remember { mutableStateOf(true) }
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())
    val allPlayersRegistered by dataStoreManager.allPlayersRegisteredFlow.collectAsState(initial = false)
    var showContinueDialog by remember { mutableStateOf(false) }
    val gameCompleted by dataStoreManager.gameCompletedFlow.collectAsState(initial = false)
    val musicEnabled by dataStoreManager.musicEnabledFlow.collectAsState(initial = true)

    // Ajout du scope de coroutine manquant
    val scope = rememberCoroutineScope()

    // Musique de fond
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (musicEnabled) {
            MusicPlayerManager.playMusicIntro(context)
        }
    }

    // Animation pour alterner la visibilité du texte
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Délai avant l'animation inverse
            isTextVisible = !isTextVisible
        }
    }

    // Boîte de dialogue pour demander à l'utilisateur s'il souhaite continuer le jeu en cours
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
                                // Forcer un rafraîchissement des données avant de vérifier
                                dataStoreManager.forceRefreshRegisteredPlayers()

                                if (allPlayersRegistered) {
                                    // Si tous les joueurs sont enregistrés, naviguer vers la page principale du jeu
                                    navController.navigate("ChoosePlayerBallScreen?refresh=true")
                                } else {
                                    // S'il manque des joueurs, continuer l'enregistrement
                                    // Correction: complétez la navigation vers la page d'ajout de joueurs
                                    navController.navigate("PlayerSetupScreen")
                                }
                            } catch (e: Exception) {
                                // En cas d'erreur, naviguer vers un endroit sûr
                                navController.navigate("ChooseConfigurationScreen") {
                                    // Clear the back stack to prevent going back to the home screen
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    }
                ) {
                    Text("Continuer")
                }
            },
            // Bouton pour commencer une nouvelle partie
            dismissButton = {
                Button(
                    onClick = {
                        showContinueDialog = false
                        scope.launch {
                            // Réinitialiser le jeu avant de commencer une nouvelle partie
                            dataStoreManager.resetGame()
                            // S'assurer que cette route existe
                            navController.navigate("gameConfiguration") {
                                // Clear the back stack to prevent going back to the home screen
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
                    // Si aucun joueur enregistré ou partie terminée, aller directement à la configuration
                    navController.navigate("startingPage") {
                        // Clear the back stack to prevent going back to the home screen
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Image de fond
        Image(
            painter = painterResource(id = R.drawable.background_image), // Remplace 'background_image' par ton image de fond
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Cette option permet d'ajuster l'image pour qu'elle couvre tout l'écran
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icône maison et icône settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Utilisation de Coil pour afficher le SVG
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.drapeau_francais), // SVG du drapeau français
                    contentDescription = "Drapeau Français",
                    modifier = Modifier.size(40.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onSettingsClick()
                            // Vérifier que cette route existe
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
                enter = fadeIn(animationSpec = tween(700)), // Apparition douce
                exit = fadeOut(animationSpec = tween(700))  // Disparition douce
            ) {
                Text(
                    text = "APPUYEZ",
                    fontSize = 30.sp, // Texte plus gros
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // Texte en blanc
                    fontFamily = FontFamily(Font(R.font.rubik_medium_italic)), // Police Rubik
                    modifier = Modifier.padding(top = 20.dp) // Spacer au-dessus du texte
                )
            }
        }
    }
}