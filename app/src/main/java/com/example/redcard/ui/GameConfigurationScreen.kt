package com.example.redcard.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.redcard.data.DataStoreManager
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
@Composable
fun ConfigurationScreen(navController: NavController, context: Context, themeViewModel: ThemeViewModel) {
    val dataStore = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Configuration initiale des joueurs
    var players by remember { mutableStateOf(3) }
    var footix by remember { mutableStateOf(0) }
    var remplacants by remember { mutableStateOf(1) }

    // Calcul dynamique des titulaires
    val titulaires by remember { derivedStateOf {
        players - footix - remplacants
    }}

    // Fonction pour ajuster les rôles avec contraintes
    fun updateRoles(newFootix: Int, newRemplacants: Int) {
        val minTitulaires = (players + 1) / 2  // Plus de titulaires que footix/remplaçants

        // Vérifications des contraintes
        if ((newFootix == 0 && newRemplacants == 0) || newFootix + newRemplacants > players - minTitulaires) {
            return
        }

        if (newFootix >= 0 && newRemplacants >= 0 && newFootix + newRemplacants <= players - minTitulaires) {
            footix = newFootix
            remplacants = newRemplacants
        }
    }

    // Gestion du thème
    val currentTheme by themeViewModel.theme.collectAsState()
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = if (darkTheme) Color.White else Color.Black

    RedCardTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // En-tête avec navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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
                Text(
                    text = "Configuration",
                    fontSize = 24.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Paramètres",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate("generalSettings") {
                                popUpTo("generalSettings") { inclusive = true }
                            }
                        }
                )
            }

            // Sélecteur de nombre de joueurs
            NumberSelector(
                title = "Joueurs",
                value = players,
                footix = footix,
                remplacants = remplacants,
                onValueChange = { newValue ->
                    if (newValue in 3..20) {
                        if (newValue > players) {
                            // Ajout d'un joueur = ajouter un titulaire
                            players = newValue
                        } else if (newValue < players) {
                            // Retrait de joueurs avec logique de priorité
                            val diff = players - newValue
                            for (i in 1..diff) {
                                if (titulaires > footix + remplacants) {
                                    players--
                                } else {
                                    if (remplacants == footix && remplacants > 0) {
                                        remplacants--
                                    } else if (remplacants > footix) {
                                        remplacants--
                                    } else if (footix > 0) {
                                        footix--
                                    }
                                    players--
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section des rôles
            Text(
                text = "Les Rôles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            // Affichage des titulaires (non modifiable directement)
            RoleDisplay(title = "Titulaire", value = titulaires)

            Spacer(modifier = Modifier.height(8.dp))

            // Sélecteur pour les remplaçants
            NumberSelector(
                title = "Remplaçant",
                value = remplacants,
                footix = footix,
                remplacants = remplacants,
                onValueChange = { newValue ->
                    if (newValue >= 0 && newValue + footix <= players - (players + 1) / 2) {
                        updateRoles(footix, newValue)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sélecteur pour les footix
            NumberSelector(
                title = "Footix",
                value = footix,
                footix = footix,
                remplacants = remplacants,
                onValueChange = { newValue ->
                    if (newValue >= 0 && newValue + remplacants <= players - (players + 1) / 2) {
                        updateRoles(newValue, remplacants)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bouton pour commencer le jeu
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Sauvegarde de la configuration
                        dataStore.savePlayers(players)
                        dataStore.saveTitulaires(titulaires)
                        dataStore.saveFootix(footix)
                        dataStore.saveRemplacants(remplacants)

                        navController.navigate("GameIntroductionScreen") {
                            popUpTo("GameIntroductionScreen") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Commencer",
                    fontSize = 18.sp
                )
            }
        }
    }
}

// Composant pour affichage en lecture seule
@Composable
fun RoleDisplay(
    title: String,
    value: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp
        )
        Text(
            text = value.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Composant pour sélection numérique
@Composable
fun NumberSelector(
    title: String,
    value: Int,
    footix: Int,
    remplacants: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bouton pour diminuer
            IconButton(
                onClick = { onValueChange(value - 1) },
                enabled = value > 0 && (
                        (title == "Footix" && (footix > 1 || remplacants > 0)) || 
                        (title == "Remplaçant" && (remplacants > 1 || footix > 0)) || 
                        (title != "Footix" && title != "Remplaçant")
                )
            ) {
                Text(text = "-", fontSize = 20.sp)
            }

            Text(
                text = value.toString(),
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Bouton pour augmenter
            IconButton(
                onClick = { onValueChange(value + 1) }
            ) {
                Text(text = "+", fontSize = 20.sp)
            }
        }
    }
}