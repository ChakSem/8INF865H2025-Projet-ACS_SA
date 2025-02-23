package com.example.redcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.redcard.data.DataStoreManager
import com.example.redcard.ui.ConfigurationScreen
import com.example.redcard.ui.GeneralSettingScreen
import com.example.redcard.ui.HomeScreen
import com.example.redcard.ui.StartingPage
import com.example.redcard.ui.tutorial.TutorialSwipeableScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current // Récupération du contexte
    val dataStoreManager = DataStoreManager(context) // Création de l'instance DataStoreManager

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("startingPage") { StartingPage(navController) }
        composable("gameConfiguration") { ConfigurationScreen(navController, context) } // Correction ici
        composable("generalSettings") { GeneralSettingScreen(navController, dataStoreManager) } // Correction ici
        composable("tutorialSwipeableScreen") { TutorialSwipeableScreen(navController) }

    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), onSettingsClick = {})
}
