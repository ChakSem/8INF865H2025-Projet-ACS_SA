package com.example.redcard.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun GameScreen(
    navController: NavController,
) {
    var isEyeOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize() // Utiliser toute la taille de l'écran
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp) // Espacement accru entre les éléments
    ) {
        // Icône maison et titre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Accueil",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
            )
        }

        // Titre
        Text(
            text = "Décrivez votre mot",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Row avec des cercles et des noms
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Répartir les éléments sur toute la largeur
        ) {
            // Cercle 1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp) // Augmenter la taille du cercle
                        .background(Color.Gray, shape = CircleShape)
                )
                Text(text = "Joueur 1", fontSize = 16.sp) // Augmenter la taille du texte
            }

            // Cercle 2
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp) // Augmenter la taille du cercle
                        .background(Color.Gray, shape = CircleShape)
                )
                Text(text = "Joueur 2", fontSize = 16.sp) // Augmenter la taille du texte
            }

            // Cercle 3
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp) // Augmenter la taille du cercle
                        .background(Color.Gray, shape = CircleShape)
                )
                Text(text = "Joueur 3", fontSize = 16.sp) // Augmenter la taille du texte
            }
        }

        // Espacer les éléments supplémentaires dans la colonne si nécessaire
        Spacer(modifier = Modifier.weight(1f)) // Remplir l'espace restant

        // Bouton "Passer au vote"
        Button(
            onClick = {
                // Logique pour passer au vote (par exemple, navigation)
                navController.navigate("voteScreen")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Passer au vote")
        }

        // Boutons en bas à droite
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Bouton œil fermé qui s'ouvre au clic
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isEyeOpen = !isEyeOpen }) {
                    Icon(
                        imageVector = if (isEyeOpen) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Afficher/Masquer",
                        tint = Color.Black
                    )
                }

                // Bouton Settings
                IconButton(
                    onClick = {
                        navController.navigate("generalSettings") {
                            popUpTo("generalSettings") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Réglages",
                        tint = Color.Black
                    )
                }
            }
        }
    }


}
