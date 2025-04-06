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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Ajout du scope de coroutine manquant
    val scope = rememberCoroutineScope()

    // Animation pour alterner la visibilité du texte
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
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
                if (registeredPlayers.isNotEmpty()) {
                    showContinueDialog = true
                } else {
                    // Si aucun joueur enregistré, aller directement à la configuration
                    navController.navigate("startingPage") {
                        // Clear the back stack to prevent going back to the home screen
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Le reste du code reste inchangé
            Spacer(modifier = Modifier.height(16.dp))

            // Entête avec icônes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flag_fr),
                    contentDescription = "Accueil",
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
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo Red Card",
                modifier = Modifier
                    .width(400.dp)
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))


            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(animationSpec = tween(700)),
                exit = fadeOut(animationSpec = tween(700))
            ) {
                Text(
                    text = "Appuyer pour continuer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}