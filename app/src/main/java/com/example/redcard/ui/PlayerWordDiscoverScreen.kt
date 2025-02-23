package com.example.redcard.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager

@Composable
fun PlayerWordDiscoverScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    modifier: Modifier = Modifier
) {
    val playerName by dataStoreManager.playerNameFlow.collectAsState(initial = null)
    val currentPhotoUri by dataStoreManager.playerPhotoUriFlow.collectAsState(initial = null)
    val selectedWord by dataStoreManager.selectedBallWordFlow.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Découverte du mot") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display Photo
            currentPhotoUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(
                        model = uri,
                        error = painterResource(id = R.drawable.ic_camera)
                    ),
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            playerName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            selectedWord?.let { word ->
                Text(
                    text = "Votre mot secret : $word",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Bouton Continuer
            Button(
                onClick = {
                    navController.navigate("gameScreen") {
                        popUpTo("gameScreen") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continuer",
                    fontSize = 18.sp
                )
            }
        }
    }
}