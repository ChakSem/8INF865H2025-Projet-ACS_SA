package com.example.redcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R

@Composable
fun VoteScreen(
    navController: NavController,
) {
    var selectedProfile by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Row en haut avec la maison et les réglages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Home, // Icône Home de Google Fonts
                    contentDescription = "Accueil",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    navController.navigate("generalSettings")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Cadran avec le texte
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Qui voulez-vous éliminer ?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Liste des profils
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val profiles = listOf("Joueur 1", "Joueur 2", "Joueur 3")

            profiles.forEach { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedProfile = if (selectedProfile == profile) {
                                null // Désélectionner si c'est déjà sélectionné
                            } else {
                                profile // Sélectionner un profil
                            }
                        }
                        .padding(16.dp)
                        .background(
                            if (selectedProfile == profile) Color.Green.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profil avec cercle plus grand
                    Box(
                        modifier = Modifier
                            .size(60.dp) // Augmentation de la taille des cercles
                            .background(Color.Gray, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = profile,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    // Icône de vérification si le profil est sélectionné
                    if (selectedProfile == profile) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Profil sélectionné",
                            tint = Color.Green
                        )
                    }
                }
            }
        }

        // Row avec les boutons Confirmer et Annuler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bouton Confirmer
            Button(
                onClick = {
                    navController.navigate("victoryScreen")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Confirmer")
            }

            // Bouton Annuler
            Button(
                onClick = {
                    navController.navigate("gameScreen")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Annuler")
            }
        }
    }
}

