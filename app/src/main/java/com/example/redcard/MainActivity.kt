package com.example.redcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.redcard.data.DataStoreManager
import com.example.redcard.data.dataStore
import com.example.redcard.model.FirestoreManager
import com.example.redcard.model.TurnManager
import com.example.redcard.ui.*
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val themeViewModel by viewModels<ThemeViewModel>()
    private val dataStoreManager by lazy { DataStoreManager(this) } // Initialisation de DataStoreManager

    // Initialisation du TurnManager avec le DataStore
    private val turnManager by lazy { TurnManager(dataStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialisation de Firebase
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
                // Passer le TurnManager initalisé à la navigation
                AppNavigation(themeViewModel = themeViewModel, turnManager = turnManager)
            }
        }
    }
}

@Composable
fun AppNavigation(themeViewModel: ThemeViewModel, turnManager: TurnManager) {
    val navController = rememberNavController()
    val context = LocalContext.current // Récupération du contexte
    val dataStoreManager = remember { DataStoreManager(context) } // Création de l'instance DataStoreManager
    val firestoreManager = remember { FirestoreManager(context) } // Création de l'instance FirestoreManager

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, dataStoreManager) }
        composable("startingPage") { StartingPage(navController, themeViewModel) }
        composable("gameConfiguration") {
            ConfigurationScreen(
                navController,
                context,
                themeViewModel
            )
        }
        composable("generalSettings") {
            GeneralSettingScreen(
                navController,
                dataStoreManager,
                themeViewModel
            )
        }
        composable("GameIntroductionScreen") {
            GameIntroductionScreen(
                navController,
                themeViewModel
            )
        }
        composable("ChoosePlayerBallScreen") {
            ChoosePlayerBallScreen(navController, dataStoreManager)
        }
        composable("PlayerSetupScreen") {
            PlayerSetupScreen(navController, dataStoreManager)
        }
        composable("TutorialSwipeableScreen") {
            TutorialSwipeableScreen(
                navController,
                themeViewModel
            )
        }
        composable("gameScreen") {
            GameScreen(
                navController,
                dataStoreManager,
                themeViewModel,
                turnManager
            )
        }
        composable("voteScreen") {
            VoteScreen(
                navController,
                dataStoreManager,
                themeViewModel,
                turnManager
            )
        }
        composable("victoryScreen") {
            VictoryScreen(
                navController,
                dataStoreManager,
                themeViewModel
            )
        }
        composable(
            route = "ChoosePlayerBallScreen?refresh={refresh}",
            arguments = listOf(
                navArgument("refresh") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val refresh = backStackEntry.arguments?.getBoolean("refresh") ?: false
            ChoosePlayerBallScreen(navController, dataStoreManager)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    HomeScreen(navController = rememberNavController(), dataStoreManager = dataStoreManager)
}