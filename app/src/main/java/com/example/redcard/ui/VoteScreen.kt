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
    themeViewModel: ThemeViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())
    val eliminablePlayers = registeredPlayers


    // Fonction d'élimination d'un joueur sélectionné
    fun eliminatePlayer() {
        selectedPlayer?.let { player ->
            scope.launch {
                val updatedPlayers = registeredPlayers.filter { it.id != player.id }

                context.dataStore.edit { preferences ->
                    val playersJson = kotlinx.serialization.json.Json.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(Player.serializer()),
                        updatedPlayers
                    )
                    preferences[DataStoreManager.REGISTERED_PLAYERS_KEY] = playersJson
                }

                // Vérification des conditions de fin de partie après élimination
                val newTitulaireCount = updatedPlayers.count { it.role == "Titulaire" }
                val newFootixCount = updatedPlayers.count { it.role == "Footix" }
                val newRemplacantCount = updatedPlayers.count { it.role == "Remplaçant" }
                val newImpostorCount = newFootixCount + newRemplacantCount

                // Navigation vers l'écran de victoire si conditions remplies
                if (newTitulaireCount == 0 || newImpostorCount == 0 || updatedPlayers.isEmpty() ||
                    (newTitulaireCount == 1 && newImpostorCount == 1)) {
                    navController.navigate("victoryScreen")
                } else {
                    navController.navigate("gameScreen")
                }
            }
        }
    }

    // Configuration du thème
    val currentTheme by themeViewModel.theme.collectAsState()
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
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
            // En-tête avec navigation et titre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("startingPage") }) {
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

                IconButton(onClick = { navController.navigate("generalSettings") }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Réglages",
                        modifier = Modifier.size(40.dp),
                        tint = iconColor
                    )
                }
            }

            Text(
                text = "Sélectionnez un joueur à éliminer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Liste des joueurs à éliminer
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

            // Bouton d'élimination
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

        }
    }
}