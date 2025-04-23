package com.example.redcard.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.redcard.model.TurnManager
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlinx.serialization.SerialName
// Création de DataStore
val Context.dataStore by preferencesDataStore(name = "configurations")

// Classe représentant un joueur
@Serializable
data class Player(
    @SerialName("id") val id: String = UUID.randomUUID().toString(),
    @SerialName("name") val name: String,
    @SerialName("photoUri") val photoUri: String?,
    @SerialName("role") val role: String,
    @SerialName("word") val word: String?
)

class DataStoreManager(private val context: Context) {
    // Add this configuration
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        // Clés existantes
        val PLAYERS_KEY = intPreferencesKey("players")
        val TITULAIRES_KEY = intPreferencesKey("titulaires")
        val FOOTIX_KEY = intPreferencesKey("footix")
        val REMPLACANTS_KEY = intPreferencesKey("remplacants")
        val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        val CURRENT_LANGUAGE_KEY = stringPreferencesKey("current_language")
        val SECRET_WORDS_KEY = stringSetPreferencesKey("secret_words")
        val SELECTED_BALL_WORD_KEY = stringPreferencesKey("selected_ball_word")
        val PLAYER_PHOTO_URI_KEY = stringPreferencesKey("player_photo_uri")
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val SELECTED_BALLS_KEY = stringSetPreferencesKey("selected_balls")
        val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")

        // Clés pour la gestion des comptes et des mots
        val REGISTERED_PLAYERS_KEY = stringPreferencesKey("registered_players")
        val CURRENT_BALL_KEY = stringPreferencesKey("current_ball")
        val CURRENT_ROLE_KEY = stringPreferencesKey("current_role")
        val ALL_PLAYERS_REGISTERED_KEY = booleanPreferencesKey("all_players_registered")

        // Nouvelles clés pour la gestion des mots par rôle
        val TITULAIRE_WORD_KEY = stringPreferencesKey("titulaire_word")
        val REMPLACANT_WORD_KEY = stringPreferencesKey("remplacant_word")

