package com.example.redcard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.Player
import com.example.redcard.model.TurnManager
import com.example.redcard.data.dataStore
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun VoteScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel,
    turnManager: TurnManager // Ajout du TurnManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    // Récupérer les joueurs enregistrés
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Récupérer le tour actuel
    val currentTurn by turnManager.currentTurnFlow.collectAsState(initial = 1)

    // Filtrer les joueurs pour le vote
    val eliminablePlayers = registeredPlayers

    // Compter les joueurs par rôle
    val titulaireCount = remember(registeredPlayers) {
        registeredPlayers.count { it.role == "Titulaire" }
    }

    // Analyser les joueurs par rôle pour déterminer l'état du jeu
    val footixCount = remember(registeredPlayers) {
        registeredPlayers.count { it.role == "Footix" }
    }

    val remplacantCount = remember(registeredPlayers) {
        registeredPlayers.count { it.role == "Remplaçant" }
    }

    // Compter les imposteurs (Remplaçants + Footix)
    val impostorCount = remember(footixCount, remplacantCount) {
        footixCount + remplacantCount
    }

    val gameCanContinue = remember(titulaireCount, impostorCount) {
        titulaireCount > 0 && impostorCount > 0
    }

    // Fonction pour éliminer un joueur et passer au tour suivant
    fun eliminatePlayer() {
        selectedPlayer?.let { player ->
            scope.launch {
                // Créer une nouvelle liste sans le joueur sélectionné
                val updatedPlayers = registeredPlayers.filter { it.id != player.id }

                // Mettre à jour le DataStore
                context.dataStore.edit { preferences ->
                    val playersJson = kotlinx.serialization.json.Json.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(Player.serializer()),
                        updatedPlayers
                    )
                    preferences[DataStoreManager.REGISTERED_PLAYERS_KEY] = playersJson
                }

                // Vérifier l'état du jeu après l'élimination
                val newTitulaireCount = updatedPlayers.count { it.role == "Titulaire" }
                val newFootixCount = updatedPlayers.count { it.role == "Footix" }
                val newRemplacantCount = updatedPlayers.count { it.role == "Remplaçant" }
                val newImpostorCount = newFootixCount + newRemplacantCount

                // Cas de victoire
                if (newTitulaireCount == 0 || newImpostorCount == 0 || updatedPlayers.isEmpty()) {
                    // Passage à l'écran de victoire
                    navController.navigate("victoryScreen")
                } else {
                    // Passer au tour suivant
                    turnManager.moveToNextTurn()

                    // Retour à l'écran de jeu
                    navController.navigate("gameScreen")
                }
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

    RedCardTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row en haut avec la maison et les réglages
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("startingPage")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Accueil",
                        modifier = Modifier.size(40.dp),
                        tint = iconColor
                    )
                }

                Text(
                    text = "Éliminer un joueur",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                IconButton(
                    onClick = {
                        navController.navigate("generalSettings")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Réglages",
                        modifier = Modifier.size(40.dp),
                        tint = iconColor
                    )
                }
            }

            

            // Afficher le tour actuel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choisissez un joueur à éliminer",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Liste des joueurs pour le vote
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eliminablePlayers) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlayer = player },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPlayer?.id == player.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Photo ou avatar du joueur
                            if (player.photoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = player.photoUri),
                                    contentDescription = "Photo de ${player.name}",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (selectedPlayer?.id == player.id) 2.dp else 1.dp,
                                            color = if (selectedPlayer?.id == player.id)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(
                                            color = when (player.role) {
                                                "Titulaire" -> Color.Blue.copy(alpha = 0.7f)
                                                "Remplaçant" -> Color.Green.copy(alpha = 0.7f)
                                                "Footix" -> Color.Yellow.copy(alpha = 0.7f)
                                                else -> Color.Gray
                                            },
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = if (selectedPlayer?.id == player.id) 2.dp else 1.dp,
                                            color = if (selectedPlayer?.id == player.id)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Joueur",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = player.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            // Indicateur de sélection
                            if (selectedPlayer?.id == player.id) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Sélectionné",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Bouton de validation du vote
            Button(
                onClick = { eliminatePlayer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = selectedPlayer != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Éliminer",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Éliminer le joueur",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bouton pour passer l'élimination (optionnel)
            TextButton(
                onClick = {
                    scope.launch {
                        // Passer au tour suivant sans éliminer
                        turnManager.moveToNextTurn()
                        navController.navigate("gameScreen")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Passer cette élimination",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}