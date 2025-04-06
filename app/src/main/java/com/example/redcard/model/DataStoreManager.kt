package com.example.redcard.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

        // Nouvelles clés pour la gestion des comptes
        val REGISTERED_PLAYERS_KEY = stringPreferencesKey("registered_players")
        val CURRENT_BALL_KEY = stringPreferencesKey("current_ball")
        val CURRENT_ROLE_KEY = stringPreferencesKey("current_role")
        val ALL_PLAYERS_REGISTERED_KEY = booleanPreferencesKey("all_players_registered")
    }

    // Mots secrets par défaut
    private val defaultSecretWords = setOf(
        "Zidane", "Platini", "Benzema", "Mbappé", "Henry",
        "Cantona", "Papin", "Griezmann", "Thuram", "Deschamps"
    )

    // Flux pour les mots secrets
    val secretWordsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[SECRET_WORDS_KEY] ?: defaultSecretWords }

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


    val registeredPlayersFlow: Flow<List<Player>> = context.dataStore.data
        .map { preferences ->
            val playersJson = preferences[REGISTERED_PLAYERS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Player>>(playersJson)
            } catch (e: Exception) {
                emptyList()
            }
        }


    suspend fun saveSecretWords(words: Set<String>) {
        context.dataStore.edit { it[SECRET_WORDS_KEY] = words }
    }

    suspend fun saveSelectedBallWord(word: String) {
        context.dataStore.edit { it[SELECTED_BALL_WORD_KEY] = word }
    }

    suspend fun saveCurrentBall(word: String) {
        context.dataStore.edit { it[CURRENT_BALL_KEY] = word }
    }

    suspend fun saveCurrentRole(role: String) {
        context.dataStore.edit { it[CURRENT_ROLE_KEY] = role }
    }

    // Flux pour récupérer les valeurs, avec des valeurs par défaut
    val playersFlow: Flow<Int> = context.dataStore.data.map { it[PLAYERS_KEY] ?: 3 }
    val titulairesFlow: Flow<Int> = context.dataStore.data.map { it[TITULAIRES_KEY] ?: 2 }
    val footixFlow: Flow<Int> = context.dataStore.data.map { it[FOOTIX_KEY] ?: 0 }
    val remplacantsFlow: Flow<Int> = context.dataStore.data.map { it[REMPLACANTS_KEY] ?: 1 }

    val rolesFlow: Flow<List<String>> = combine(
        titulairesFlow,
        footixFlow,
        remplacantsFlow
    ) { titulaires, footix, remplacants ->
        List(titulaires) { "Titulaire" } +
                List(footix) { "Footix" } +
                List(remplacants) { "Remplaçant" }
    }.map { it.shuffled() } // Mélanger les rôles pour éviter un ordre prévisible

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED_KEY] ?: true }
    val currentLanguageFlow: Flow<String> = context.dataStore.data.map { it[CURRENT_LANGUAGE_KEY] ?: "Français" }

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
    suspend fun saveSelectedBall(word: String, role: String) {
        context.dataStore.edit { preferences ->
            val updatedBalls = preferences[SELECTED_BALLS_KEY]?.toMutableSet() ?: mutableSetOf()
            updatedBalls.add(word)
            preferences[SELECTED_BALLS_KEY] = updatedBalls
            preferences[CURRENT_BALL_KEY] = word
            preferences[CURRENT_ROLE_KEY] = role
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
                val currentBall = preferences[CURRENT_BALL_KEY]
                val currentRole = preferences[CURRENT_ROLE_KEY] ?: "Inconnu"

                val newPlayer = Player(
                    id = playerId,
                    name = name,
                    photoUri = photoUri,
                    role = currentRole,
                    word = currentBall
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

                // Mettre à jour la liste des ballons sélectionnés de manière plus robuste
                if (currentBall != null) {
                    val updatedBalls = preferences[SELECTED_BALLS_KEY]?.toMutableSet() ?: mutableSetOf()
                    updatedBalls.add(currentBall)
                    preferences[SELECTED_BALLS_KEY] = updatedBalls
                }

                // Garder ces valeurs pour le débogage
                // NE PAS les supprimer jusqu'à ce que le joueur soit bien enregistré
                // On les supprimera après avoir vérifié que tout fonctionne
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    
    }
    // Ajout d'une méthode pour vérifier si un nom de joueur est déjà utilisé
    val isPlayerNameTakenFlow: Flow<Boolean> = combine(
        registeredPlayersFlow,
        playerNameFlow
    ) { players, currentName ->
        currentName?.let { name ->
            players.any { it.name.equals(name, ignoreCase = true) }
        } ?: false
    }

    // Modification de resetGame pour réinitialiser complètement le jeu
    suspend fun resetGame() {
        context.dataStore.edit { preferences ->
            preferences.remove(REGISTERED_PLAYERS_KEY)
            preferences.remove(SELECTED_BALLS_KEY)
            preferences.remove(ALL_PLAYERS_REGISTERED_KEY)
//            preferences.remove(PLAYER_NAME_KEY)
//            preferences.remove(PLAYER_PHOTO_URI_KEY)
//            preferences.remove(CURRENT_BALL_KEY)
//            preferences.remove(CURRENT_ROLE_KEY)
            preferences.remove(SELECTED_BALL_WORD_KEY) // Ajouté pour réinitialiser complètement
        }
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
}