        // Clé pour savoir si le jeu est terminé
        val GAME_COMPLETED_KEY = booleanPreferencesKey("game_completed")


    }


    // Mots secrets par défaut
    private val defaultSecretWords = setOf(
        "Zidane", "Platini", "Benzema", "Mbappé", "Henry",
        "Cantona", "Papin", "Griezmann", "Thuram", "Deschamps"
    )

    // Flux pour les mots secrets
    val secretWordsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[SECRET_WORDS_KEY] ?: defaultSecretWords }

    // Flux pour savoir si le jeu est terminé
    val gameCompletedFlow: Flow<Boolean> = context.dataStore.data
    .map { it[GAME_COMPLETED_KEY] ?: false }
    // Ajoutez cette propriété
    val musicEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: true // true par défaut
        }


    // Flux pour le mot de ballon sélectionné
    val selectedBallWordFlow: Flow<String?> = context.dataStore.data
        .map { it[SELECTED_BALL_WORD_KEY] }

    // Flux pour le joueur en cours de création
    val currentBallFlow: Flow<String?> = context.dataStore.data
        .map { it[CURRENT_BALL_KEY] }

    val currentRoleFlow: Flow<String?> = context.dataStore.data
        .map { it[CURRENT_ROLE_KEY] }

    // Flux pour savoir si tous les joueurs sont enregistrés
    val allPlayersRegisteredFlow: Flow<Boolean> = context.dataStore.data
        .map { it[ALL_PLAYERS_REGISTERED_KEY] ?: false }

    // Flux pour les mots par rôle
    val titulaireWordFlow: Flow<String?> = context.dataStore.data
        .map { it[TITULAIRE_WORD_KEY] }

    val remplacantWordFlow: Flow<String?> = context.dataStore.data
        .map { it[REMPLACANT_WORD_KEY] }

    val registeredPlayersFlow: Flow<List<Player>> = context.dataStore.data
        .map { preferences ->
            val playersJson = preferences[REGISTERED_PLAYERS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Player>>(playersJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    // Nouvelle méthode pour compter les joueurs par rôle
    suspend fun getPlayerCountByRole(role: String): Int {
        return registeredPlayersFlow.first().count { it.role == role }
    }
    suspend fun saveSecretWords(words: Set<String>) {
        context.dataStore.edit { it[SECRET_WORDS_KEY] = words }
    }

    suspend fun saveSelectedBallWord(word: String) {
        context.dataStore.edit { it[SELECTED_BALL_WORD_KEY] = word }
    }

    suspend fun saveCurrentBall(word: String?) {
        context.dataStore.edit {
            if (word != null) {
                it[CURRENT_BALL_KEY] = word
            } else {
                it.remove(CURRENT_BALL_KEY)
            }
        }
    }
    suspend fun markGameAsCompleted() {
        context.dataStore.edit { preferences ->
            preferences[GAME_COMPLETED_KEY] = true
        }
    }

    suspend fun saveCurrentRole(role: String) {
        context.dataStore.edit { it[CURRENT_ROLE_KEY] = role }
    }

    // Méthode pour initialiser les mots par rôle
    suspend fun initializeGameWords() {
        val words = secretWordsFlow.map { it.toList().shuffled() }.collect { shuffledWords ->
            if (shuffledWords.size >= 2) {
                context.dataStore.edit {
                    it[TITULAIRE_WORD_KEY] = shuffledWords[0]
                    it[REMPLACANT_WORD_KEY] = shuffledWords[1]
                }
            }
        }
    }
    // Ajoutez cette méthode
    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    // Flux pour récupérer les valeurs, avec des valeurs par défaut
    val playersFlow: Flow<Int> = context.dataStore.data.map { it[PLAYERS_KEY] ?: 3 }
    val titulairesFlow: Flow<Int> = context.dataStore.data.map { it[TITULAIRES_KEY] ?: 2 }
    val footixFlow: Flow<Int> = context.dataStore.data.map { it[FOOTIX_KEY] ?: 0 }
    val remplacantsFlow: Flow<Int> = context.dataStore.data.map { it[REMPLACANTS_KEY] ?: 1 }

    val rolesFlow: Flow<List<String>> = combine(
        titulairesFlow,
        footixFlow,
        remplacantsFlow,
        playersFlow
    ) { titulaires, footix, remplacants, totalPlayers ->
        // S'assurer que nous avons exactement le bon nombre de rôles
        val rolesList = mutableListOf<String>()

        // Ajout des titulaires
        repeat(titulaires) { rolesList.add("Titulaire") }

        // Ajout des footix
        repeat(footix) { rolesList.add("Footix") }

        // Ajout des remplaçants
        repeat(remplacants) { rolesList.add("Remplaçant") }

        // Vérification finale
        if (rolesList.size != totalPlayers) {
            // Correction si nécessaire (ajout ou suppression de rôles)
            while (rolesList.size < totalPlayers) {
                rolesList.add("Titulaire") // Par défaut, ajouter des titulaires
            }
            while (rolesList.size > totalPlayers) {
                rolesList.removeLast() // Enlever le dernier rôle
            }
        }

        rolesList.shuffled() // Mélanger les rôles pour éviter un ordre prévisible
    }
    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED_KEY] ?: true }
    val currentLanguageFlow: Flow<String> = context.dataStore.data.map { it[CURRENT_LANGUAGE_KEY] ?: "Français" }

    // Nouvelle méthode pour obtenir les ballons disponibles
    val availableBallsFlow: Flow<Map<String, Int>> = combine(
        rolesFlow,
        registeredPlayersFlow
    ) { roles, registeredPlayers ->
        // Compte le nombre de chaque rôle configuré dans la partie
        val roleCounts = roles.groupingBy { it }.eachCount()

        // Compte le nombre de chaque rôle déjà attribué
        val usedRoleCounts = registeredPlayers.groupingBy { it.role }.eachCount()

        // Calcule le nombre de ballons disponibles pour chaque rôle
        roleCounts.mapValues { (role, count) ->
            count - (usedRoleCounts[role] ?: 0)
        }.filter { it.value > 0 } // Ne garde que les rôles avec des ballons disponibles
    }

    // Méthodes pour enregistrer les valeurs
    suspend fun savePlayers(value: Int) {
        context.dataStore.edit { it[PLAYERS_KEY] = value }
    }

    suspend fun saveTitulaires(value: Int) {
        context.dataStore.edit { it[TITULAIRES_KEY] = value }
    }

    suspend fun saveFootix(value: Int) {
        context.dataStore.edit { it[FOOTIX_KEY] = value }
    }

    suspend fun saveRemplacants(value: Int) {
        context.dataStore.edit { it[REMPLACANTS_KEY] = value }
    }

    suspend fun saveSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED_KEY] = value }
    }

    suspend fun saveCurrentLanguage(value: String) {
        context.dataStore.edit { it[CURRENT_LANGUAGE_KEY] = value }
    }

    // Flux des ballons sélectionnés
    val selectedBallsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[SELECTED_BALLS_KEY] ?: emptySet() }

    // Sauvegarder un ballon sélectionné
    suspend fun saveSelectedBall(word: String?, role: String) {
        context.dataStore.edit { preferences ->
            if (word != null) {
                val updatedBalls = preferences[SELECTED_BALLS_KEY]?.toMutableSet() ?: mutableSetOf()
                updatedBalls.add(word)
                preferences[SELECTED_BALLS_KEY] = updatedBalls
            }
            preferences[CURRENT_ROLE_KEY] = role
            if (word != null) {
                preferences[CURRENT_BALL_KEY] = word
            } else {
                preferences.remove(CURRENT_BALL_KEY)
            }
        }
    }

    // Nouveau flux pour l'URI de la photo
    val playerPhotoUriFlow: Flow<String?> = context.dataStore.data
        .map { it[PLAYER_PHOTO_URI_KEY] }

    val playerNameFlow: Flow<String?> = context.dataStore.data
        .map { it[PLAYER_NAME_KEY] }

    suspend fun savePlayerPhotoUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_PHOTO_URI_KEY] = uri
        }
    }

    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_NAME_KEY] = name
        }
    }

    fun getPhotoFile(): File {
        val photoDir = File(context.filesDir, "player_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        return File(photoDir, "player_photo_${System.currentTimeMillis()}.jpg")
    }

    // Méthode modifiée pour attribuer correctement les mots selon le rôle
    suspend fun registerPlayer(name: String, photoUri: String?) {
        try {
            context.dataStore.edit { preferences ->
                val currentPlayers = try {
                    json.decodeFromString<List<Player>>(preferences[REGISTERED_PLAYERS_KEY] ?: "[]")
                } catch (e: Exception) {
                    emptyList()
                }

                // Vérifier si le joueur existe déjà
                val existingPlayerIndex = currentPlayers.indexOfFirst {
                    it.name.equals(name, ignoreCase = true)
                }

                val playerId = UUID.randomUUID().toString()
                val currentRole = preferences[CURRENT_ROLE_KEY] ?: "Inconnu"

                // Déterminer le mot en fonction du rôle
                val playerWord = when (currentRole) {
                    "Titulaire" -> preferences[TITULAIRE_WORD_KEY]
                    "Remplaçant" -> preferences[REMPLACANT_WORD_KEY]
                    "Footix" -> null // Le Footix n'a pas de mot
                    else -> preferences[CURRENT_BALL_KEY]
                }

                val newPlayer = Player(
                    id = playerId,
                    name = name,
                    photoUri = photoUri,
                    role = currentRole,
                    word = playerWord
                )

                // Mettre à jour ou ajouter le joueur
                val updatedPlayers = if (existingPlayerIndex >= 0) {
                    currentPlayers.toMutableList().apply {
                        set(existingPlayerIndex, newPlayer)
                    }
                } else {
                    currentPlayers + newPlayer
                }

                preferences[REGISTERED_PLAYERS_KEY] = json.encodeToString(updatedPlayers)

                // Vérifier si tous les joueurs sont enregistrés
                val totalPlayers = preferences[PLAYERS_KEY] ?: 3
                preferences[ALL_PLAYERS_REGISTERED_KEY] = updatedPlayers.size >= totalPlayers

                // Ajouter le ballon actuel à la liste des ballons sélectionnés
                val currentBall = preferences[CURRENT_BALL_KEY]
                if (currentBall != null) {
                    val updatedBalls = preferences[SELECTED_BALLS_KEY]?.toMutableSet() ?: mutableSetOf()
                    updatedBalls.add(currentBall)
                    preferences[SELECTED_BALLS_KEY] = updatedBalls
                }
                // Forcer un rafraîchissement en modifiant puis remettant la valeur
                val players = preferences[PLAYERS_KEY] ?: 3
                preferences[PLAYERS_KEY] = players
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    /**
     * Récupère la liste des joueurs enregistrés à partir des préférences
     * @param preferences Les préférences du DataStore
     * @return La liste des joueurs enregistrés
     */
    private fun getRegisteredPlayersFromPreferences(preferences: Preferences): List<Player> {
        val playersJson = preferences[REGISTERED_PLAYERS_KEY] ?: return emptyList()

        return try {
            Json.decodeFromString<List<Player>>(playersJson)
        } catch (e: Exception) {
            Log.e("DataStoreManager", "Erreur lors de la désérialisation des joueurs: ${e.message}")
            emptyList()
        }
    }

    fun isPlayerNameTakenFlow(name: String) = context.dataStore.data.map { preferences ->
        val registeredPlayers = getRegisteredPlayersFromPreferences(preferences)
        registeredPlayers.any { it.name == name }
    }

    suspend fun resetGame(turnManager: TurnManager? = null) {
        context.dataStore.edit { preferences ->
            preferences.remove(REGISTERED_PLAYERS_KEY)
            preferences.remove(SELECTED_BALLS_KEY)
            preferences.remove(ALL_PLAYERS_REGISTERED_KEY)
            preferences.remove(PLAYER_NAME_KEY)
            preferences.remove(PLAYER_PHOTO_URI_KEY)
            preferences.remove(CURRENT_BALL_KEY)
            preferences.remove(CURRENT_ROLE_KEY)
            preferences.remove(SELECTED_BALL_WORD_KEY)
            preferences.remove(TITULAIRE_WORD_KEY)
            preferences.remove(REMPLACANT_WORD_KEY)
            preferences.remove(GAME_COMPLETED_KEY) 
        }
        
        // Réinitialiser aussi le TurnManager si fourni
        turnManager?.resetTurnManager()
    }

    suspend fun forceRefreshRegisteredPlayers() {
        context.dataStore.edit { preferences ->
            val currentPlayers = try {
                json.decodeFromString<List<Player>>(preferences[REGISTERED_PLAYERS_KEY] ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            preferences[REGISTERED_PLAYERS_KEY] = json.encodeToString(currentPlayers)
        }
    }

    suspend fun resetPlayerPhoto() {
        context.dataStore.edit { preferences ->
            preferences.remove(PLAYER_PHOTO_URI_KEY)
        }
        
        // Supprimer également tous les fichiers photos temporaires
        val photoDir = File(context.filesDir, "player_photos")
        if (photoDir.exists()) {
            photoDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("player_photo_")) {
                    file.delete()
                }
            }
        }
    }

    suspend fun resetPlayerName() {
        context.dataStore.edit { preferences ->
            preferences.remove(PLAYER_NAME_KEY)
        }
    }
}