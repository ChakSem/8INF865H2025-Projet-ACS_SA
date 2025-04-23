package com.example.redcard.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.redcard.model.TurnManager
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

val Context.dataStore by preferencesDataStore(name = "configurations")

/**
 * Modèle représentant un joueur dans le jeu.
 */
@Serializable
data class Player(
    @SerialName("id") val id: String = UUID.randomUUID().toString(),
    @SerialName("name") val name: String,
    @SerialName("photoUri") val photoUri: String?,
    @SerialName("role") val role: String,
    @SerialName("word") val word: String?
)

/**
 * Gestionnaire central pour toutes les données persistantes de l'application.
 * Utilise DataStore pour sauvegarder les configurations, joueurs et état du jeu.
 */
class DataStoreManager(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        // Clés de configuration générales
        val PLAYERS_KEY = intPreferencesKey("players")
        val TITULAIRES_KEY = intPreferencesKey("titulaires")
        val FOOTIX_KEY = intPreferencesKey("footix")
        val REMPLACANTS_KEY = intPreferencesKey("remplacants")
        val SECRET_WORDS_KEY = stringSetPreferencesKey("secret_words")
        val SELECTED_BALL_WORD_KEY = stringPreferencesKey("selected_ball_word")
        val PLAYER_PHOTO_URI_KEY = stringPreferencesKey("player_photo_uri")
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val SELECTED_BALLS_KEY = stringSetPreferencesKey("selected_balls")
        val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")

        // Clés de gestion des joueurs et des mots
        val REGISTERED_PLAYERS_KEY = stringPreferencesKey("registered_players")
        val CURRENT_BALL_KEY = stringPreferencesKey("current_ball")
        val CURRENT_ROLE_KEY = stringPreferencesKey("current_role")
        val ALL_PLAYERS_REGISTERED_KEY = booleanPreferencesKey("all_players_registered")
        val TITULAIRE_WORD_KEY = stringPreferencesKey("titulaire_word")
        val REMPLACANT_WORD_KEY = stringPreferencesKey("remplacant_word")
        val GAME_COMPLETED_KEY = booleanPreferencesKey("game_completed")
    }

    // Mots secrets par défaut
    private val defaultSecretWords = setOf(
        "Zidane", "Platini", "Benzema", "Mbappé", "Henry",
        "Cantona", "Papin", "Griezmann", "Thuram", "Deschamps"
    )

    // Flows d'état du jeu
    val secretWordsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[SECRET_WORDS_KEY] ?: defaultSecretWords }

    val gameCompletedFlow: Flow<Boolean> = context.dataStore.data
        .map { it[GAME_COMPLETED_KEY] ?: false }

    val musicEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[MUSIC_ENABLED_KEY] ?: true }

    val currentBallFlow: Flow<String?> = context.dataStore.data
        .map { it[CURRENT_BALL_KEY] }

    val allPlayersRegisteredFlow: Flow<Boolean> = context.dataStore.data
        .map { it[ALL_PLAYERS_REGISTERED_KEY] ?: false }

    // Flows pour les mots par rôle
    val titulaireWordFlow: Flow<String?> = context.dataStore.data
        .map { it[TITULAIRE_WORD_KEY] }

    val remplacantWordFlow: Flow<String?> = context.dataStore.data
        .map { it[REMPLACANT_WORD_KEY] }

    /**
     * Flow pour accéder à la liste des joueurs enregistrés.
     */
    val registeredPlayersFlow: Flow<List<Player>> = context.dataStore.data
        .map { preferences ->
            val playersJson = preferences[REGISTERED_PLAYERS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Player>>(playersJson)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Sauvegarde l'ensemble des mots secrets disponibles.
     */
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

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    // Flows pour la configuration des joueurs
    val playersFlow: Flow<Int> = context.dataStore.data.map { it[PLAYERS_KEY] ?: 3 }
    val titulairesFlow: Flow<Int> = context.dataStore.data.map { it[TITULAIRES_KEY] ?: 2 }
    val footixFlow: Flow<Int> = context.dataStore.data.map { it[FOOTIX_KEY] ?: 0 }
    val remplacantsFlow: Flow<Int> = context.dataStore.data.map { it[REMPLACANTS_KEY] ?: 1 }

    /**
     * Flow pour générer la liste des rôles selon la configuration actuelle.
     * Retourne une liste mélangée pour l'attribution aléatoire des rôles.
     */
    val rolesFlow: Flow<List<String>> = combine(
        titulairesFlow,
        footixFlow,
        remplacantsFlow,
        playersFlow
    ) { titulaires, footix, remplacants, totalPlayers ->
        val rolesList = mutableListOf<String>()

        repeat(titulaires) { rolesList.add("Titulaire") }
        repeat(footix) { rolesList.add("Footix") }
        repeat(remplacants) { rolesList.add("Remplaçant") }

        if (rolesList.size != totalPlayers) {
            while (rolesList.size < totalPlayers) {
                rolesList.add("Titulaire")
            }
            while (rolesList.size > totalPlayers) {
                rolesList.removeLast()
            }
        }

        rolesList.shuffled()
    }

    /**
     * Flow calculant les ballons disponibles en fonction des rôles configurés
     * et déjà attribués.
     */
    val availableBallsFlow: Flow<Map<String, Int>> = combine(
        rolesFlow,
        registeredPlayersFlow
    ) { roles, registeredPlayers ->
        val roleCounts = roles.groupingBy { it }.eachCount()
        val usedRoleCounts = registeredPlayers.groupingBy { it.role }.eachCount()

        roleCounts.mapValues { (role, count) ->
            count - (usedRoleCounts[role] ?: 0)
        }.filter { it.value > 0 }
    }

    // Méthodes de sauvegarde de configuration
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

    /**
     * Sauvegarde un ballon sélectionné et son rôle associé.
     */
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

    val playerPhotoUriFlow: Flow<String?> = context.dataStore.data
        .map { it[PLAYER_PHOTO_URI_KEY] }

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

    /**
     * Crée un fichier pour stocker la photo du joueur.
     */
    fun getPhotoFile(): File {
        val photoDir = File(context.filesDir, "player_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        return File(photoDir, "player_photo_${System.currentTimeMillis()}.jpg")
    }

    /**
     * Enregistre un nouveau joueur avec son nom, photo et attributs selon son rôle.
     */
    suspend fun registerPlayer(name: String, photoUri: String?) {
        try {
            context.dataStore.edit { preferences ->
                val currentPlayers = try {
                    json.decodeFromString<List<Player>>(preferences[REGISTERED_PLAYERS_KEY] ?: "[]")
                } catch (e: Exception) {
                    emptyList()
                }

                val existingPlayerIndex = currentPlayers.indexOfFirst {
                    it.name.equals(name, ignoreCase = true)
                }

                val playerId = UUID.randomUUID().toString()
                val currentRole = preferences[CURRENT_ROLE_KEY] ?: "Inconnu"

                // Attribution du mot selon le rôle
                val playerWord = when (currentRole) {
                    "Titulaire" -> preferences[TITULAIRE_WORD_KEY]
                    "Remplaçant" -> preferences[REMPLACANT_WORD_KEY]
                    "Footix" -> null
                    else -> preferences[CURRENT_BALL_KEY]
                }

                val newPlayer = Player(
                    id = playerId,
                    name = name,
                    photoUri = photoUri,
                    role = currentRole,
                    word = playerWord
                )

                val updatedPlayers = if (existingPlayerIndex >= 0) {
                    currentPlayers.toMutableList().apply {
                        set(existingPlayerIndex, newPlayer)
                    }
                } else {
                    currentPlayers + newPlayer
                }

                preferences[REGISTERED_PLAYERS_KEY] = json.encodeToString(updatedPlayers)

                val totalPlayers = preferences[PLAYERS_KEY] ?: 3
                preferences[ALL_PLAYERS_REGISTERED_KEY] = updatedPlayers.size >= totalPlayers

                val currentBall = preferences[CURRENT_BALL_KEY]
                if (currentBall != null) {
                    val updatedBalls = preferences[SELECTED_BALLS_KEY]?.toMutableSet() ?: mutableSetOf()
                    updatedBalls.add(currentBall)
                    preferences[SELECTED_BALLS_KEY] = updatedBalls
                }

                val players = preferences[PLAYERS_KEY] ?: 3
                preferences[PLAYERS_KEY] = players
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Récupère la liste des joueurs enregistrés à partir des préférences.
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

    /**
     * Vérifie si un nom de joueur est déjà pris.
     */
    fun isPlayerNameTakenFlow(name: String) = context.dataStore.data.map { preferences ->
        val registeredPlayers = getRegisteredPlayersFromPreferences(preferences)
        registeredPlayers.any { it.name == name }
    }

    /**
     * Réinitialise l'état du jeu et optionnellement le TurnManager.
     */
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

        turnManager?.resetTurnManager()
    }

    /**
     * Force le rafraîchissement de la liste des joueurs enregistrés.
     */
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

    /**
     * Supprime la photo d'un joueur et nettoie les fichiers temporaires.
     */
    suspend fun resetPlayerPhoto() {
        context.dataStore.edit { preferences ->
            preferences.remove(PLAYER_PHOTO_URI_KEY)
        }

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