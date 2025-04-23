package com.example.redcard.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.Player
import com.example.redcard.data.dataStore
import kotlinx.coroutines.delay

@Composable
fun ChoosePlayerBallScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val totalPlayers by dataStoreManager.playersFlow.collectAsState(initial = 3)
    val secretWords by dataStoreManager.secretWordsFlow.collectAsState(initial = emptySet())
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())
    val allPlayersRegistered by dataStoreManager.allPlayersRegisteredFlow.collectAsState(initial = false)
    val availableBalls by dataStoreManager.availableBallsFlow.collectAsState(initial = emptyMap())
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val titulaireWord by dataStoreManager.titulaireWordFlow.collectAsState(initial = null)
    val remplacantWord by dataStoreManager.remplacantWordFlow.collectAsState(initial = null)

    // Initialisation des mots du jeu si nécessaire
    LaunchedEffect(Unit) {
        if (titulaireWord == null || remplacantWord == null) {
            val words = secretWords.toList().shuffled()
            if (words.size >= 2) {
                dataStoreManager.saveSecretWords(secretWords)
                context.dataStore.edit {
                    it[DataStoreManager.Companion.TITULAIRE_WORD_KEY] = words[0]
                    it[DataStoreManager.Companion.REMPLACANT_WORD_KEY] = words[1]
                }
            }
        }
    }

    LaunchedEffect(currentBackStackEntry) {
        dataStoreManager.resetPlayerPhoto()
    }
    
    // Création d'une liste des ballons disponibles
    val availableRolesList = remember(availableBalls) {
        availableBalls.flatMap { (role, count) ->
            List(count) { role }
        }.shuffled() // Mélange pour randomiser l'ordre
    }

    // Navigation automatique quand tous les joueurs sont inscrits
    LaunchedEffect(allPlayersRegistered) {
        if (allPlayersRegistered) {
            Toast.makeText(
                context,
                "Tous les joueurs sont inscrits ! La partie va commencer.",
                Toast.LENGTH_LONG
            ).show()

            delay(2000)

            navController.navigate("gameScreen") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choisis ton ballon") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Joueurs inscrits: ${registeredPlayers.size}/${totalPlayers}",
                style = MaterialTheme.typography.headlineSmall
            )

            // Liste des joueurs déjà inscrits
            if (registeredPlayers.isNotEmpty()) {
                Text(
                    text = "Joueurs inscrits:",
                    style = MaterialTheme.typography.titleMedium
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(registeredPlayers) { player ->
                        RegisteredPlayerItem(player = player)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Gestion des erreurs et affichage des ballons
            if (availableRolesList.isEmpty() && !allPlayersRegistered) {
                Text(
                    text = "Erreur: Aucun ballon disponible. Veuillez reconfigurer les rôles.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!allPlayersRegistered) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(availableRolesList) { role ->
                        FootballBallButton(
                            role = role,
                            isSelected = false,
                            onClick = {
                                scope.launch {
                                    when (role) {
                                        "Footix" -> dataStoreManager.saveSelectedBall(null, role)
                                        "Titulaire" -> dataStoreManager.saveSelectedBall(titulaireWord, role)
                                        "Remplaçant" -> dataStoreManager.saveSelectedBall(remplacantWord, role)
                                    }
                                    navController.navigate("PlayerSetupScreen")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisteredPlayerItem(
    player: Player,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
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
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C65EA))
            ) {
                Text(
                    text = player.name.first().toString(),
                    fontSize = 40.sp,
                    color = Color.White,
                )
            }
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
fun FootballBallButton(
    role: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = !isSelected,
        modifier = modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SportsSoccer,
                contentDescription = "Ballon de football",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            // Affiche "Joueur" pour masquer le rôle
            Text(
                text = "Joueur",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}