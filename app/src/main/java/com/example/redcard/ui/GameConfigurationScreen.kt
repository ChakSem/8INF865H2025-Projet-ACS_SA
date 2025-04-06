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

    // État initial avec 3 joueurs : 2 titulaires, 1 footix, 0 remplacant
    var players by remember { mutableStateOf(3) }
    var footix by remember { mutableStateOf(1) }
    var remplacants by remember { mutableStateOf(0) }

    // Calcul dynamique des titulaires
    val titulaires by remember { derivedStateOf {
        players - footix - remplacants
    }}

    // Fonction pour ajuster les rôles avec la contrainte : plus de titu que de footix ou de remplaçant
    fun updateRoles(newFootix: Int, newRemplacants: Int) {
        val total = newFootix + newRemplacants
        val minTitulaires = (players + 1) / 2  // Plus de titulaires que footix/remplaçants

        // Vérifie si on essaie de mettre 0 footix et 0 remplacants → impossible
        if ((newFootix == 0 && newRemplacants == 0) || newFootix + newRemplacants > players - minTitulaires) {
            return
        }

        // Vérifier que le total des footix + remplacants ne dépasse pas les titulaires
        if (newFootix >= 0 && newRemplacants >= 0 && newFootix + newRemplacants <= players - minTitulaires) {
            footix = newFootix
            remplacants = newRemplacants
        }
    }

    // Obtenir le thème actuel via ThemeViewModel
    val currentTheme by themeViewModel.theme.collectAsState()

    // Déterminez si le thème est sombre ou clair
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = if (darkTheme) Color.White else Color.Black // Icônes blanches si thème clair, sinon couleur par défaut


    // Appliquez le thème dynamique
    RedCardTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icône et titre
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

            // Compteur de joueurs
            NumberSelector(
                title = "Joueurs",
                value = players,
                footix = footix,
                remplacants = remplacants,
                onValueChange = { newValue ->
                    if (newValue in 3..20) {
                        if (newValue > players) {
                            // Ajout d'un joueur => ajoute un titulaire
                            players = newValue
                        } else if (newValue < players) {
                            // Retrait d'un joueur
                            val diff = players - newValue
                            for (i in 1..diff) {
                                if (titulaires > footix + remplacants) {
                                    // Si plus de titulaires que footix + remplacants, enlève un titulaire
                                    players--
                                } else {
                                    // Sinon, regarde les remplaçants et les footix
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

            // Titulaires - Pas de modification possible
            RoleDisplay(title = "Titulaire", value = titulaires)

            Spacer(modifier = Modifier.height(8.dp))

            // Footix - Modification possible
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

            Spacer(modifier = Modifier.height(8.dp))

            // Remplaçant - Modification possible
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

            Spacer(modifier = Modifier.weight(1f))

            // Bouton Commencer
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Sauvegarde les valeurs dans le DataStore avant de naviguer
                        dataStore.savePlayers(players)
                        dataStore.saveTitulaires(titulaires)
                        dataStore.saveFootix(footix)
                        dataStore.saveRemplacants(remplacants)

                        // Navigation vers la page suivante
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


// Composant pour afficher les rôles non modifiables
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

// Composant pour les sélecteurs de rôles modifiables
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
            IconButton(
                onClick = { onValueChange(value - 1) },
                enabled = value > 0 && (
                        (title == "Footix" && (footix > 1 || remplacants > 0)) ||  // Footix peut être réduit si > 1 ou si remplaçant existe
                                (title == "Remplaçant" && (remplacants > 1 || footix > 0)) ||  // Remplaçant peut être réduit si > 1 ou si footix existe
                                (title != "Footix" && title != "Remplaçant")  // Si ce n'est pas un Footix/Remplaçant, autorise toujours
                        )
            )
            {
                Text(text = "-", fontSize = 20.sp)
            }


            Text(
                text = value.toString(),
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { onValueChange(value + 1) }
            ) {
                Text(text = "+", fontSize = 20.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    val context = LocalContext.current
    ConfigurationScreen(
        navController = rememberNavController(),
        context = context,
        themeViewModel = ThemeViewModel()
    )
}
