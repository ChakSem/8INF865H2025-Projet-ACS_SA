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
import com.example.redcard.data.dataStore
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun VoteScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    // Récupérer les joueurs enregistrés
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Filtrer les joueurs non titulaires (ceux qui peuvent être éliminés)
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

    // Fonction pour éliminer un joueur
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
                    // Sinon, retour à l'écran de jeu
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

            // Afficher les compteurs de rôles
            Text(
                text = "Titulaires: $titulaireCount | " +
                        "Remplaçants: $remplacantCount | " +
                        "Footix: $footixCount",
                fontSize = 14.sp
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

            // Cadran avec le texte
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Qui voulez-vous éliminer ?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
            }

        // Liste des joueurs pouvant être éliminés
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(eliminablePlayers) { player ->
                PlayerVoteItem(
                    player = player,
                    isSelected = selectedPlayer?.id == player.id,
                    onPlayerSelected = { selectedPlayer = player }
                )
            }
        }

        // Afficher un message si aucun joueur éliminable n'est disponible
        if (eliminablePlayers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pas de joueurs à éliminer !",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Boutons d'action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bouton Annuler
            OutlinedButton(
                onClick = {
                    navController.navigateUp()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Annuler")
            }

                // Bouton Confirmer
                Button(
                    onClick = {
                        eliminatePlayer()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedPlayer != null
                ) {
                    Text(text = "Confirmer")
                }
            }
        }
    }
}

@Composable
fun PlayerVoteItem(
    player: Player,
    isSelected: Boolean,
    onPlayerSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerSelected() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                Color.LightGray.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo du joueur ou avatar
            if (player.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = player.photoUri),
                    contentDescription = "Photo de ${player.name}",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Informations du joueur
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = player.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Icône de sélection
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Sélectionné",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}