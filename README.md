# 8INF865H2025-Projet-ACS_SA

# Documentation : RedCard

RedCard est une application mobile Android développée avec Jetpack Compose. Il s'agit d’un jeu de société d’ambiance et de bluff sur le thème du football, inspiré des mécaniques de jeux comme Undercover, mais revisité selon notre passion.

## *Fonctionnalités principales*

- Attribution aléatoire des rôles : Titulaire, Remplaçant, Footix
- Navigation fluide entre les écrans grâce à Jetpack Navigation
- Système de mots secrets et d’improvisation pour créer une tension de bluff
- Vote final entre les joueurs pour identifier les imposteurs
- Utilisation de DataStore pour la gestion locale des mots et préférences

## *Structure du projet*

L’application est organisée selon l’architecture standard Android, avec une séparation logique entre les modèles, l’interface utilisateur et la logique métier.

### *res/ — Ressources*

Contient tous les éléments visuels, animations, sons et définitions de style :

- anim/ : animations
- drawable/ : images et illustrations utilisées dans l'UI
- font/ : polices d'écriture, comme celle utilisée pour le thème RedCard
- mipmap-*/ : icônes de l'application pour différentes densités d'écran
- raw/ : fichiers audio 
- values/ : couleurs, styles, dimensions, strings
- xml/ : fichiers de configuration 

### *java/com/example/redcard/ — Code principal*

- MainActivity.kt : Point d’entrée de l’application. Contient l'initialisation du NavController pour la navigation entre les différents écrans du jeu.

### *model/ — Logique et gestion*

- DataStoreManager.kt : Gestion de la persistance locale des données 
- MusicPlayerManager.kt : Gestionnaire pour jouer / arrêter les musiques de fond
- TurnManager.kt : Gère le tour des joueurs, l’ordre de jeu, et le passage d’un joueur à l’autre

### *ui/ — Interface utilisateur (Jetpack Compose)*

- theme/ : Styles, couleurs, et configuration globale du thème de l’application.

*Écrans principaux :*

- HomeScreen.kt : Page d’accueil
- StartingScreen.kt : Démarrage de la partie
- ChoosePlayerBallScreen.kt : Sélection visuelle des joueurs
- GameConfigurationScreen.kt : Configuration de la partie (nombre de joueurs, rôles, etc.)
- PlayerSetupScreen.kt : Création du joueur
- GameIntroductionScreen.kt : Introduction aux règles et rôles
- TutorialSwipeableScreen.kt : Tutoriel animé sous forme de slides
- GameScreen.kt : Écran principal de jeu
- VoteScreen.kt : Écran de vote pour éliminer un joueur
- VictoryScreen.kt : Écran de fin avec animation de victoire
- GeneralSettingScreen.kt : Paramètres généraux
