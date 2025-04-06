package com.example.redcard.ui

import android.media.MediaPlayer
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import kotlinx.coroutines.delay

import coil.compose.rememberAsyncImagePainter
import com.example.redcard.model.MusicPlayerManager

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(),
) {
    var isTextVisible by remember { mutableStateOf(true) }

    // Musique de fond
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        MusicPlayerManager.playMusicIntro(context)
    }

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
        // Image de fond
        Image(
            painter = painterResource(id = R.drawable.background_image), // Remplace 'background_image' par ton image de fond
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Cette option permet d'ajuster l'image pour qu'elle couvre tout l'écran
        )

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
                // Utilisation de Coil pour afficher le SVG
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.drapeau_francais), // SVG du drapeau français
                    contentDescription = "Drapeau Français",
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
                painter = painterResource(id = R.drawable.redcard_logo),
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
                    text = "APPUYEZ",
                    fontSize = 30.sp, // Texte plus gros
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // Texte en blanc
                    fontFamily = FontFamily(Font(R.font.rubik_medium_italic)), // Police Rubik
                    modifier = Modifier.padding(top = 20.dp) // Spacer au-dessus du texte
                )
            }
        }
    }
}


