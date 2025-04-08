package com.example.redcard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.Player
import com.example.redcard.model.MusicPlayerManager
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun VictoryScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Récupérer les joueurs enregistrés
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Filtrer les joueurs par rôle
    val titulairePlayers = remember(registeredPlayers) {
        registeredPlayers.filter { it.role == "Titulaire" }
    }

    val remplacantPlayers = remember(registeredPlayers) {
        registeredPlayers.filter { it.role == "Remplaçant" }
    }

    val footixPlayers = remember(registeredPlayers) {
        registeredPlayers.filter { it.role == "Footix" }
    }

    // Compter les imposteurs (Remplaçants + Footix)
    val impostorCount = remplacantPlayers.size + footixPlayers.size

    // Déterminer les vainqueurs en fonction des joueurs restants
    val winners = when {
        titulairePlayers.isNotEmpty() && impostorCount == 0 -> "Les Titulaires"
        titulairePlayers.isEmpty() && remplacantPlayers.isNotEmpty() && footixPlayers.isEmpty() -> "Les Remplaçants"
        titulairePlayers.isEmpty() && remplacantPlayers.isEmpty() && footixPlayers.isNotEmpty() -> "Les Footix"
        titulairePlayers.isEmpty() && impostorCount > 0 -> "Les Imposteurs" // Cas où les remplaçants ET footix restent
        else -> "Personne"
    }

    // Récupérer le mot secret des titulaires
    val titulaireWord by dataStoreManager.titulaireWordFlow.collectAsState(initial = null)

    // État pour afficher les détails du jeu
    var showDetails by remember { mutableStateOf(false) }

    // Fonction pour réinitialiser le jeu
    fun resetGame() {
        scope.launch {
            dataStoreManager.resetGame()
            navController.navigate("startingPage") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Observer le thème actuel
    val currentTheme by themeViewModel.theme.collectAsState()

    // Déterminer si le thème est sombre ou clair
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme() // Utiliser le thème système par défaut
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = if (darkTheme) Color.White else Color.Black

    // Musique de fond

    LaunchedEffect(Unit) {
        MusicPlayerManager.playMusicVictory(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête avec le titre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("startingPage") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Accueil",
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Fin de la Partie",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    navController.navigate("generalSettings")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Carte de victoire
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Trophée",
                    tint = Color(0xFFFFD700), // Couleur or
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$winners ont gagné !",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Afficher le mot secret si disponible et si les titulaires ont gagné
                if (winners == "Les Titulaires") {
                    titulaireWord?.let { word ->
                        Text(
                            text = "Mot secret: $word",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Si les imposteurs ont gagné, afficher un message spécial
                if (winners == "Les Imposteurs") {
                    Text(
                        text = "Les Remplaçants et les Footix ont unis leurs forces !",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Afficher aussi le mot secret des titulaires qu'ils ont réussi à éliminer
                    titulaireWord?.let { word ->
                        Text(
                            text = "Le mot secret des titulaires était: $word",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Afficher les joueurs restants
        if (registeredPlayers.isNotEmpty()) {
            Text(
                text = "Joueurs restants:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(registeredPlayers) { player ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (player.photoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = player.photoUri),
                                contentDescription = "Photo de ${player.name}",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = when (player.role) {
                                            "Titulaire" -> Color.Blue.copy(alpha = 0.7f)
                                            "Remplaçant" -> Color.Green.copy(alpha = 0.7f)
                                            "Footix" -> Color.Yellow.copy(alpha = 0.7f)
                                            else -> Color.Gray
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Joueur",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Text(
                            text = player.name,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Afficher le rôle à la fin du jeu
                        Text(
                            text = player.role,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Tous les joueurs ont été éliminés!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Bouton pour afficher/masquer les détails
        OutlinedButton(
            onClick = { showDetails = !showDetails },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = if (showDetails) "Masquer les détails" else "Afficher les détails")
        }

        // Détails du jeu
        AnimatedVisibility(visible = showDetails) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Récapitulatif de la partie",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Afficher les statistiques de la partie
                Text(
                    text = "Nombre total de joueurs restants: ${registeredPlayers.size}",
                    fontSize = 14.sp
                )

                Text(
                    text = "Titulaires: ${titulairePlayers.size}",
                    fontSize = 14.sp
                )

                Text(
                    text = "Remplaçants: ${remplacantPlayers.size}",
                    fontSize = 14.sp
                )

                Text(
                    text = "Footix: ${footixPlayers.size}",
                    fontSize = 14.sp
                )

                if (winners == "Les Imposteurs") {
                    Text(
                        text = "Total Imposteurs: $impostorCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Mot secret des titulaires: ${titulaireWord ?: "Inconnu"}",
                    fontSize = 14.sp
                )
            }
        }

        // Boutons d'action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bouton Rejouer
            Button(
                onClick = {
                    scope.launch {
                        MusicPlayerManager.playMusicSalon(context) // 🎵 Lancer la musique d'intro
                        dataStoreManager.resetGame()
                        navController.navigate("ChoosePlayerBallScreen")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Rejouer",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Rejouer")
            }


            // Bouton Retour à l'accueil
            OutlinedButton(
                onClick = {
                    resetGame()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Accueil",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Retour à l'accueil")
            }
        }
    }
}