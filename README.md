# RedCard

RedCard est une application mobile Android développée avec Jetpack Compose. Il s'agit d'un jeu de société d'ambiance et de bluff sur le thème du football, inspiré des mécaniques de jeux comme Undercover, mais revisité selon notre passion.

![RedCard - Vue d'ensemble](RedCard%20-%20Vue%20d'ensemble.png)

## Table des matières
- [Fonctionnalités principales](#fonctionnalités-principales)
- [Architecture de l'application](#architecture-de-lapplication)
  - [Structure du projet](#structure-du-projet)
  - [Composants clés](#composants-clés)
- [Logique de jeu](#logique-de-jeu)
- [Aspects techniques](#aspects-techniques)
- [Configuration et déploiement](#configuration-et-déploiement)
- [Améliorations futures](#améliorations-futures)

## Fonctionnalités principales

- Attribution aléatoire des rôles : Titulaire, Remplaçant, Footix
- Navigation fluide entre les écrans grâce à Jetpack Navigation
- Système de mots secrets et d'improvisation pour créer une tension de bluff
- Vote final entre les joueurs pour identifier les imposteurs
- Utilisation de DataStore pour la gestion locale des mots et préférences

## Architecture de l'application

### Structure du projet

L'application est organisée selon l'architecture standard Android, avec une séparation logique entre les modèles, l'interface utilisateur et la logique métier.

#### res/ — Ressources

Contient tous les éléments visuels, animations, sons et définitions de style :

- **anim/** : animations
- **drawable/** : images et illustrations utilisées dans l'UI
- **font/** : polices d'écriture, comme celle utilisée pour le thème RedCard
- **mipmap-\*/** : icônes de l'application pour différentes densités d'écran
- **raw/** : fichiers audio 
- **values/** : couleurs, styles, dimensions, strings
- **xml/** : fichiers de configuration 

#### java/com/example/redcard/ — Code principal

- **MainActivity.kt** : Point d'entrée de l'application. Contient l'initialisation du NavController.

#### model/ — Logique et gestion

- **DataStoreManager.kt** : Gestion de la persistance locale des données 
- **MusicPlayerManager.kt** : Gestionnaire pour jouer/arrêter les musiques de fond
- **TurnManager.kt** : Gère le tour des joueurs, l'ordre de jeu, et le passage d'un joueur à l'autre

#### ui/ — Interface utilisateur (Jetpack Compose)

- **theme/** : Styles, couleurs, et configuration globale du thème de l'application.

**Écrans principaux :**
- **HomeScreen.kt** : Page d'accueil
- **StartingScreen.kt** : Démarrage de la partie
- **ChoosePlayerBallScreen.kt** : Sélection visuelle des joueurs
- **GameConfigurationScreen.kt** : Configuration de la partie (nombre de joueurs, rôles, etc.)
- **PlayerSetupScreen.kt** : Création du joueur
- **GameIntroductionScreen.kt** : Introduction aux règles et rôles
- **TutorialSwipeableScreen.kt** : Tutoriel animé sous forme de slides
- **GameScreen.kt** : Écran principal de jeu
- **VoteScreen.kt** : Écran de vote pour éliminer un joueur
- **VictoryScreen.kt** : Écran de fin avec animation de victoire
- **GeneralSettingScreen.kt** : Paramètres généraux

### Composants clés

#### Navigation

La navigation est gérée dans `MainActivity.kt` via `NavHost`. Chaque écran est défini comme une destination dans le graphe de navigation.

```kotlin
NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("startingPage") { StartingPage(navController) }
    composable("gameConfiguration") { ConfigurationScreen(navController, context) }
    // ... autres écrans
}
```

**Pour ajouter un nouvel écran:**
1. Créer le composable Jetpack Compose
2. L'ajouter au NavHost dans MainActivity
3. Mettre à jour les routes qui y mènent (souvent via des boutons ou actions)

#### Gestion des données (DataStoreManager)

Le `DataStoreManager` est responsable de la persistance des données avec Jetpack DataStore. Il stocke:
- Configuration du jeu (nombre de joueurs, distribution des rôles)
- Informations des joueurs (noms, photos)
- Mots secrets pour le jeu
- Préférences utilisateur

```kotlin
// Exemple d'utilisation du DataStoreManager
class DataStoreManager(private val context: Context) {
    // Clés pour les préférences
    companion object {
        val PLAYERS_KEY = intPreferencesKey("players")
        // ... autres clés
    }
    
    // Flow pour accéder aux données
    val playersFlow: Flow<Int> = context.dataStore.data.map { it[PLAYERS_KEY] ?: 3 }
    
    // Méthodes pour enregistrer les données
    suspend fun savePlayers(value: Int) {
        context.dataStore.edit { it[PLAYERS_KEY] = value }
    }
}
```

> **Important:** Toutes les opérations d'écriture doivent être exécutées dans un scope coroutine!

#### Gestion des photos

La capture et le stockage des photos sont gérés dans `PlayerSetupScreen.kt`. Le processus comprend:
1. Demande des permissions de caméra
2. Création d'un fichier temporaire pour la photo
3. Lancement de l'intent caméra via ActivityResultLauncher

```kotlin
fun takeNewPhoto() {
    if (hasCameraPermission) {
        val photoFile = dataStoreManager.getPhotoFile()
        currentPhotoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
    } else {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
```

> **Note:** En cas de problème avec la caméra, vérifier:
> - Le manifest pour la configuration de FileProvider
> - Le fichier `res/xml/file_paths.xml`
> - Les permissions de stockage dans le manifest

## Logique de jeu

### Système de rôles

Le jeu repose sur 3 types de rôles:
- **Titulaires:** Majoritaires, doivent se reconnaître et éliminer les autres
- **Remplaçants:** Doivent se faire passer pour des Titulaires
- **Footix:** Doivent démasquer les Remplaçants

La répartition des rôles est gérée dans `GameConfigurationScreen.kt`:

```kotlin
// Calcul dynamique des titulaires
val titulaires by remember { derivedStateOf {
    players - footix - remplacants
}}

// Fonction pour ajuster les rôles
fun updateRoles(newFootix: Int, newRemplacants: Int) {
    val minTitulaires = (players + 1) / 2  // Plus de titulaires que la somme des autres
    // ...
}
```

> **Règle importante:** Il doit toujours y avoir plus de Titulaires que la somme des autres rôles!

### Mots secrets

Les mots secrets sont gérés dans `DataStoreManager.kt` avec un ensemble par défaut:

```kotlin
private val defaultSecretWords = setOf(
    "Zidane", "Platini", "Benzema", "Mbappé", "Henry",
    "Cantona", "Papin", "Griezmann", "Thuram", "Deschamps"
)
```

## Aspects techniques

### Thème et design

L'application utilise Material 3 avec un thème personnalisé défini dans `Theme.kt`. Les couleurs principales sont:

```kotlin
val md_theme_light_primary = Color(0xFF984062)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
// ... autres couleurs
```

## Configuration et déploiement

### Exécuter le projet
1. Cloner le dépôt
2. Ouvrir dans Android Studio
3. Synchroniser avec Gradle
4. Exécuter sur un émulateur ou appareil physique Android

## Améliorations futures

- **Performance:** Optimiser la gestion des photos (compression)
- **UI:** Ajouter des animations plus fluides entre les écrans
- **Firebase:** Intégrer Firebase pour la gestion des mots secrets avec filtrage par catégories (clubs, joueurs, coachs, stades, pays)
- **Modes de jeu:** Ajouter des variantes avec des règles différentes
- **Sécurité:** Améliorer le stockage des données sensibles
