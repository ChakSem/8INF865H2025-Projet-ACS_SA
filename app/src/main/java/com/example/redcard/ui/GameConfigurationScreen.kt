package com.example.redcard.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import kotlinx.coroutines.launch

@Composable
fun ConfigurationScreen(navController: NavController, context: Context) {
    val dataStore = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val players by dataStore.playersFlow.collectAsState(initial = 3)
    val titulaires by dataStore.titulairesFlow.collectAsState(initial = 2)
    val footix by dataStore.footixFlow.collectAsState(initial = 0)
    val remplacants by dataStore.remplacantsFlow.collectAsState(initial = 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            Text(
                text = "Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Parametrages",
                modifier = Modifier
                    .size(24.dp)
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
            onValueChange = { newValue ->
                if (newValue >= 0) {
                    coroutineScope.launch { dataStore.savePlayers(newValue) }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section des rôles
        Text(
            text = "Les Rôles",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        // Sélecteurs pour les rôles
        NumberSelector(
            title = "Titulaire",
            value = titulaires,
            onValueChange = { newValue ->
                if (newValue >= 0) {
                    coroutineScope.launch { dataStore.saveTitulaires(newValue) }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumberSelector(
            title = "Footix",
            value = footix,
            onValueChange = { newValue ->
                if (newValue >= 0) {
                    coroutineScope.launch { dataStore.saveFootix(newValue) }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumberSelector(
            title = "Remplaçant",
            value = remplacants,
            onValueChange = { newValue ->
                if (newValue >= 0) {
                    coroutineScope.launch { dataStore.saveRemplacants(newValue) }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Commencer
        Button(
            onClick = {
                navController.navigate("GameIntroductionScreen") {
                    popUpTo("GameIntroductionScreen") { inclusive = true }
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

@Composable
fun NumberSelector(
    title: String,
    value: Int,
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
                enabled = value > 0
            ) {
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
        context = context
    )
}