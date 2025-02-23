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
    val players by dataStoreManager.playersFlow.collectAsState(initial = 3)
    val secretWords by dataStoreManager.secretWordsFlow.collectAsState(initial = emptySet())
    var selectedWords by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(players, secretWords) {
        // Sélectionner aléatoirement le nombre nécessaire de mots secrets
        selectedWords = secretWords.shuffled().take(players)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Joueur 1 choisis un ballon") },
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
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(selectedWords) { word ->
                    FootballBallButton(
                        onClick = {
                            scope.launch {
                                dataStoreManager.saveSelectedBallWord(word)
                                navController.navigate("PlayerSetupScreen")
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_football),
            contentDescription = "Ballon de football",
            modifier = Modifier.fillMaxSize(),
            tint = Color.Unspecified
        )
    }
}