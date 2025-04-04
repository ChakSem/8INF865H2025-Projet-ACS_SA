package com.example.redcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.example.redcard.data.DataStoreManager
import com.example.redcard.model.FirestoreManager
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
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


class MainActivity : ComponentActivity() {
    private val themeViewModel by viewModels<ThemeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            // Observer le thème depuis le ViewModel
            val currentTheme by themeViewModel.theme.collectAsState()

            // Déterminer si le thème doit être sombre ou clair
            val darkTheme = when (currentTheme) {
                AppTheme.SOMBRE -> true
                AppTheme.CLAIR -> false
                AppTheme.SYSTEME -> isSystemInDarkTheme() // Utiliser le thème système par défaut
            }

            // Appliquer le thème à l'ensemble de l'application
            RedCardTheme(darkTheme = darkTheme) {
                // Passer seulement le navController aux composables, pas le ViewModel
                AppNavigation(themeViewModel = themeViewModel)
            }
        }
    }
}


@Composable
fun AppNavigation(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    // Passer les autres paramètres nécessaires à chaque écran
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val firestoreManager = FirestoreManager(context)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("startingPage") { StartingPage(navController, themeViewModel) }
        composable("gameConfiguration") { ConfigurationScreen(navController, context) }
        composable("generalSettings") {
            GeneralSettingScreen(navController, dataStoreManager, themeViewModel)
        }
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
