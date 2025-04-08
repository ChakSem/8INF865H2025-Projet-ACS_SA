package com.example.redcard.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import com.example.redcard.model.MusicPlayerManager
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.Player
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    themeViewModel: ThemeViewModel
) {
    // Musique de fond
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        MusicPlayerManager.playMusicGame(context)
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

    var isEyeOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Variables pour la gestion de la pop-up des mots secrets
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var showWordDialog by remember { mutableStateOf(false) }

    // Récupérer les joueurs enregistrés
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Filtrer les joueurs par rôle
    val footixPlayers = remember(registeredPlayers) {
        registeredPlayers.filter { it.role == "Footix" }
    }

    val remplacantPlayers = remember(registeredPlayers) {
        registeredPlayers.filter { it.role == "Remplaçant" }
    }

    // Vérifier s'il reste des joueurs
    val gameCanContinue = remember(registeredPlayers) {
        registeredPlayers.isNotEmpty()
    }

    RedCardTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Espacement accru entre les éléments
        ) {
            // Icône maison et titre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Accueil",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate("startingPage") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                )

                // Afficher le décompte des joueurs par rôle
                Text(
                    text = "Titulaires: ${registeredPlayers.count { it.role == "Titulaire" }} | " +
                            "Remplaçants: ${remplacantPlayers.size} | " +
                            "Footix: ${footixPlayers.size}",
                    fontSize = 14.sp
                )
            }

            // Titre avec instructions
            Text(
                text = "Décrivez votre mot",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Afficher la grille des joueurs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Afficher les 3 premiers joueurs ou moins si pas assez
                for (i in 0 until minOf(3, registeredPlayers.size)) {
                    val player = registeredPlayers[i]
                    PlayerAvatar(
                        player = player,
                        onPlayerClick = {
                            selectedPlayer = player
                            showWordDialog = true
                        }
                    )
                }
            }

            // Afficher une deuxième rangée si plus de 3 joueurs
            if (registeredPlayers.size > 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 3 until minOf(6, registeredPlayers.size)) {
                        val player = registeredPlayers[i]
                        PlayerAvatar(
                            player = player,
                            onPlayerClick = {
                                selectedPlayer = player
                                showWordDialog = true
                            }
                        )
                    }
                }
            }

            // Espacer les éléments supplémentaires
            Spacer(modifier = Modifier.weight(1f))

            // Texte informatif sur l'état du jeu
            Text(
                text = if (gameCanContinue)
                    "La partie continue! Votez pour éliminer un joueur."
                else
                    "Tous les joueurs ont été éliminés!",
                fontSize = 16.sp,
                color = if (gameCanContinue) Color.Green else Color.Red
            )

            // Bouton "Passer au vote"
            Button(
                onClick = {
                    if (gameCanContinue) {
                        navController.navigate("voteScreen")
                    } else {
                        // Si plus de joueurs, passer à l'écran de victoire
                        navController.navigate("victoryScreen")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = if (gameCanContinue) "Passer au vote" else "Terminer la partie")
            }

            // Boutons en bas à droite
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("generalSettings")
                    },
                    modifier = Modifier.size(40.dp)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Réglages",
                        tint = iconColor
                    )
                }
            }
        }
    }

    // Dialogue pour afficher le mot secret
    if (showWordDialog && selectedPlayer != null) {
        AlertDialog(
            onDismissRequest = { showWordDialog = false },
            title = { Text(text = selectedPlayer!!.name) },
            text = {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedPlayer!!.role == "Footix")
                            "Mot secret : Rien"
                        else
                            "Mot secret : ${selectedPlayer!!.word ?: "Non défini"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showWordDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun PlayerAvatar(
    player: Player,
    onPlayerClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onPlayerClick() }
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
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = player.name,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}