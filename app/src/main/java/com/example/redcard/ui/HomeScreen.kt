package com.example.redcard.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(),
) {
    var isTextVisible by remember { mutableStateOf(true) }

    // Animation pour alterner la visibilité du texte
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Délai avant l'animation inverse
            isTextVisible = !isTextVisible
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clickable {
                navController.navigate("startingPage")
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icône maison et icône settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flag_fr),
                    contentDescription = "Accueil",
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate("generalSettings") {
                                popUpTo("generalSettings") { inclusive = true }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo Red Card",
                modifier = Modifier
                    .width(400.dp)
                    .height(400.dp)
            )

            // Spacer pour décaler le texte un peu vers le bas
            Spacer(modifier = Modifier.height(100.dp))

            // Texte avec animation de fondu progressif
            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(animationSpec = tween(700)), // Apparition douce
                exit = fadeOut(animationSpec = tween(700))  // Disparition douce
            ) {
                Text(
                    text = "Appuyer pour continuer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
