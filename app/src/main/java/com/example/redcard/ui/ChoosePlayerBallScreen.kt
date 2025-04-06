package com.example.redcard.ui

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
// import androidx.compose.material.icons.filled.Refresh // Commenté car lié au bouton de rechargement
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.Player
import kotlinx.coroutines.delay

@Composable
fun ChoosePlayerBallScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    // refresh: Boolean = false // Commenté car paramètre lié au rechargement
)  {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // var refreshTrigger by remember { mutableStateOf(0) } // Commenté car variable de rechargement
    // var showDebugDialog by remember { mutableStateOf(false) } // Commenté car dialogue de debug

    // Récupérer les arguments de navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Amélioration: utiliser une variable pour suivre le rafraîchissement
    // var shouldRefreshData by remember { mutableStateOf(false) } // Commenté car variable de rechargement

    // Vérifier les arguments de navigation et déclencher un rafraîchissement si nécessaire
    /* Commenté car bloc lié au rechargement
    LaunchedEffect(navBackStackEntry) {
        val refreshParam = navBackStackEntry?.arguments?.getString("refresh")
        if (refreshParam == "true") {
            shouldRefreshData = true
            refreshTrigger += 1
            // Réinitialiser l'argument
            navBackStackEntry?.arguments?.remove("refresh")
        }
    }
    */

    // Récupérer les données stockées
    val totalPlayers by dataStoreManager.playersFlow.collectAsState(initial = 3)
    val secretWords by dataStoreManager.secretWordsFlow.collectAsState(initial = emptySet())
    val roles by dataStoreManager.rolesFlow.collectAsState(initial = listOf())
    val selectedBalls by dataStoreManager.selectedBallsFlow.collectAsState(initial = emptySet())
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())
    val allPlayersRegistered by dataStoreManager.allPlayersRegisteredFlow.collectAsState(initial = false)

    // Récupérer les informations sur qui a sélectionné quel ballon
    var ballWithPlayers by remember { mutableStateOf<List<Triple<String?, String, Player?>>>(emptyList()) }

    // Initialiser la liste des ballons de football
    var footballBalls by remember { mutableStateOf<List<Triple<String?, String, Boolean>>>(emptyList()) }

    // Effet pour rafraîchir les données lorsque nécessaire
    LaunchedEffect(totalPlayers, secretWords, roles, selectedBalls, registeredPlayers) { // Supprimé refreshTrigger, shouldRefreshData
        /* Commenté car bloc lié au rechargement
        if (shouldRefreshData) {
            // Forcer un rafraîchissement des données
            dataStoreManager.forceRefreshRegisteredPlayers()
            shouldRefreshData = false
        }
        */

        if (roles.isNotEmpty() && secretWords.isNotEmpty()) {
            // Traitement des mots secrets et attribution aux rôles
            val shuffledWords = secretWords.toList().shuffled()

            val titulaireWord = shuffledWords.firstOrNull() ?: "Mot inconnu"
            val footixWords = shuffledWords.drop(1).take(roles.count { it == "Footix" })
            val remplacantWords = shuffledWords.drop(1 + footixWords.size).take(roles.count { it == "Remplaçant" })

            val ballList = mutableListOf<Triple<String?, String, Player?>>()
            val tempFootballBalls = mutableListOf<Triple<String?, String, Boolean>>()

            var footixIndex = 0
            var remplacantIndex = 0

            roles.forEach { role ->
                when (role) {
                    "Titulaire" -> {
                        // Trouver si un joueur a déjà ce mot
                        val player = registeredPlayers.find { it.word == titulaireWord }
                        ballList.add(Triple(titulaireWord, role, player))
                        tempFootballBalls.add(Triple(titulaireWord, role, player != null))
                    }
                    "Footix" -> {
                        val word = footixWords.getOrNull(footixIndex) ?: "Mot inconnu"
                        val player = registeredPlayers.find { it.word == word }
                        ballList.add(Triple(word, role, player))
                        tempFootballBalls.add(Triple(word, role, player != null))
                        footixIndex++
                    }
                    "Remplaçant" -> {
                        val word = remplacantWords.getOrNull(remplacantIndex) ?: "Mot inconnu"
                        val player = registeredPlayers.find { it.word == word }
                        ballList.add(Triple(word, role, player))
                        tempFootballBalls.add(Triple(word, role, player != null))
                        remplacantIndex++
                    }
                    else -> {
                        // Gérer d'autres rôles si nécessaire
                    }
                }
            }

            ballWithPlayers = ballList.shuffled()
            footballBalls = tempFootballBalls.shuffled()
        }
    }

    // Observer la navigation pour rafraîchir les données quand on revient à cet écran
    /* Commenté car bloc lié au rechargement
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.route?.startsWith("ChoosePlayerBallScreen") == true) {
                refreshTrigger += 1
                shouldRefreshData = true
            }
        }
    }
    */

    // Gérer la navigation vers l'écran de jeu une fois tous les joueurs inscrits
    LaunchedEffect(allPlayersRegistered) {
        if (allPlayersRegistered) {
            // Afficher un message pour confirmer que tous les joueurs sont inscrits
            Toast.makeText(
                context,
                "Tous les joueurs sont inscrits ! La partie va commencer.",
                Toast.LENGTH_LONG
            ).show()

            // Attendre un peu pour que les joueurs puissent voir le message
            delay(2000)

            // Tous les joueurs sont inscrits, on peut passer à l'écran suivant
            navController.navigate("gameScreen") {
                // Supprimer tous les écrans précédents de la pile
                popUpTo(0) { inclusive = true }
                // Make sure to have launchSingleTop to avoid duplicating the destination
                launchSingleTop = true
            }
        }
    }

    /* Commenté car dialogue de debug
    // Boîte de dialogue de débogage
    if (showDebugDialog) {
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("Joueurs enregistrés") },
            text = {
                Column {
                    Text("Nombre total: ${registeredPlayers.size}/${totalPlayers}")
                    Divider()
                    registeredPlayers.forEach { player ->
                        Text("${player.name} - ${player.role} - Mot: ${player.word}")
                        Divider()
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDebugDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
    */

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
                },
                actions = {
                    /* Commenté car actions liées au débogage
                    IconButton(onClick = { showDebugDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Debug"
                        )
                    }
                    // Bouton de reset pour le développement/test
                    IconButton(onClick = {
                        scope.launch {
                            dataStoreManager.resetGame()
                            // Rafraîchir l'écran après la réinitialisation
                            refreshTrigger += 1
                            shouldRefreshData = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Réinitialiser"
                        )
                    }
                    */
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
            // Afficher le statut d'inscription des joueurs
            Text(
                text = "Joueurs inscrits: ${registeredPlayers.size}/${totalPlayers}",
                style = MaterialTheme.typography.headlineSmall
            )

            // Afficher la liste des joueurs déjà inscrits
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

            Text(
                text = "Sélectionner un ballon pour le prochain joueur:",
                style = MaterialTheme.typography.titleMedium
            )

            // Grille des ballons disponibles
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(footballBalls) { (word, role, isSelected) ->
                    FootballBallButton(
                        role = role,
                        word = word,
                        isSelected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                scope.launch {
                                    if (word != null) {
                                        dataStoreManager.saveSelectedBall(word, role)
                                    } else {
                                        dataStoreManager.saveCurrentRole(role)
                                    }
                                    navController.navigate("PlayerSetupScreen")
                                }
                            }
                        }
                    )
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
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Joueur",
                modifier = Modifier.size(56.dp)
            )
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
    word: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val registeredPlayers by dataStoreManager.registeredPlayersFlow.collectAsState(initial = emptyList())

    // Trouver le joueur qui a sélectionné ce ballon (si applicable)
    val selectedBy = remember(registeredPlayers, word, role) {
        if (isSelected) {
            registeredPlayers.find {
                (it.word == word && word != null) ||
                        (word == null && it.role == role && it.word == null)
            }
        } else null
    }

    IconButton(
        onClick = onClick,
        enabled = !isSelected,
        modifier = modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isSelected && selectedBy != null) {
                // Afficher la photo du joueur qui a choisi ce ballon
                if (selectedBy.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedBy.photoUri),
                        contentDescription = "Photo de ${selectedBy.name}",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Joueur",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary

                    )
                }
                Text(
                    text = selectedBy.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            } else {

                Icon(
                    imageVector = Icons.Filled.SportsSoccer,
                    contentDescription = "Ballon de football",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                // Cacher le rôle pour ne pas révéler d'informations sur le jeu
                Text(
                    text = when (role) {
                        // On remplace tous les rôles par "Joueur"
                        "Titulaire" -> "Joueur"
                        "Footix" -> "Joueur"
                        "Remplaçant" -> "Joueur"
                        else -> "Joueur"

                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}