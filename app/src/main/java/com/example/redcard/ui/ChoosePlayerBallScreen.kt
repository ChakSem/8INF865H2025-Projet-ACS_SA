package com.example.redcard.ui

// ChoosePlayerBallScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager

@Composable
fun ChoosePlayerBallScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Récupérer les données stockées
    val players by dataStoreManager.playersFlow.collectAsState(initial = 3)
    val secretWords by dataStoreManager.secretWordsFlow.collectAsState(initial = emptySet())
    val roles by dataStoreManager.rolesFlow.collectAsState(initial = listOf()) // Liste des rôles récupérée depuis DataStore
    val selectedBalls by dataStoreManager.selectedBallsFlow.collectAsState(initial = emptySet())

    var footballBalls by remember { mutableStateOf<List<Pair<String?, String>>>(emptyList()) }

    LaunchedEffect(players, secretWords, roles) {
        if (roles.isNotEmpty() && secretWords.isNotEmpty()) {
            val shuffledWords = secretWords.shuffled() // Mélanger les mots pour plus de hasard

            val titulaireWord = shuffledWords.firstOrNull() ?: "Mot inconnu"
            val footixWords = shuffledWords.drop(1).take(roles.count { it == "Footix" }) // Prendre des mots différents pour les footix

            val ballList = mutableListOf<Pair<String?, String>>() // (Mot secret ou null, Rôle)

            var footixIndex = 0
            roles.forEach { role ->
                when (role) {
                    "Titulaire" -> ballList.add(titulaireWord to role)
                    "Footix" -> {
                        val word = footixWords.getOrNull(footixIndex) ?: "Mot inconnu"
                        ballList.add(word to role)
                        footixIndex++
                    }
                    "Remplaçant" -> ballList.add(null to role) // Pas de mot pour les remplaçants
                }
            }

            footballBalls = ballList.shuffled() // Mélanger la liste des ballons
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
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(footballBalls) { (word, role) ->
                    val isDisabled = word != null && selectedBalls.contains(word)

                    FootballBallButton(
                        role = role,
                        word = word,
                        onClick = {
                            if (!isDisabled) {
                                scope.launch {
                                    if (word != null) {
                                        dataStoreManager.saveSelectedBall(word)
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
fun FootballBallButton(
    role: String,
    word: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_football),
                contentDescription = "Ballon de football",
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = word ?: "Vous êtes remplaçant !",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


