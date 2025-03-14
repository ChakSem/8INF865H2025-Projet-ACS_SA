package com.example.redcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.example.redcard.data.DataStoreManager
import com.example.redcard.ui.ChoosePlayerBallScreen
import com.example.redcard.ui.ConfigurationScreen
import com.example.redcard.ui.GameIntroductionScreen
import com.example.redcard.ui.GameScreen
import com.example.redcard.ui.GeneralSettingScreen
import com.example.redcard.ui.HomeScreen
import com.example.redcard.ui.PlayerSetupScreen
import com.example.redcard.ui.PlayerWordDiscoverScreen
import com.example.redcard.ui.StartingPage
import com.example.redcard.ui.TutorialSwipeableScreen
import com.example.redcard.ui.VictoryScreen
import com.example.redcard.ui.VoteScreen

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
        //GameIntroductionScreen
        composable("startingPage") { StartingPage(navController) }
        composable("gameConfiguration") { ConfigurationScreen(navController, context) }
        composable("generalSettings") { GeneralSettingScreen(navController, dataStoreManager) }
        composable("GameIntroductionScreen") { GameIntroductionScreen(navController) }
        composable("ChoosePlayerBallScreen") {
            ChoosePlayerBallScreen(navController, dataStoreManager)
        }
        composable("PlayerSetupScreen") {
            PlayerSetupScreen(navController, dataStoreManager)
        }
        composable("TutorialSwipeableScreen") { TutorialSwipeableScreen(navController) }
        composable("PlayerWordDiscoverScreen") {
            PlayerWordDiscoverScreen(navController, dataStoreManager)
        }
        composable("gameScreen") { GameScreen(navController) }
        composable("voteScreen") { VoteScreen(navController) }
        composable("victoryScreen") { VictoryScreen(navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), onSettingsClick = {})
}
