package com.example.redcard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.redcard.model.TurnManager
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel,
    turnManager: TurnManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Récupérer les joueurs enregistrés
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Récupérer les informations de tour actuel
    val currentTurn by turnManager.currentTurnFlow.collectAsState(initial = 1)
    val playersOrder by turnManager.playersOrderFlow.collectAsState(initial = emptyList())
    val currentPlayerIndex by turnManager.currentPlayerIndexFlow.collectAsState(initial = 0)

    // Récupérer le mot secret des titulaires
    val titulaireWord by dataStoreManager.titulaireWordFlow.collectAsState(initial = null)

    // État pour gérer l'affichage du mot
    var showWord by remember { mutableStateOf(false) }

    // État pour le joueur sélectionné pour voir son mot secret
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    // État pour contrôler l'affichage de la boîte de dialogue
    var showWordDialog by remember { mutableStateOf(false) }

    // État pour contrôler si le mot est visible dans la boîte de dialogue
    var isWordVisible by remember { mutableStateOf(false) }

    // État pour suivre si la vérification initiale a été faite
    var initialCheckDone by remember { mutableStateOf(false) }

    // Compter les joueurs par rôle
    val titulaireCount = registeredPlayers.count { it.role == "Titulaire" }
    val footixCount = registeredPlayers.count { it.role == "Footix" }
    val remplacantCount = registeredPlayers.count { it.role == "Remplaçant" }

    // Compter les imposteurs (Remplaçants + Footix)
    val impostorCount = footixCount + remplacantCount

    // Vérifier si le jeu peut continuer
    val gameCanContinue = titulaireCount > 0 && impostorCount > 0

    // Obtenir le joueur actuel s'il existe
    val currentPlayer = if (playersOrder.isNotEmpty() && currentPlayerIndex < playersOrder.size) {
        playersOrder[currentPlayerIndex]
    } else null

    // Vérifier si tous les joueurs ont joué dans ce tour
    val isLastPlayerInTurn = currentPlayerIndex >= playersOrder.size - 1

    // Observer le thème actuel
    val currentTheme by themeViewModel.theme.collectAsState()

    // Déterminer si le thème est sombre ou clair
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme() // Utiliser le thème système par défaut
    }

    // Initialiser l'ordre des joueurs au premier chargement ou quand les joueurs changent
    LaunchedEffect(registeredPlayers) {
        // Démarrer la musique de jeu
        MusicPlayerManager.playMusicGame(context)

        // Synchroniser les joueurs du TurnManager avec ceux du DataStore
        if (registeredPlayers.isNotEmpty()) {
            turnManager.syncPlayersWithDataStore(dataStoreManager.registeredPlayersFlow)
        }
    }

    // Vérifier les conditions de victoire après que toutes les données sont chargées
    LaunchedEffect(registeredPlayers) {
        // Seulement si nous avons des joueurs et que la vérification initiale n'a pas été faite
        if (registeredPlayers.isNotEmpty() && initialCheckDone) {
            // Ne vérifier les conditions de victoire que si le jeu ne peut pas continuer
            if (!gameCanContinue) {
                // Si le jeu ne peut plus continuer, aller à l'écran de victoire
                navController.navigate("victoryScreen")
            }
        }
    }

    // Effectuer la vérification initiale après un petit délai pour s'assurer que tout est chargé
    LaunchedEffect(Unit) {
        // Attendre que les données soient chargées avant de faire la vérification
        kotlinx.coroutines.delay(300)
        initialCheckDone = true
    }

    // Boîte de dialogue pour afficher le mot secret
    if (showWordDialog) {
        AlertDialog(
            onDismissRequest = {
                showWordDialog = false
                isWordVisible = false
            },
            title = { Text("Mot Secret") },
            text = {
                Column {
                    Text("Joueur: ${selectedPlayer?.name}")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isWordVisible) {
                        val wordToShow = when (selectedPlayer?.role) {
                            "Titulaire" -> titulaireWord ?: "Pas de mot défini"
                            "Remplaçant" -> "Ne connaît pas le mot secret"
                            "Footix" -> "N'a pas de mot secret"
                            else -> "Rôle inconnu"
                        }
                        Text("Mot secret: $wordToShow")
                    } else {
                        Button(
                            onClick = { isWordVisible = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Afficher le mot secret")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWordDialog = false
                        isWordVisible = false
                    }
                ) {
                    Text("Fermer")
                }
            }
        )
    }

    RedCardTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // En-tête avec informations de tour et boutons
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
                            popUpTo("startingPage") { inclusive = true }
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
                    text = "Tour ${currentTurn}",
                    fontSize = 20.sp,
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

            // Texte explicatif
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tour $currentTurn",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Les joueurs doivent proposer un mot en suivant l'ordre ci-dessous. Une fois tous les joueurs passés, vous pourrez voter pour éliminer un joueur.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Ordre de passage des joueurs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ordre de passage",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(playersOrder) { index, player ->
                            val isCurrentPlayer = index == currentPlayerIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isCurrentPlayer) 2.dp else 1.dp,
                                        color = if (isCurrentPlayer)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        color = if (isCurrentPlayer)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        // Afficher la boîte de dialogue du mot secret
                                        selectedPlayer = player
                                        showWordDialog = true
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                if (player.photoUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = player.photoUri),
                                        contentDescription = "Photo de ${player.name}",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = when (player.role) {
                                                    "Titulaire" -> Color.Blue.copy(alpha = 0.7f)
                                                    "Remplaçant" -> Color.Green.copy(alpha = 0.7f)
                                                    "Footix" -> Color.Yellow.copy(alpha = 0.7f)
                                                    else -> Color.Gray
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Joueur",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = player.name,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Bouton pour aller directement au vote
            Button(
                onClick = {
                    scope.launch {
                        navController.navigate("voteScreen")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.HowToVote,
                    contentDescription = "Aller au vote",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Passer au vote",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}